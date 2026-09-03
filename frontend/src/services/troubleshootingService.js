import apiClient from './apiClient';

export const troubleshootingService = {
  analyze: async (payload) => {
    return await apiClient.post('/api/troubleshooting/analyze', payload);
  },

  chat: async (payload) => {
    return await apiClient.post('/api/troubleshooting/chat', payload);
  },

  searchCategory: async (category) => {
    return await apiClient.post('/api/troubleshooting/category-search', { category });
  },

  searchBrand: async (brand) => {
    return await apiClient.post('/api/troubleshooting/brand-search', { brand });
  },

  searchIssue: async (issue) => {
    return await apiClient.post('/api/troubleshooting/issue-search', { issue });
  },

  getHistory: async (userId) => {
    return await apiClient.get(`/api/troubleshooting/history/${userId}`);
  }
};
