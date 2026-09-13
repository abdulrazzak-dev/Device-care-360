import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { deviceService } from '../../services/deviceService';
import { bookingService } from '../../services/bookingService';
import { troubleshootingService } from '../../services/troubleshootingService';
import StatusBadge from '../../components/common/StatusBadge';
import Loader from '../../components/common/Loader';
import { Smartphone, Cpu, Calendar, ShieldAlert, PlusCircle, ArrowRight } from 'lucide-react';

const DashboardPage = () => {
  const { user } = useAuth();
  const [devices, setDevices] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      if (!user?.userId) return;
      try {
        const [devRes, bookRes, histRes] = await Promise.all([
          deviceService.getUserDevices(user.userId).catch(() => ({ data: [] })),
          bookingService.getByUserId(user.userId).catch(() => ({ data: [] })),
          troubleshootingService.getHistory(user.userId).catch(() => ({ data: [] }))
        ]);
        setDevices(devRes.data || []);
        setBookings(bookRes.data || []);
        setHistory(histRes.data || []);
      } finally {
        setLoading(false);
      }
    };
    loadDashboardData();
  }, [user]);

  if (loading) return <DashboardLayout><Loader text="Loading your dashboard..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-3 mb-4">
        <div>
          <h3 className="fw-bold mb-1 text-break">Welcome back, {user?.username}!</h3>
          <p className="text-muted small mb-0">Overview of your registered devices, AI sessions, and repair appointments.</p>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          <Link to="/diagnose" className="btn btn-primary btn-sm d-flex align-items-center gap-2">
            <Cpu size={16} /> AI Diagnose
          </Link>
          <Link to="/devices" className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2">
            <PlusCircle size={16} /> Add Device
          </Link>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-primary h-100">
            <div className="text-muted small fw-semibold text-truncate">Registered Devices</div>
            <div className="fs-3 fw-bold mt-1">{devices.length}</div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-info h-100">
            <div className="text-muted small fw-semibold text-truncate">Active Bookings</div>
            <div className="fs-3 fw-bold mt-1">{bookings.filter(b => b.status !== 'COMPLETED' && b.status !== 'CANCELLED').length}</div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-success h-100">
            <div className="text-muted small fw-semibold text-truncate">Completed Repairs</div>
            <div className="fs-3 fw-bold mt-1">{bookings.filter(b => b.status === 'COMPLETED').length}</div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="card p-3 border-0 shadow-sm border-start border-4 border-warning h-100">
            <div className="text-muted small fw-semibold text-truncate">AI Diagnoses</div>
            <div className="fs-3 fw-bold mt-1">{history.length}</div>
          </div>
        </div>
      </div>

      <div className="row g-4">
        {/* Recent Appointments */}
        <div className="col-lg-7">
          <div className="card p-4 border-0 shadow-sm">
            <div className="d-flex align-items-center justify-content-between mb-3">
              <h5 className="fw-bold mb-0">Upcoming Appointments</h5>
              <Link to="/bookings" className="small text-primary fw-semibold">View All</Link>
            </div>

            {bookings.length === 0 ? (
              <p className="text-muted small my-3">No active bookings. Start AI Diagnosis if your device needs repair.</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead>
                    <tr className="text-muted small">
                      <th>Date</th>
                      <th>Issue</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bookings.slice(0, 5).map((b) => (
                      <tr key={b.id}>
                        <td className="small">{new Date(b.appointmentTime).toLocaleDateString()}</td>
                        <td className="fw-semibold small">{b.issueDescription}</td>
                        <td><StatusBadge status={b.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* My Devices List Preview */}
        <div className="col-lg-5">
          <div className="card p-4 border-0 shadow-sm">
            <div className="d-flex align-items-center justify-content-between mb-3">
              <h5 className="fw-bold mb-0">My Devices</h5>
              <Link to="/devices" className="small text-primary fw-semibold">Manage</Link>
            </div>

            {devices.length === 0 ? (
              <div className="text-center py-4">
                <Smartphone size={32} className="text-muted mb-2" />
                <p className="text-muted small">No registered devices yet.</p>
              </div>
            ) : (
              <div className="d-flex flex-column gap-2">
                {devices.slice(0, 4).map((dev) => (
                  <div className="p-3 bg-light rounded-3 d-flex align-items-center justify-content-between" key={dev.id}>
                    <div>
                      <div className="fw-bold small">{dev.brand} {dev.model}</div>
                      <div className="text-muted extra-small">{dev.category}</div>
                    </div>
                    <Link to="/diagnose" className="btn btn-sm btn-outline-primary py-1 px-2">Diagnose</Link>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default DashboardPage;
