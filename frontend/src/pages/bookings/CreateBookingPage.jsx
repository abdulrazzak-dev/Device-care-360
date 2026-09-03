import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { bookingService } from '../../services/bookingService';
import { technicianService } from '../../services/technicianService';
import { deviceService } from '../../services/deviceService';
import { useAuth } from '../../context/AuthContext';
import { Calendar, Clock, DollarSign, Wrench } from 'lucide-react';

const CreateBookingPage = () => {
  const [searchParams] = useSearchParams();
  const techIdParam = searchParams.get('techId') || '';
  const { user } = useAuth();
  const navigate = useNavigate();

  const [technicians, setTechnicians] = useState([]);
  const [userDevices, setUserDevices] = useState([]);
  const [selectedTech, setSelectedTech] = useState(techIdParam);
  const [selectedDevice, setSelectedDevice] = useState('');
  const [issueDescription, setIssueDescription] = useState('');
  const [appointmentDate, setAppointmentDate] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    technicianService.getAll().then(res => setTechnicians(res.data || [])).catch(() => {});
    if (user?.userId) {
      deviceService.getUserDevices(user.userId).then(res => setUserDevices(res.data || [])).catch(() => {});
    }
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await bookingService.create({
        technicianId: selectedTech,
        deviceId: selectedDevice,
        issueDescription: issueDescription,
        appointmentTime: appointmentDate,
        estimatedCost: 75.00
      });
      navigate(`/bookings`);
    } catch (err) {
      alert('Failed to create booking: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-lg mx-auto card p-4 border-0 shadow-sm rounded-4">
        <h4 className="fw-bold mb-3 d-flex align-items-center gap-2">
          <Calendar className="text-primary" size={24} /> Book Repair Appointment
        </h4>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label small fw-semibold">Select Technician</label>
            <select className="form-select" value={selectedTech} onChange={e => setSelectedTech(e.target.value)} required>
              <option value="">-- Choose Certified Technician --</option>
              {technicians.map(t => (
                <option key={t.id} value={t.id}>{t.fullName} ({t.specializations?.join(', ')})</option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label small fw-semibold">Select Device</label>
            <select className="form-select" value={selectedDevice} onChange={e => setSelectedDevice(e.target.value)}>
              <option value="">-- Choose Registered Device (Optional) --</option>
              {userDevices.map(d => (
                <option key={d.id} value={d.id}>{d.brand} {d.model} ({d.category})</option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label small fw-semibold">Appointment Date & Time</label>
            <input type="datetime-local" className="form-control" value={appointmentDate} onChange={e => setAppointmentDate(e.target.value)} required />
          </div>

          <div className="mb-4">
            <label className="form-label small fw-semibold">Issue / Special Instructions</label>
            <textarea className="form-control" rows={3} placeholder="Describe problem details..." value={issueDescription} onChange={e => setIssueDescription(e.target.value)} required></textarea>
          </div>

          <div className="p-3 bg-light rounded-3 mb-4 d-flex align-items-center justify-content-between">
            <span className="small fw-semibold text-muted">Estimated Diagnosis Base Cost:</span>
            <span className="fs-5 fw-bold text-success">$75.00</span>
          </div>

          <button type="submit" className="btn btn-primary w-100 py-2.5 fw-bold" disabled={loading}>
            {loading ? 'Scheduling...' : 'Confirm Appointment'}
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
};

export default CreateBookingPage;
