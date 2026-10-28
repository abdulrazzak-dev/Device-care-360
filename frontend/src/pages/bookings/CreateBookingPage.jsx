import React, { useState, useEffect, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { bookingService } from '../../services/bookingService';
import { technicianService } from '../../services/technicianService';
import { deviceService } from '../../services/deviceService';
import { useAuth } from '../../context/AuthContext';
import { Calendar, Clock, DollarSign, Wrench, AlertCircle, CheckCircle2, ShieldCheck } from 'lucide-react';
import { CANONICAL_SPECIALIZATIONS, normalizeToCanonical, matchesCategory, getCategoryDisplayLabel } from '../../utils/categoryConstants';

const CreateBookingPage = () => {
  const [searchParams] = useSearchParams();
  const techIdParam = searchParams.get('techId') || '';
  const categoryParam = searchParams.get('category') || '';
  const { user } = useAuth();
  const navigate = useNavigate();

  const [allTechnicians, setAllTechnicians] = useState([]);
  const [userDevices, setUserDevices] = useState([]);
  const [selectedTech, setSelectedTech] = useState(techIdParam);
  const [selectedDevice, setSelectedDevice] = useState('');
  const [manualCategory, setManualCategory] = useState(normalizeToCanonical(categoryParam) || '');
  const [issueDescription, setIssueDescription] = useState('');
  const [appointmentDate, setAppointmentDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Fetch approved technicians and user's registered devices
  useEffect(() => {
    const fetchData = async () => {
      setFetching(true);
      try {
        const [techRes, devRes] = await Promise.all([
          technicianService.getAll().catch(() => ({ data: [] })),
          user?.userId ? deviceService.getUserDevices(user.userId).catch(() => ({ data: [] })) : Promise.resolve({ data: [] })
        ]);
        setAllTechnicians(techRes.data || []);
        setUserDevices(devRes.data || []);
      } finally {
        setFetching(false);
      }
    };
    fetchData();
  }, [user]);

  // Determine active canonical category from selected device or manual selection
  const selectedDeviceObj = useMemo(() => {
    return userDevices.find(d => d.id === selectedDevice) || null;
  }, [userDevices, selectedDevice]);

  const activeCategory = useMemo(() => {
    if (selectedDeviceObj && selectedDeviceObj.category) {
      return normalizeToCanonical(selectedDeviceObj.category) || selectedDeviceObj.category;
    }
    return manualCategory;
  }, [selectedDeviceObj, manualCategory]);

  // Filter technicians based on verification status, availability, and category match
  const eligibleTechnicians = useMemo(() => {
    return allTechnicians.filter(tech => {
      // 1. Strict approval check
      const status = (tech.verificationStatus || '').toUpperCase();
      const isApproved = status === 'APPROVED' || status === 'VERIFIED';
      if (!isApproved) return false;

      // 2. Availability check
      const isAvail = tech.available !== undefined ? tech.available : tech.isAvailable !== undefined ? tech.isAvailable : true;
      if (!isAvail) return false;

      // 3. Category match check (if category is selected)
      if (activeCategory) {
        const specs = tech.specializations || tech.categories || [];
        return matchesCategory(specs, activeCategory);
      }

      return true;
    });
  }, [allTechnicians, activeCategory]);

  // Reset selected technician if the active category changes and the selected tech does not support it
  useEffect(() => {
    if (selectedTech && activeCategory) {
      const currentTech = allTechnicians.find(t => t.id === selectedTech);
      if (currentTech) {
        const specs = currentTech.specializations || currentTech.categories || [];
        const isEligible = matchesCategory(specs, activeCategory);
        if (!isEligible) {
          setSelectedTech('');
        }
      }
    }
  }, [activeCategory, selectedTech, allTechnicians]);

  const selectedTechObj = useMemo(() => {
    return allTechnicians.find(t => t.id === selectedTech) || null;
  }, [allTechnicians, selectedTech]);

  const estimatedCost = useMemo(() => {
    if (selectedTechObj && selectedTechObj.hourlyRate) {
      return Number(selectedTechObj.hourlyRate);
    }
    return 2500;
  }, [selectedTechObj]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (!selectedTech) {
      setErrorMsg('Please select a certified technician from the eligible list.');
      return;
    }

    if (!appointmentDate) {
      setErrorMsg('Please specify an appointment date and time.');
      return;
    }

    const appt = new Date(appointmentDate);
    if (isNaN(appt.getTime()) || appt <= new Date()) {
      setErrorMsg('Appointment date and time must be scheduled in the future.');
      return;
    }

    setLoading(true);
    try {
      await bookingService.create({
        technicianId: selectedTech,
        deviceId: selectedDevice || null,
        deviceModel: selectedDeviceObj ? `${selectedDeviceObj.brand} ${selectedDeviceObj.model}` : 'Unregistered Device',
        deviceCategory: activeCategory || (selectedDeviceObj ? selectedDeviceObj.category : 'General'),
        issueDescription: issueDescription.trim(),
        appointmentTime: appointmentDate,
        estimatedCost: estimatedCost
      });
      navigate('/bookings');
    } catch (err) {
      setErrorMsg(err.message || 'Failed to create booking. Please check details and try again.');
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

        {errorMsg && (
          <div className="alert alert-danger small p-3 mb-3 rounded-3 d-flex align-items-start gap-2">
            <AlertCircle size={18} className="flex-shrink-0 mt-0.5" />
            <div>{errorMsg}</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Step 1: Device Selection */}
          <div className="mb-3">
            <label className="form-label small fw-semibold">Select Device</label>
            <select 
              className="form-select" 
              value={selectedDevice} 
              onChange={e => {
                setSelectedDevice(e.target.value);
                if (e.target.value) setManualCategory('');
              }}
            >
              <option value="">-- Choose Registered Device (Optional) --</option>
              {userDevices.map(d => (
                <option key={d.id} value={d.id}>
                  {d.brand} {d.model} ({d.category})
                </option>
              ))}
            </select>
          </div>

          {/* Fallback Category Selection if no registered device is selected */}
          {!selectedDevice && (
            <div className="mb-3">
              <label className="form-label small fw-semibold">
                Device Category <span className="text-muted fw-normal">(filter technicians by specialty)</span>
              </label>
              <select
                className="form-select"
                value={manualCategory}
                onChange={e => setManualCategory(e.target.value)}
              >
                <option value="">-- All Categories (Browse All Available) --</option>
                {CANONICAL_SPECIALIZATIONS.map(spec => (
                  <option key={spec.key} value={spec.key}>
                    {spec.label}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Active Category Indicator Badge */}
          {activeCategory && (
            <div className="mb-3 p-2.5 bg-light rounded-3 border d-flex align-items-center justify-content-between">
              <span className="small text-muted">Filtering technicians specialized in:</span>
              <span className="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 px-2.5 py-1">
                {getCategoryDisplayLabel(activeCategory)}
              </span>
            </div>
          )}

          {/* Step 2: Technician Selection */}
          <div className="mb-3">
            <label className="form-label small fw-semibold">Select Certified Technician</label>
            <select 
              className="form-select" 
              value={selectedTech} 
              onChange={e => setSelectedTech(e.target.value)} 
              required
              disabled={fetching || eligibleTechnicians.length === 0}
            >
              <option value="">
                {fetching
                  ? 'Loading certified technicians...'
                  : eligibleTechnicians.length === 0
                  ? `-- No eligible technicians for ${getCategoryDisplayLabel(activeCategory)} --`
                  : '-- Choose Approved Technician --'}
              </option>
              {eligibleTechnicians.map(t => (
                <option key={t.id} value={t.id}>
                  {t.fullName} — LKR {Number(t.hourlyRate || 2500).toLocaleString()} | {t.city || t.serviceArea || 'General'} | ★ {t.averageRating || 5.0} ({t.yearsOfExperience || 1} yrs exp)
                </option>
              ))}
            </select>

            {/* Empty state alert when no eligible technicians are found */}
            {!fetching && activeCategory && eligibleTechnicians.length === 0 && (
              <div className="alert alert-warning small p-2.5 mt-2 rounded-2 d-flex align-items-center gap-2">
                <AlertCircle size={16} className="text-warning flex-shrink-0" />
                <span>
                  No certified technicians are currently available for <strong>{getCategoryDisplayLabel(activeCategory)}</strong>. Try selecting a different category or check back later.
                </span>
              </div>
            )}
          </div>

          {/* Step 3: Appointment Date & Time */}
          <div className="mb-3">
            <label className="form-label small fw-semibold">Appointment Date & Time</label>
            <input 
              type="datetime-local" 
              className="form-control" 
              value={appointmentDate} 
              onChange={e => setAppointmentDate(e.target.value)} 
              required 
            />
          </div>

          {/* Step 4: Issue Description */}
          <div className="mb-4">
            <label className="form-label small fw-semibold">Issue / Special Instructions</label>
            <textarea 
              className="form-control" 
              rows={3} 
              placeholder="Describe problem details, symptoms, error codes..." 
              value={issueDescription} 
              onChange={e => setIssueDescription(e.target.value)} 
              required
            />
          </div>

          {/* Cost Summary */}
          <div className="p-3 bg-light rounded-3 mb-4 d-flex align-items-center justify-content-between border">
            <div>
              <span className="small fw-semibold text-muted d-block">Estimated Diagnosis Base Cost:</span>
              <span className="extra-small text-muted">Final repair costs determined following inspection</span>
            </div>
            <span className="fs-5 fw-bold text-success">
              LKR {estimatedCost.toLocaleString()}
            </span>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary w-100 py-2.5 fw-bold shadow-sm" 
            disabled={loading || fetching || eligibleTechnicians.length === 0}
          >
            {loading ? 'Scheduling Appointment...' : 'Confirm Appointment'}
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
};

export default CreateBookingPage;
