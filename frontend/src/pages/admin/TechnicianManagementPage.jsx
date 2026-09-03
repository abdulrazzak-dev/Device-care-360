import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { technicianService } from '../../services/technicianService';
import { adminService } from '../../services/adminService';
import Loader from '../../components/common/Loader';
import StatusBadge from '../../components/common/StatusBadge';
import { ShieldCheck, Check, X } from 'lucide-react';

const TechnicianManagementPage = () => {
  const [techs, setTechs] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadTechs = async () => {
    try {
      const res = await technicianService.getAll();
      setTechs(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTechs();
  }, []);

  const handleVerify = async (id, status) => {
    try {
      await adminService.verifyTechnician(id, status);
      loadTechs();
    } catch (err) {
      alert('Verification failed: ' + err.message);
    }
  };

  if (loading) return <DashboardLayout><Loader text="Loading technicians..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">Technician Verification & Governance</h3>
        <p className="text-muted small mb-0">Approve or reject registered service technicians.</p>
      </div>

      <div className="card p-4 border-0 shadow-sm">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr className="text-muted small">
                <th>Name</th>
                <th>Email</th>
                <th>Specializations</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {techs.map((t) => (
                <tr key={t.id}>
                  <td className="fw-semibold small">{t.fullName}</td>
                  <td className="small">{t.email}</td>
                  <td className="small">{t.specializations?.join(', ') || 'General'}</td>
                  <td><StatusBadge status={t.verificationStatus} /></td>
                  <td>
                    <div className="d-flex gap-1">
                      <button className="btn btn-sm btn-success py-1 px-2 d-flex align-items-center gap-1" onClick={() => handleVerify(t.id, 'VERIFIED')}>
                        <Check size={14} /> Approve
                      </button>
                      <button className="btn btn-sm btn-outline-danger py-1 px-2 d-flex align-items-center gap-1" onClick={() => handleVerify(t.id, 'REJECTED')}>
                        <X size={14} /> Reject
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default TechnicianManagementPage;
