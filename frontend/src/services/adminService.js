import apiClient from './apiClient';

export const adminService = {
  getDashboardStats: async () => {
    return await apiClient.get('/api/admin/dashboard');
  },

  verifyTechnician: async (id, status) => {
    return await apiClient.put(`/api/admin/technicians/${id}/verify?status=${status}`);
  }
};
