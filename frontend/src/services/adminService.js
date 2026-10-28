import apiClient from './apiClient';

export const adminService = {
  getDashboardStats: async () => {
    return await apiClient.get('/api/admin/dashboard');
  },

  verifyTechnician: async (id, status, reason = '') => {
    const params = new URLSearchParams({ status });
    if (reason) params.append('reason', reason);
    return await apiClient.put(`/api/admin/technicians/${id}/verify?${params.toString()}`);
  }
};

export default adminService;
