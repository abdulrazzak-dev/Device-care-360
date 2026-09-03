import apiClient from './apiClient';

export const userService = {
  getProfile: async () => {
    return await apiClient.get('/api/users/me');
  },

  getUserById: async (id) => {
    return await apiClient.get(`/api/users/${id}`);
  },

  updateProfile: async (id, profileData) => {
    return await apiClient.put(`/api/users/${id}`, profileData);
  },

  deleteUser: async (id) => {
    return await apiClient.delete(`/api/users/${id}`);
  }
};
