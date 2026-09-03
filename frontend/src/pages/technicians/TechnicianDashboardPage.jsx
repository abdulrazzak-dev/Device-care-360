import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { technicianService } from '../../services/technicianService';
import { bookingService } from '../../services/bookingService';
import StatusBadge from '../../components/common/StatusBadge';
import Loader from '../../components/common/Loader';
import { Wrench, Calendar, Star, CheckCircle, ToggleLeft, ToggleRight } from 'lucide-react';

const TechnicianDashboardPage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadTechData = async () => {
    if (!user?.userId) return;
    try {
      const [profRes, bookRes] = await Promise.all([
        technicianService.getById(user.userId).catch(() => ({ data: null })),
        bookingService.getByTechnicianId(user.userId).catch(() => ({ data: [] }))
      ]);
      setProfile(profRes.data);
      setBookings(bookRes.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTechData();
  }, [user]);

  const toggleAvailability = async () => {
    if (!profile) return;
    try {
      const newAvail = !profile.available;
      await technicianService.setAvailability(user.userId, newAvail);
      loadTechData();
    } catch (err) {
      alert('Failed to update availability: ' + err.message);
    }
  };

  const handleUpdateBookingStatus = async (bookingId, status) => {
    try {
      await bookingService.updateStatus(bookingId, status, 'Technician updated status');
      loadTechData();
    } catch (err) {
      alert('Failed to update status: ' + err.message);
    }
  };

  if (loading) return <DashboardLayout><Loader text="Loading technician workspace..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h3 className="fw-bold mb-1">Technician Workspace</h3>
          <p className="text-muted small mb-0">Manage service availability, incoming repair requests, and client ratings.</p>
        </div>
        {profile && (
          <button className={`btn btn-sm ${profile.available ? 'btn-success' : 'btn-outline-secondary'} d-flex align-items-center gap-2`} onClick={toggleAvailability}>
            {profile.available ? <ToggleRight size={20} /> : <ToggleLeft size={20} />}
            <span>{profile.available ? 'Available for Jobs' : 'Offline / Unavailable'}</span>
          </button>
        )}
      </div>

      {/* Stats Cards */}
      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-primary">
            <div className="text-muted small fw-semibold">Verification Status</div>
            <div className="fs-4 fw-bold mt-1 text-uppercase text-primary">{profile?.verificationStatus || 'PENDING'}</div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-warning">
            <div className="text-muted small fw-semibold">Rating Score</div>
            <div className="fs-4 fw-bold mt-1 text-warning">★ {profile?.averageRating || 5.0} ({profile?.totalReviews || 0} Reviews)</div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-success">
            <div className="text-muted small fw-semibold">Assigned Jobs</div>
            <div className="fs-4 fw-bold mt-1">{bookings.length}</div>
          </div>
        </div>
      </div>

      {/* Assigned Bookings Table */}
      <div className="card border-0 shadow-sm p-4">
        <h5 className="fw-bold mb-3">Assigned Client Repair Jobs</h5>
        {bookings.length === 0 ? (
          <p className="text-muted small">No repair jobs assigned currently.</p>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead>
                <tr className="text-muted small">
                  <th>Date</th>
                  <th>Client Issue</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map(b => (
                  <tr key={b.id}>
                    <td className="small">{new Date(b.appointmentTime).toLocaleString()}</td>
                    <td className="small fw-semibold">{b.issueDescription}</td>
                    <td><StatusBadge status={b.status} /></td>
                    <td>
                      <div className="d-flex gap-1">
                        {b.status === 'PENDING' && (
                          <button className="btn btn-sm btn-primary py-1 px-2" onClick={() => handleUpdateBookingStatus(b.id, 'CONFIRMED')}>Confirm</button>
                        )}
                        {b.status === 'CONFIRMED' && (
                          <button className="btn btn-sm btn-info text-white py-1 px-2" onClick={() => handleUpdateBookingStatus(b.id, 'IN_PROGRESS')}>Start Work</button>
                        )}
                        {b.status === 'IN_PROGRESS' && (
                          <button className="btn btn-sm btn-success py-1 px-2" onClick={() => handleUpdateBookingStatus(b.id, 'COMPLETED')}>Mark Complete</button>
                        )}
                      </div>
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

export default TechnicianDashboardPage;
