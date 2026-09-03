import apiClient from './apiClient';

export const paymentService = {
  processPayment: async (paymentData) => {
    return await apiClient.post('/api/payments', paymentData);
  },

  getById: async (id) => {
    return await apiClient.get(`/api/payments/${id}`);
  },

  getByUserId: async (userId) => {
    return await apiClient.get(`/api/payments/user/${userId}`);
  },

  getByBookingId: async (bookingId) => {
    return await apiClient.get(`/api/payments/booking/${bookingId}`);
  }
};
