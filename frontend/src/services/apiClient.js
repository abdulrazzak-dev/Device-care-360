import axios from 'axios';

const API_GATEWAY_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

// In-flight GET request deduplication map
const inFlightRequests = new Map();

// In-memory GET response cache (keyed by normalized URL + query params)
const responseCache = new Map();
const DEFAULT_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

// Endpoints that are eligible for short-term client caching
const CACHEABLE_URL_PATTERNS = [
  '/api/devices/categories',
  '/api/devices/brands',
  '/api/devices/issues',
  '/api/repair-guides',
];

const getRequestKey = (config) => {
  const method = (config.method || 'get').toLowerCase();
  const url = config.url || '';
  const params = config.params ? JSON.stringify(config.params) : '';
  return `${method}:${url}:${params}`;
};

const isCacheableUrl = (url = '') => {
  return CACHEABLE_URL_PATTERNS.some((pattern) => url.startsWith(pattern));
};

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const parseRetryAfter = (retryAfterHeader) => {
  if (!retryAfterHeader) return null;
  // If it's a number (seconds)
  const seconds = Number(retryAfterHeader);
  if (!isNaN(seconds) && seconds > 0) {
    return seconds * 1000;
  }
  // If it's an HTTP date
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

// Request interceptor: attach token & check cache for GET requests
apiClient.interceptors.request.use(
  (config) => {
    const rawToken = localStorage.getItem('token');
    if (rawToken) {
      const cleanToken = rawToken.replace(/['"]+/g, '').trim();
      config.headers.Authorization = `Bearer ${cleanToken}`;
    }

    const method = (config.method || 'get').toLowerCase();
    const url = config.url || '';

    // Cache hit check for GET requests
    if (method === 'get' && isCacheableUrl(url) && !config.skipCache) {
      const cacheKey = getRequestKey(config);
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

// Response interceptor: handle caching, 429 backoff retries for GET, 401s, error format
apiClient.interceptors.response.use(
  (response) => {
    const config = response.config || {};
    const method = (config.method || 'get').toLowerCase();
    const url = config.url || '';

    // Store in cache if eligible
    if (method === 'get' && isCacheableUrl(url) && response.data) {
      const cacheKey = getRequestKey(config);
      const ttl = config.cacheTTL || DEFAULT_CACHE_TTL_MS;
      responseCache.set(cacheKey, {
        data: response.data,
        expiresAt: Date.now() + ttl,
      });
    }

    return response.data;
  },
  async (error) => {
    const config = error.config;

    // Retry policy: Only retry idempotent GET requests on 429, 502, 503, 504 or network errors
    const method = (config?.method || 'get').toLowerCase();
    const status = error.response?.status;
    const isRetryableStatus = status === 429 || status === 502 || status === 503 || status === 504 || !error.response;
    const isGet = method === 'get';

    if (config && isGet && isRetryableStatus) {
      config.__retryCount = config.__retryCount || 0;
      const MAX_RETRIES = 3;

      if (config.__retryCount < MAX_RETRIES) {
        config.__retryCount += 1;

        // Calculate delay: respect Retry-After or calculate exponential backoff + jitter
        let delayMs = 1000 * Math.pow(2, config.__retryCount - 1);
        const retryAfterMs = parseRetryAfter(error.response?.headers?.['retry-after']);
        if (retryAfterMs !== null) {
          delayMs = Math.min(retryAfterMs, 10000);
        } else {
          const jitter = Math.random() * 300;
          delayMs = Math.min(delayMs + jitter, 10000);
        }

        console.warn(`[apiClient] Retrying GET ${config.url} (attempt ${config.__retryCount}/${MAX_RETRIES}) after ${Math.round(delayMs)}ms due to status ${status || 'Network Error'}`);
        await sleep(delayMs);
        return apiClient(config);
      }
    }

    // Build user-friendly message
    let message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An unexpected error occurred';

    if (status === 429) {
      message = 'Too many requests. Please wait a moment and try again.';
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

// Wrapper with in-flight GET promise deduplication
const wrappedApiClient = {
  ...apiClient,
  get: (url, config = {}) => {
    const key = `get:${url}:${config.params ? JSON.stringify(config.params) : ''}`;
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
