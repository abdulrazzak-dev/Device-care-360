import apiClient from './apiClient';

export const reviewService = {
  createReview: async (reviewData) => {
    return await apiClient.post('/api/reviews', reviewData);
  },

  getByTechnicianId: async (technicianId) => {
    return await apiClient.get(`/api/reviews/technician/${technicianId}`);
  },

  getByUserId: async (userId) => {
    return await apiClient.get(`/api/reviews/user/${userId}`);
  }
};
