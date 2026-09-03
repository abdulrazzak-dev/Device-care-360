import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { bookingService } from '../../services/bookingService';
import { useAuth } from '../../context/AuthContext';
import Loader from '../../components/common/Loader';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import { FileText } from 'lucide-react';

const RepairHistoryPage = () => {
  const { user } = useAuth();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.userId) {
      bookingService.getByUserId(user.userId)
        .then(res => setHistory((res.data || []).filter(b => b.status === 'COMPLETED')))
        .finally(() => setLoading(false));
    }
  }, [user]);

  if (loading) return <DashboardLayout><Loader text="Loading repair history..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">Completed Repair History</h3>
        <p className="text-muted small mb-0">Archive of resolved device repairs and service receipts.</p>
      </div>

      {history.length === 0 ? (
        <EmptyState title="No Completed Repairs" message="Completed technician appointments will be archived here." />
      ) : (
        <div className="card p-4 border-0 shadow-sm">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead>
                <tr className="text-muted small">
                  <th>Repair ID</th>
                  <th>Date Completed</th>
                  <th>Issue Handled</th>
                  <th>Cost</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {history.map((h) => (
                  <tr key={h.id}>
                    <td className="small text-muted">{h.id?.substring(0, 8)}</td>
                    <td className="small">{new Date(h.updatedAt || h.createdAt).toLocaleDateString()}</td>
                    <td className="small fw-semibold">{h.issueDescription}</td>
                    <td className="small text-success fw-bold">${h.estimatedCost || '75.00'}</td>
                    <td><StatusBadge status={h.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
};

export default RepairHistoryPage;
