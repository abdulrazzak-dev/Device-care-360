import axios from 'axios';

const API_GATEWAY_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

// In-flight GET request deduplication map
const inFlightRequests = new Map();

// In-memory GET response cache (keyed by normalized URL + query params)
const responseCache = new Map();
const DEFAULT_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

// Configuration for bounded retries
const MAX_GET_RETRIES = 2;
const BASE_RETRY_DELAY_MS = 1500;
const MAX_RETRY_DELAY_MS = 12000; // 12 seconds maximum allowable client wait

// Endpoints eligible for short-term client caching
const CACHEABLE_URL_PATTERNS = [
  '/api/devices/categories',
  '/api/devices/brands',
  '/api/devices/issues',
  '/api/repair-guides',
];

/**
 * Normalizes URL and parameters into a canonical deterministic key.
 * Handles both embedded query strings (e.g. /api/devices/brands?category=Mobile)
 * and params objects ({ category: 'Mobile' }).
 */
const normalizeRequestKey = (config = {}) => {
  const method = (config.method || 'get').toLowerCase();
  const rawUrl = config.url || '';
  
  try {
    const parsedUrl = new URL(rawUrl, 'http://dummy-base');
    const searchParams = new URLSearchParams(parsedUrl.search);

    if (config.params && typeof config.params === 'object') {
      Object.entries(config.params).forEach(([key, val]) => {
        if (val !== undefined && val !== null) {
          searchParams.set(key, String(val));
        }
      });
    }

    // Sort params for deterministic cache key
    const sortedEntries = Array.from(searchParams.entries()).sort((a, b) => a[0].localeCompare(b[0]));
    const queryString = sortedEntries.length > 0 ? `?${new URLSearchParams(sortedEntries).toString()}` : '';

    return `${method}:${parsedUrl.pathname}${queryString}`;
  } catch {
    const params = config.params ? JSON.stringify(config.params) : '';
    return `${method}:${rawUrl}:${params}`;
  }
};

const isCacheableUrl = (url = '') => {
  return CACHEABLE_URL_PATTERNS.some((pattern) => url.startsWith(pattern));
};

/**
 * Promise-based sleep that immediately aborts and clears timer if signal is cancelled.
 */
const sleepWithAbort = (ms, signal) => {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) {
      return reject(new axios.CanceledError('Request aborted during retry delay'));
    }

    const timer = setTimeout(() => {
      if (signal) {
        signal.removeEventListener('abort', onAbort);
      }
      resolve();
    }, ms);

    const onAbort = () => {
      clearTimeout(timer);
      reject(new axios.CanceledError('Request aborted during retry delay'));
    };

    if (signal) {
      signal.addEventListener('abort', onAbort, { once: true });
    }
  });
};

/**
 * Parses HTTP Retry-After header (seconds integer or RFC 1123 HTTP date).
 */
const parseRetryAfter = (retryAfterHeader) => {
  if (!retryAfterHeader) return null;
  const seconds = Number(retryAfterHeader);
  if (!isNaN(seconds) && seconds > 0) {
    return seconds * 1000;
  }
  const dateParsed = Date.parse(retryAfterHeader);
  if (!isNaN(dateParsed)) {
    const diff = dateParsed - Date.now();
    return Math.max(diff, 500);
  }
  return null;
};

const apiClient = axios.create({
  baseURL: API_GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000,
});

// Request interceptor: attach auth token & check cache for GET requests
apiClient.interceptors.request.use(
  (config) => {
    const rawToken = localStorage.getItem('token');
    if (rawToken) {
      const cleanToken = rawToken.replace(/['"]+/g, '').trim();
      config.headers.Authorization = `Bearer ${cleanToken}`;
    }

    const method = (config.method || 'get').toLowerCase();
    const url = config.url || '';

    // Cache hit check for eligible GET requests
    if (method === 'get' && isCacheableUrl(url) && !config.skipCache) {
      const cacheKey = normalizeRequestKey(config);
      const cached = responseCache.get(cacheKey);
      if (cached && Date.now() < cached.expiresAt) {
        config.adapter = () =>
          Promise.resolve({
            data: cached.data,
            status: 200,
            statusText: 'OK (cached)',
            headers: {},
            config,
          });
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: caching, status-aware bounded retry policy, 401 redirect, unified errors
apiClient.interceptors.response.use(
  (response) => {
    const config = response.config || {};
    const method = (config.method || 'get').toLowerCase();
    const url = config.url || '';

    // Store in cache if eligible
    if (method === 'get' && isCacheableUrl(url) && response.data) {
      const cacheKey = normalizeRequestKey(config);
      const ttl = config.cacheTTL || DEFAULT_CACHE_TTL_MS;
      responseCache.set(cacheKey, {
        data: response.data,
        expiresAt: Date.now() + ttl,
      });
    }

    return response.data;
  },
  async (error) => {
    // If request was explicitly cancelled, propagate cancellation immediately without retrying
    if (axios.isCancel(error) || error.name === 'CanceledError') {
      return Promise.reject(error);
    }

    const config = error.config;
    const method = (config?.method || 'get').toLowerCase();
    const status = error.response?.status;

    // Strict retry eligibility:
    // 1. ONLY idempotent GET requests are eligible (never POST/PUT/PATCH/DELETE)
    // 2. ONLY transient network errors or specific gateway/proxy status codes (429, 502, 503, 504)
    // 3. NEVER retry ordinary 4xx errors (400, 401, 403, 404, 405, 422, etc.)
    const isIdempotentGet = method === 'get';
    const isTransientError = !error.response || [502, 503, 504].includes(status);
    const isRateLimited = status === 429;

    if (config && isIdempotentGet && (isTransientError || isRateLimited)) {
      config.__retryCount = config.__retryCount || 0;

      if (config.__retryCount < MAX_GET_RETRIES) {
        const retryAfterMs = parseRetryAfter(error.response?.headers?.['retry-after']);

        // If the server explicitly specifies a Retry-After that exceeds our allowable wait,
        // DO NOT retry early. Return a controlled error immediately.
        if (isRateLimited && retryAfterMs !== null && retryAfterMs > MAX_RETRY_DELAY_MS) {
          const waitSec = Math.ceil(retryAfterMs / 1000);
          console.warn(`[apiClient] Rate limit cooldown (${waitSec}s) exceeds client limit (${MAX_RETRY_DELAY_MS / 1000}s). Not retrying.`);
        } else {
          config.__retryCount += 1;

          // Compute bounded exponential backoff with randomized jitter
          let delayMs;
          if (isRateLimited && retryAfterMs !== null) {
            delayMs = Math.min(retryAfterMs, MAX_RETRY_DELAY_MS);
          } else {
            const exponentialDelay = BASE_RETRY_DELAY_MS * Math.pow(2, config.__retryCount - 1);
            const jitter = Math.random() * 500;
            delayMs = Math.min(exponentialDelay + jitter, MAX_RETRY_DELAY_MS);
          }

          console.warn(
            `[apiClient] Retrying GET ${config.url} (attempt ${config.__retryCount}/${MAX_GET_RETRIES}) after ${Math.round(delayMs)}ms due to ${status ? `status ${status}` : 'Network Error'}`
          );

          try {
            await sleepWithAbort(delayMs, config.signal);
            return apiClient(config);
          } catch (abortErr) {
            return Promise.reject(abortErr);
          }
        }
      }
    }

    // Build user-friendly message
    const renderRouting = error.response?.headers?.['x-render-routing'];
    let message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An unexpected error occurred';

    if (status === 429) {
      if (renderRouting === 'hibernate-rate-limited') {
        message = 'Service is waking up. Please wait a moment and try again.';
      } else {
        message = 'Too many requests. Please wait a moment and try again.';
      }
    } else if (status === 404) {
      message = error.response?.data?.message || 'The requested service or endpoint was not found.';
    } else if (status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (typeof window !== 'undefined' && window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login?expired=true';
      }
    }

    return Promise.reject({
      status: error.response?.status,
      message,
      data: error.response?.data,
    });
  }
);

// Wrapper with unified in-flight GET promise deduplication
const wrappedApiClient = {
  ...apiClient,
  get: (url, config = {}) => {
    const key = normalizeRequestKey({ method: 'get', url, ...config });
    if (inFlightRequests.has(key)) {
      return inFlightRequests.get(key);
    }
    const promise = apiClient.get(url, config).finally(() => {
      inFlightRequests.delete(key);
    });
    inFlightRequests.set(key, promise);
    return promise;
  },
  post: (url, data, config) => apiClient.post(url, data, config),
  put: (url, data, config) => apiClient.put(url, data, config),
  patch: (url, data, config) => apiClient.patch(url, data, config),
  delete: (url, config) => apiClient.delete(url, config),
};

export default wrappedApiClient;
