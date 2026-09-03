import apiClient from './apiClient';

export const bookingService = {
  create: async (bookingData) => {
    return await apiClient.post('/api/bookings', bookingData);
  },

  getById: async (id) => {
    return await apiClient.get(`/api/bookings/${id}`);
  },

  getByUserId: async (userId) => {
    return await apiClient.get(`/api/bookings/user/${userId}`);
  },

  getByTechnicianId: async (technicianId) => {
    return await apiClient.get(`/api/bookings/technician/${technicianId}`);
  },

  updateStatus: async (id, status, reason = '') => {
    return await apiClient.patch(`/api/bookings/${id}/status?status=${status}&reason=${encodeURIComponent(reason)}`);
  },

  reschedule: async (id, newTime) => {
    return await apiClient.put(`/api/bookings/${id}/reschedule?newTime=${encodeURIComponent(newTime)}`);
  },

  deleteBooking: async (id) => {
    return await apiClient.delete(`/api/bookings/${id}`);
  }
};
