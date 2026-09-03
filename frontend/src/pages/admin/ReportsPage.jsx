import React from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { BarChart2, Cpu, ShieldAlert, CheckCircle } from 'lucide-react';

const ReportsPage = () => {
  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">Analytics & System Reports</h3>
        <p className="text-muted small mb-0">Platform metrics, safety triggers, and diagnostic trends.</p>
      </div>

      <div className="row g-4 mb-4">
        <div className="col-md-4">
          <div className="card p-4 border-0 shadow-sm">
            <h6 className="fw-bold text-dark mb-3">Diagnostic Request Volume</h6>
            <div className="display-6 fw-bold text-primary">1,248</div>
            <p className="small text-muted mb-0">Total AI troubleshooting queries processed</p>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card p-4 border-0 shadow-sm">
            <h6 className="fw-bold text-dark mb-3">Safety Engine Overrides</h6>
            <div className="display-6 fw-bold text-danger">184</div>
            <p className="small text-muted mb-0">Critical electrical/battery hazards detected</p>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card p-4 border-0 shadow-sm">
            <h6 className="fw-bold text-dark mb-3">Completed Appointments</h6>
            <div className="display-6 fw-bold text-success">562</div>
            <p className="small text-muted mb-0">Verified technician repairs fulfilled</p>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ReportsPage;
