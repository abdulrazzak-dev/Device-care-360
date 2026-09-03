import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { bookingService } from '../../services/bookingService';
import { useAuth } from '../../context/AuthContext';
import Loader from '../../components/common/Loader';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import { Calendar, CreditCard, XCircle, CheckCircle } from 'lucide-react';
import { Link } from 'react-router-dom';

const MyBookingsPage = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchBookings = async () => {
    if (!user?.userId) return;
    try {
      const res = await bookingService.getByUserId(user.userId);
      setBookings(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, [user]);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this booking?')) return;
    try {
      await bookingService.updateStatus(id, 'CANCELLED', 'User requested cancellation');
      fetchBookings();
    } catch (err) {
      alert('Failed to cancel booking: ' + err.message);
    }
  };

  if (loading) return <DashboardLayout><Loader text="Loading appointments..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h3 className="fw-bold mb-1">My Appointments & Bookings</h3>
          <p className="text-muted small mb-0">Track technician visits, service statuses, and payments.</p>
        </div>
        <Link to="/bookings/new" className="btn btn-primary btn-sm fw-bold">+ New Booking</Link>
      </div>

      {bookings.length === 0 ? (
        <EmptyState title="No Bookings Found" message="You have no active repair bookings." />
      ) : (
        <div className="card border-0 shadow-sm p-4">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead>
                <tr className="text-muted small">
                  <th>ID</th>
                  <th>Appointment Time</th>
                  <th>Issue</th>
                  <th>Est. Cost</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id}>
                    <td className="small fw-semibold">{b.id?.substring(0, 8)}</td>
                    <td className="small">{new Date(b.appointmentTime).toLocaleString()}</td>
                    <td className="small">{b.issueDescription}</td>
                    <td className="small fw-bold text-success">${b.estimatedCost || '75.00'}</td>
                    <td><StatusBadge status={b.status} /></td>
                    <td>
                      <div className="d-flex gap-2">
                        {b.status === 'PENDING' || b.status === 'CONFIRMED' ? (
                          <>
                            <Link to={`/payments/${b.id}?amount=${b.estimatedCost || 75.00}`} className="btn btn-sm btn-success d-flex align-items-center gap-1">
                              <CreditCard size={14} /> Pay
                            </Link>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => handleCancel(b.id)}>
                              Cancel
                            </button>
                          </>
                        ) : (
                          <span className="text-muted small">N/A</span>
                        )}
                      </div>
                    </td>
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

export default MyBookingsPage;
