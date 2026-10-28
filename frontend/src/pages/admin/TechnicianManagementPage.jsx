import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { technicianService } from '../../services/technicianService';
import { adminService } from '../../services/adminService';
import Loader from '../../components/common/Loader';
import StatusBadge from '../../components/common/StatusBadge';
import { ShieldCheck, Check, X, Ban, FileText, ExternalLink, Filter } from 'lucide-react';
import { getCategoryDisplayLabel } from '../../utils/categoryConstants';

const TechnicianManagementPage = () => {
  const [techs, setTechs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [rejectingTechId, setRejectingTechId] = useState(null);
  const [rejectionReason, setRejectionReason] = useState('');

  const loadTechs = async () => {
    setLoading(true);
    try {
      const params = statusFilter ? { status: statusFilter } : {};
      const res = await technicianService.search(params);
      setTechs(res.data || []);
    } catch {
      // Fallback
      const res = await technicianService.getAll().catch(() => ({ data: [] }));
      setTechs(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTechs();
  }, [statusFilter]);

  const handleVerify = async (id, status, reason = '') => {
    try {
      await adminService.verifyTechnician(id, status, reason);
      setRejectingTechId(null);
      setRejectionReason('');
      await loadTechs();
    } catch (err) {
      alert('Verification status update failed: ' + err.message);
    }
  };

  return (
    <DashboardLayout>
      <div className="d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-3 mb-4">
        <div>
          <h3 className="fw-bold mb-1 d-flex align-items-center gap-2">
            <ShieldCheck className="text-primary" size={26} />
            <span>Technician Verification & Governance</span>
          </h3>
          <p className="text-muted small mb-0">
            Review uploaded certification credentials, evaluate qualifications, and approve or reject service technicians.
          </p>
        </div>

        {/* Status Filter */}
        <div className="d-flex align-items-center gap-2">
          <Filter size={16} className="text-muted" />
          <select 
            className="form-select form-select-sm shadow-sm"
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            style={{ minWidth: '160px' }}
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending Review</option>
            <option value="APPROVED">Approved / Verified</option>
            <option value="REJECTED">Rejected</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </div>
      </div>

      <div className="card p-4 border-0 shadow-sm rounded-4">
        {loading ? (
          <Loader text="Loading technician records..." />
        ) : techs.length === 0 ? (
          <div className="text-center py-5 text-muted small">
            No technicians found matching the selected filter.
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead>
                <tr className="text-muted small">
                  <th>Technician</th>
                  <th>Contact</th>
                  <th>Specializations</th>
                  <th>Rate / Exp</th>
                  <th>Credentials</th>
                  <th>Status</th>
                  <th>Governance Actions</th>
                </tr>
              </thead>
              <tbody>
                {techs.map((t) => (
                  <tr key={t.id}>
                    <td>
                      <div className="fw-semibold small text-dark">{t.fullName}</div>
                      <div className="text-muted extra-small">{t.city || t.serviceArea || 'Location not specified'}</div>
                    </td>
                    <td className="small">
                      <div>{t.email}</div>
                      <div className="text-muted extra-small">{t.phone || 'No phone'}</div>
                    </td>
                    <td className="small" style={{ maxWidth: '220px' }}>
                      <div className="d-flex flex-wrap gap-1">
                        {(t.specializations || t.categories || []).map((s, idx) => (
                          <span key={idx} className="badge bg-light text-dark border extra-small">
                            {getCategoryDisplayLabel(s)}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="small">
                      <div className="fw-semibold text-success">LKR {Number(t.hourlyRate || 2500).toLocaleString()}</div>
                      <div className="text-muted extra-small">{t.yearsOfExperience || t.experienceYears || 1} yrs exp</div>
                    </td>
                    <td className="small">
                      {t.certificateUrl ? (
                        <a 
                          href={t.certificateUrl} 
                          target="_blank" 
                          rel="noopener noreferrer" 
                          className="btn btn-sm btn-outline-primary py-1 px-2 d-inline-flex align-items-center gap-1 extra-small"
                        >
                          <FileText size={13} />
                          <span>View Doc</span>
                          <ExternalLink size={11} />
                        </a>
                      ) : (
                        <span className="text-muted extra-small fst-italic">None uploaded</span>
                      )}
                    </td>
                    <td>
                      <StatusBadge status={t.verificationStatus} />
                    </td>
                    <td>
                      <div className="d-flex flex-wrap gap-1">
                        {t.verificationStatus !== 'APPROVED' && t.verificationStatus !== 'VERIFIED' && (
                          <button 
                            className="btn btn-sm btn-success py-1 px-2 d-flex align-items-center gap-1"
                            onClick={() => handleVerify(t.id, 'APPROVED')}
                            title="Approve Technician"
                          >
                            <Check size={14} /> Approve
                          </button>
                        )}

                        {t.verificationStatus !== 'REJECTED' && (
                          <button 
                            className="btn btn-sm btn-outline-danger py-1 px-2 d-flex align-items-center gap-1"
                            onClick={() => setRejectingTechId(t.id)}
                            title="Reject Technician"
                          >
                            <X size={14} /> Reject
                          </button>
                        )}

                        {t.verificationStatus !== 'SUSPENDED' && (
                          <button 
                            className="btn btn-sm btn-outline-secondary py-1 px-2 d-flex align-items-center gap-1"
                            onClick={() => handleVerify(t.id, 'SUSPENDED')}
                            title="Suspend Technician"
                          >
                            <Ban size={14} /> Suspend
                          </button>
                        )}
                      </div>

                      {/* Inline Rejection Reason prompt */}
                      {rejectingTechId === t.id && (
                        <div className="mt-2 p-2 bg-light rounded border border-danger-subtle">
                          <label className="form-label extra-small fw-semibold text-danger mb-1">
                            Rejection Reason / Feedback:
                          </label>
                          <input 
                            type="text" 
                            className="form-control form-control-sm mb-1.5"
                            placeholder="e.g. Expired certification or blurry certificate document"
                            value={rejectionReason}
                            onChange={e => setRejectionReason(e.target.value)}
                          />
                          <div className="d-flex gap-1">
                            <button 
                              className="btn btn-sm btn-danger py-0.5 px-2 extra-small"
                              onClick={() => handleVerify(t.id, 'REJECTED', rejectionReason)}
                            >
                              Confirm Rejection
                            </button>
                            <button 
                              className="btn btn-sm btn-light py-0.5 px-2 extra-small"
                              onClick={() => { setRejectingTechId(null); setRejectionReason(''); }}
                            >
                              Cancel
                            </button>
                          </div>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TechnicianManagementPage;
