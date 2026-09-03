import apiClient from './apiClient';

export const notificationService = {
  getNotifications: async () => {
    return await apiClient.get('/api/notifications');
  },

  markAsRead: async (id) => {
    return await apiClient.patch(`/api/notifications/${id}/read`);
  }
};
