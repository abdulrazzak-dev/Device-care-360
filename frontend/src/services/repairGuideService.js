import apiClient from './apiClient';

export const repairGuideService = {
  getGuides: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return await apiClient.get(`/api/repair-guides${query ? `?${query}` : ''}`);
  },

  getGuideById: async (id) => {
    return await apiClient.get(`/api/repair-guides/${id}`);
  },

  createGuide: async (guideData) => {
    return await apiClient.post('/api/repair-guides', guideData);
  },

  updateGuide: async (id, guideData) => {
    return await apiClient.put(`/api/repair-guides/${id}`, guideData);
  },

  deleteGuide: async (id) => {
    return await apiClient.delete(`/api/repair-guides/${id}`);
  }
};
