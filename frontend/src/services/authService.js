import apiClient from './apiClient';

export const authService = {
  login: async (credentials) => {
    const res = await apiClient.post('/api/auth/login', credentials);
    if (res.data?.token) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data));
    }
    return res;
  },

  register: async (userData) => {
    const res = await apiClient.post('/api/auth/register', userData);
    if (res.data?.token) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data));
    }
    return res;
  },

  validateToken: async (token) => {
    return await apiClient.get(`/api/auth/validate?token=${token}`);
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }
};
