import apiClient from './apiClient';

export const technicianService = {
  getAll: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return await apiClient.get(`/api/technicians${query ? `?${query}` : ''}`);
  },

  getById: async (id) => {
    return await apiClient.get(`/api/technicians/${id}`);
  },

  getMyProfile: async () => {
    return await apiClient.get('/api/technicians/me');
  },

  updateMyProfile: async (profileData) => {
    return await apiClient.put('/api/technicians/me', profileData);
  },

  uploadCertificate: async (file, onUploadProgress) => {
    const formData = new FormData();
    formData.append('file', file);

    return await apiClient.post('/api/technicians/me/certificates', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    });
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

  updateStatus: async (id, status, reason = '') => {
    return await apiClient.put(`/api/technicians/${id}/status`, { status, reason });
  },

  setAvailability: async (id, available) => {
    return await apiClient.put(`/api/technicians/${id}/availability?available=${available}`);
  }
};

export default technicianService;
