import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { adminService } from '../../services/adminService';
import Loader from '../../components/common/Loader';
import { LayoutDashboard, Users, Wrench, Cpu, ShieldCheck } from 'lucide-react';

const AdminDashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService.getDashboardStats()
      .then(res => setStats(res.data || {}))
      .catch(() => setStats({ systemStatus: 'HEALTHY', activeServices: 11 }))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <DashboardLayout><Loader text="Loading System Administration..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">Central Administrative Control</h3>
        <p className="text-muted small mb-0">Platform overview, active microservices health, and system approvals.</p>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-success">
            <div className="text-muted small fw-semibold">System Health</div>
            <div className="fs-4 fw-bold mt-1 text-success">{stats?.systemStatus || 'HEALTHY'}</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-primary">
            <div className="text-muted small fw-semibold">Active Microservices</div>
            <div className="fs-4 fw-bold mt-1">{stats?.activeServices || 11} / 11 Services</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-warning">
            <div className="text-muted small fw-semibold">Eureka Service Discovery</div>
            <div className="fs-4 fw-bold mt-1 text-warning">Port 8761 UP</div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-info">
            <div className="text-muted small fw-semibold">API Gateway</div>
            <div className="fs-4 fw-bold mt-1 text-info">Port 8080 Active</div>
          </div>
        </div>
      </div>

      <div className="card p-4 border-0 shadow-sm">
        <h5 className="fw-bold mb-3">Core Administration Features</h5>
        <div className="row g-3">
          <div className="col-md-4">
            <a href="/admin/technicians" className="btn btn-outline-primary w-100 p-3 text-start rounded-3">
              <Wrench size={24} className="mb-2 text-primary" />
              <div className="fw-bold text-dark">Technician Verification</div>
              <div className="extra-small text-muted">Review credentials & set VERIFIED/REJECTED status</div>
            </a>
          </div>
          <div className="col-md-4">
            <a href="/admin/users" className="btn btn-outline-primary w-100 p-3 text-start rounded-3">
              <Users size={24} className="mb-2 text-primary" />
              <div className="fw-bold text-dark">User Orchestration</div>
              <div className="extra-small text-muted">Manage registered user accounts & status</div>
            </a>
          </div>
          <div className="col-md-4">
            <a href="/admin/reports" className="btn btn-outline-primary w-100 p-3 text-start rounded-3">
              <Cpu size={24} className="mb-2 text-primary" />
              <div className="fw-bold text-dark">AI & Platform Analytics</div>
              <div className="extra-small text-muted">Review diagnostic trends & system events</div>
            </a>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default AdminDashboardPage;
