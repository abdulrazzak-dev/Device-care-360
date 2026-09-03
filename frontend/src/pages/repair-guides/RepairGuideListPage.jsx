import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { repairGuideService } from '../../services/repairGuideService';
import Loader from '../../components/common/Loader';
import EmptyState from '../../components/common/EmptyState';
import RiskBadge from '../../components/safety/RiskBadge';
import { FileText, Wrench, ShieldAlert } from 'lucide-react';

const RepairGuideListPage = () => {
  const [guides, setGuides] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    repairGuideService.getGuides().then(res => setGuides(res.data || [])).finally(() => setLoading(false));
  }, []);

  if (loading) return <DashboardLayout><Loader text="Loading official repair guides..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">Official Repair Guides & Manuals</h3>
        <p className="text-muted small mb-0">Admin-approved safety procedures and diagnostic manuals.</p>
      </div>

      {guides.length === 0 ? (
        <EmptyState title="No Repair Guides Available" message="Check back later for official documentation updates." />
      ) : (
        <div className="row g-4">
          {guides.map((g) => (
            <div className="col-md-6" key={g.id}>
              <div className="card p-4 border-0 shadow-sm card-hover h-100">
                <div className="d-flex align-items-center justify-content-between mb-2">
                  <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-1 rounded-pill">{g.category}</span>
                  <RiskBadge riskLevel={g.riskLevel} />
                </div>
                <h5 className="fw-bold mb-2">{g.title}</h5>
                <p className="text-muted small mb-3">Issue: {g.issue}</p>

                <div className="bg-light p-3 rounded-3 mb-3">
                  <h6 className="fw-bold small text-dark mb-2">Required Tools:</h6>
                  <div className="d-flex flex-wrap gap-1">
                    {g.toolsRequired?.map((t, idx) => <span key={idx} className="badge bg-white text-dark border">{t}</span>)}
                  </div>
                </div>

                <h6 className="fw-bold small text-dark mb-1">Safe Instructions:</h6>
                <ol className="ps-3 small text-muted">
                  {g.safeInstructions?.map((ins, idx) => <li key={idx}>{ins}</li>)}
                </ol>
              </div>
            </div>
          ))}
        </div>
      )}
    </DashboardLayout>
  );
};

export default RepairGuideListPage;
