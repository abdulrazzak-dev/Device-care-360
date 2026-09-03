import apiClient from './apiClient';

export const deviceService = {
  getCategories: async () => {
    return await apiClient.get('/api/devices/categories');
  },

  getBrands: async (category) => {
    return await apiClient.get(`/api/devices/brands?category=${encodeURIComponent(category)}`);
  },

  getCommonIssues: async (category) => {
    return await apiClient.get(`/api/devices/issues?category=${encodeURIComponent(category)}`);
  },

  registerDevice: async (deviceData) => {
    return await apiClient.post('/api/devices', deviceData);
  },

  getUserDevices: async (userId) => {
    return await apiClient.get(`/api/devices/user/${userId}`);
  },

  getDeviceById: async (id) => {
    return await apiClient.get(`/api/devices/${id}`);
  },

  updateDevice: async (id, deviceData) => {
    return await apiClient.put(`/api/devices/${id}`, deviceData);
  },

  deleteDevice: async (id) => {
    return await apiClient.delete(`/api/devices/${id}`);
  }
};
