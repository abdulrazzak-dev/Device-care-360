import apiClient from './apiClient';

export const technicianService = {
  getAll: async () => {
    return await apiClient.get('/api/technicians');
  },

  getById: async (id) => {
    return await apiClient.get(`/api/technicians/${id}`);
  },

  search: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return await apiClient.get(`/api/technicians/search${query ? `?${query}` : ''}`);
  },

  register: async (profileData) => {
    return await apiClient.post('/api/technicians/register', profileData);
  },

  update: async (id, profileData) => {
    return await apiClient.put(`/api/technicians/${id}`, profileData);
  },

  setAvailability: async (id, available) => {
    return await apiClient.put(`/api/technicians/${id}/availability?available=${available}`);
  }
};
