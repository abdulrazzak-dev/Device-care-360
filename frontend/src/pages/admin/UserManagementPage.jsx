import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Loader from '../../components/common/Loader';
import StatusBadge from '../../components/common/StatusBadge';
import { Users } from 'lucide-react';

const UserManagementPage = () => {
  const [users, setUsers] = useState([
    { id: 'usr-1', username: 'alex_dev', email: 'alex@example.com', role: 'USER', status: 'ACTIVE' },
    { id: 'usr-2', username: 'sarah_tech', email: 'sarah@tech.com', role: 'TECHNICIAN', status: 'ACTIVE' },
    { id: 'usr-3', username: 'admin_master', email: 'admin@devicecare.com', role: 'ADMIN', status: 'ACTIVE' }
  ]);

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">User Account Management</h3>
        <p className="text-muted small mb-0">Overview of registered accounts across the platform.</p>
      </div>

      <div className="card p-4 border-0 shadow-sm">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr className="text-muted small">
                <th>User ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td className="small text-muted">{u.id}</td>
                  <td className="fw-semibold small">{u.username}</td>
                  <td className="small">{u.email}</td>
                  <td><span className="badge bg-secondary">{u.role}</span></td>
                  <td><StatusBadge status={u.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default UserManagementPage;
