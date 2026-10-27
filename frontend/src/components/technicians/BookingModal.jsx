import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  X, Calendar, Clock, DollarSign, MapPin, Wrench, ShieldCheck, 
  AlertCircle, CheckCircle2, User, AlertTriangle, ArrowRight 
} from 'lucide-react';
import { bookingService } from '../../services/bookingService';
import { deviceService } from '../../services/deviceService';
import { useAuth } from '../../context/AuthContext';

const DEFAULT_CATEGORIES = [
  'Smartphone',
  'Laptop',
  'Audio/Video',
  'Home Appliance',
  'Television',
  'Other Electronics'
];

const BookingModal = ({ isOpen, onClose, technician, onBookingSuccess }) => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // Form State
  const [deviceCategory, setDeviceCategory] = useState('');
  const [deviceModel, setDeviceModel] = useState('');
  const [appointmentDate, setAppointmentDate] = useState('');
  const [appointmentTime, setAppointmentTime] = useState('10:00');
  const [issueDescription, setIssueDescription] = useState('');
  const [additionalNotes, setAdditionalNotes] = useState('');
  const [selectedDeviceId, setSelectedDeviceId] = useState('');

  // Auxiliary state
  const [categories, setCategories] = useState(DEFAULT_CATEGORIES);
  const [userDevices, setUserDevices] = useState([]);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [successData, setSuccessData] = useState(null);

  // Today's date string for min date picker constraint (YYYY-MM-DD)
  const todayStr = new Date().toISOString().split('T')[0];

  // Auto-select category based on technician specialization when technician changes
  useEffect(() => {
    if (technician) {
      const primarySpec = technician.specializations?.[0] || '';
      if (primarySpec.toLowerCase().includes('phone')) {
        setDeviceCategory('Smartphone');
      } else if (primarySpec.toLowerCase().includes('laptop') || primarySpec.toLowerCase().includes('pc')) {
        setDeviceCategory('Laptop');
      } else if (primarySpec.toLowerCase().includes('audio') || primarySpec.toLowerCase().includes('video')) {
        setDeviceCategory('Audio/Video');
      } else if (primarySpec.toLowerCase().includes('appliance') || primarySpec.toLowerCase().includes('refrigerator')) {
        setDeviceCategory('Home Appliance');
      } else {
        setDeviceCategory('Smartphone');
      }

      // Default appointment date to tomorrow
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      setAppointmentDate(tomorrow.toISOString().split('T')[0]);
    }
    setErrors({});
    setSubmitError(null);
    setSuccessData(null);
  }, [technician]);

  // Load user's registered devices if authenticated
  useEffect(() => {
    if (user?.userId) {
      deviceService.getUserDevices(user.userId)
        .then((res) => {
          const list = Array.isArray(res) ? res : Array.isArray(res?.data) ? res.data : [];
          setUserDevices(list);
        })
        .catch(() => {});
    }
  }, [user]);

  // Handle ESC key to dismiss modal
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && !submitting) {
        onClose();
      }
    };
    if (isOpen) {
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, submitting, onClose]);

  if (!isOpen || !technician) return null;

  // Format hourly rate in LKR
  const formatRate = (rate) => {
    const val = Number(rate) || 2500;
    return `LKR ${val.toLocaleString()}`;
  };

  const handleDeviceSelect = (e) => {
    const devId = e.target.value;
    setSelectedDeviceId(devId);
    if (devId) {
      const dev = userDevices.find((d) => d.id === devId);
      if (dev) {
        if (dev.category) setDeviceCategory(dev.category);
        setDeviceModel(`${dev.brand || ''} ${dev.model || ''}`.trim());
      }
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!deviceCategory) {
      newErrors.deviceCategory = 'Please select a device category.';
    }

    if (!deviceModel.trim()) {
      newErrors.deviceModel = 'Please specify device brand and model (e.g. Apple iPhone 13 Pro Max).';
    }

    if (!appointmentDate) {
      newErrors.appointmentDate = 'Please select an appointment date.';
    } else {
      const selected = new Date(`${appointmentDate}T${appointmentTime || '00:00'}`);
      if (selected <= new Date()) {
        newErrors.appointmentDate = 'Appointment date and time must be set in the future.';
      }
    }

    if (!appointmentTime) {
      newErrors.appointmentTime = 'Please select a preferred appointment time.';
    }

    if (!issueDescription.trim()) {
      newErrors.issueDescription = 'Please describe the problem or symptoms you are experiencing.';
    } else if (issueDescription.trim().length < 10) {
      newErrors.issueDescription = 'Please provide at least 10 characters describing the issue.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError(null);

    // Guard: Demo profile cannot create database booking
    if (technician.isDemo) {
      setSubmitError({
        type: 'demo',
        message: 'This is a sample demonstration profile. Real appointments can only be booked with live database technicians.'
      });
      return;
    }

    // Guard: Authentication required
    if (!isAuthenticated) {
      setSubmitError({
        type: 'auth',
        message: 'You must be signed in to confirm an appointment. Please sign in to your account.'
      });
      return;
    }

    if (!validate()) {
      return;
    }

    setSubmitting(true);

    try {
      // Build ISO 8601 appointment datetime: YYYY-MM-DDTHH:mm:ss
      const isoAppointmentTime = `${appointmentDate}T${appointmentTime.length === 5 ? `${appointmentTime}:00` : appointmentTime}`;

      // Assemble issue details including device and optional instructions
      const combinedDescription = additionalNotes.trim()
        ? `[${deviceModel.trim()}] ${issueDescription.trim()} (Notes: ${additionalNotes.trim()})`
        : `[${deviceModel.trim()}] ${issueDescription.trim()}`;

      const payload = {
        technicianId: technician.id,
        technicianName: technician.fullName,
        deviceId: selectedDeviceId || null,
        deviceModel: deviceModel.trim(),
        deviceCategory: deviceCategory,
        issueDescription: combinedDescription,
        appointmentTime: isoAppointmentTime,
        appointmentDate: isoAppointmentTime, // JSON alias supported
        estimatedCost: Number(technician.hourlyRate) || 2500.0
      };

      const res = await bookingService.create(payload);
      const createdBooking = res?.data || res;
      setSuccessData(createdBooking);

      if (onBookingSuccess) {
        onBookingSuccess(createdBooking);
      }
    } catch (err) {
      console.error('Booking submission error:', err);
      if (err.status === 401) {
        setSubmitError({
          type: 'auth',
          message: 'Your session has expired. Please sign in again to book an appointment.'
        });
      } else if (err.status === 503 || err.status === 502) {
        setSubmitError({
          type: 'network',
          message: 'The booking service is temporarily unavailable. Please try again in a few moments.'
        });
      } else {
        setSubmitError({
          type: 'general',
          message: err.message || 'Failed to submit appointment booking. Please check your details and try again.'
        });
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div 
      className="modal fade show d-block" 
      tabIndex="-1" 
      role="dialog" 
      aria-modal="true"
      style={{ backgroundColor: 'rgba(15, 23, 42, 0.65)', backdropFilter: 'blur(4px)', zIndex: 1055 }}
    >
      <div className="modal-dialog modal-dialog-centered modal-lg">
        <div className="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
          
          {/* Modal Header */}
          <div className="modal-header bg-dark text-white p-3 p-sm-4 border-0 position-relative">
            <div className="d-flex align-items-center gap-3">
              <div 
                className="rounded-circle overflow-hidden bg-white d-flex align-items-center justify-content-center flex-shrink-0 border border-2 border-primary"
                style={{ width: '56px', height: '56px' }}
              >
                {technician.profileImageUrl ? (
                  <img 
                    src={technician.profileImageUrl} 
                    alt={technician.fullName} 
                    className="w-100 h-100 object-fit-cover"
                    onError={(e) => {
                      e.target.style.display = 'none';
                      e.target.nextElementSibling.style.display = 'flex';
                    }}
                  />
                ) : null}
                <div 
                  className="w-100 h-100 d-flex align-items-center justify-content-center bg-primary text-white fw-bold fs-5"
                  style={{ display: technician.profileImageUrl ? 'none' : 'flex' }}
                >
                  {technician.fullName?.charAt(0) || 'T'}
                </div>
              </div>

              <div>
                <div className="d-flex align-items-center gap-2 flex-wrap">
                  <h5 className="modal-title fw-bold text-white mb-0">{technician.fullName}</h5>
                  {technician.verificationStatus === 'VERIFIED' && (
                    <span className="badge bg-success bg-opacity-25 text-success border border-success border-opacity-50 extra-small px-2 py-0.5 rounded-pill d-inline-flex align-items-center gap-1">
                      <ShieldCheck size={12} /> Verified Tech
                    </span>
                  )}
                  {technician.isDemo && (
                    <span className="badge bg-warning bg-opacity-25 text-warning border border-warning border-opacity-50 extra-small px-2 py-0.5 rounded-pill">
                      Sample Preview
                    </span>
                  )}
                </div>
                <div className="d-flex align-items-center gap-3 text-light opacity-75 small mt-1 flex-wrap">
                  <span className="d-flex align-items-center gap-1">
                    <Wrench size={13} /> {technician.specializations?.[0] || 'Electronics Technician'}
                  </span>
                  <span className="d-flex align-items-center gap-1">
                    <MapPin size={13} /> {technician.serviceAreas?.[0] || 'Colombo'}
                  </span>
                  <span className="d-flex align-items-center gap-1 fw-semibold text-warning">
                    <DollarSign size={13} /> {formatRate(technician.hourlyRate)}/hr
                  </span>
                </div>
              </div>
            </div>

            <button 
              type="button" 
              className="btn-close btn-close-white position-absolute top-0 end-0 m-3" 
              aria-label="Close modal"
              onClick={onClose}
              disabled={submitting}
            />
          </div>

          {/* Modal Body */}
          <div className="modal-body p-3 p-sm-4 bg-light">
            {successData ? (
              <div className="text-center py-4">
                <div className="d-inline-flex align-items-center justify-content-center p-3 bg-success bg-opacity-10 text-success rounded-circle mb-3">
                  <CheckCircle2 size={48} />
                </div>
                <h4 className="fw-bold text-dark mb-1">Appointment Confirmed!</h4>
                <p className="text-muted small mb-4">
                  Your appointment with <strong>{technician.fullName}</strong> has been successfully booked.
                </p>

                <div className="card p-3 border-0 shadow-sm text-start max-w-md mx-auto mb-4 bg-white">
                  <div className="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span className="text-muted small">Booking Reference:</span>
                    <span className="fw-bold small font-monospace">{successData.id || 'N/A'}</span>
                  </div>
                  <div className="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span className="text-muted small">Scheduled Date & Time:</span>
                    <span className="fw-semibold small">
                      {appointmentDate} at {appointmentTime}
                    </span>
                  </div>
                  <div className="d-flex justify-content-between mb-2 pb-2 border-bottom">
                    <span className="text-muted small">Device:</span>
                    <span className="fw-semibold small">{deviceModel}</span>
                  </div>
                  <div className="d-flex justify-content-between">
                    <span className="text-muted small">Estimated Diagnostic Rate:</span>
                    <span className="fw-bold text-success small">{formatRate(technician.hourlyRate)}</span>
                  </div>
                </div>

                <div className="d-flex justify-content-center gap-2">
                  <button 
                    type="button" 
                    className="btn btn-primary px-4 fw-semibold"
                    onClick={() => {
                      onClose();
                      navigate('/bookings');
                    }}
                  >
                    View My Appointments
                  </button>
                  <button 
                    type="button" 
                    className="btn btn-outline-secondary px-3"
                    onClick={onClose}
                  >
                    Close
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleSubmit} noValidate>

                {/* Demo Notice Alert */}
                {technician.isDemo && (
                  <div className="alert alert-warning border-0 shadow-sm d-flex align-items-start gap-2 mb-3 py-2.5">
                    <AlertTriangle size={18} className="text-warning flex-shrink-0 mt-0.5" />
                    <div className="small">
                      <strong>Demo Technician:</strong> This profile is sample demonstration data. 
                      You can test the form, but live database appointments require registered technicians.
                    </div>
                  </div>
                )}

                {/* Auth Warning Alert */}
                {!isAuthenticated && (
                  <div className="alert alert-info border-0 shadow-sm d-flex align-items-center justify-content-between gap-2 mb-3 py-2.5">
                    <div className="d-flex align-items-center gap-2 small">
                      <User size={18} className="text-primary flex-shrink-0" />
                      <span>Please sign in to confirm and save your booking appointment.</span>
                    </div>
                    <button 
                      type="button" 
                      className="btn btn-sm btn-primary fw-bold flex-shrink-0"
                      onClick={() => {
                        onClose();
                        navigate('/login?redirect=/technicians');
                      }}
                    >
                      Sign In
                    </button>
                  </div>
                )}

                {/* Submit Error Alert */}
                {submitError && (
                  <div className="alert alert-danger border-0 shadow-sm d-flex align-items-start gap-2 mb-3 py-2.5">
                    <AlertCircle size={18} className="text-danger flex-shrink-0 mt-0.5" />
                    <div className="small flex-grow-1">
                      <strong>{submitError.type === 'auth' ? 'Authentication Required' : 'Booking Error'}:</strong> {submitError.message}
                    </div>
                  </div>
                )}

                {/* Registered Device Quick-Select (if user has devices) */}
                {userDevices.length > 0 && (
                  <div className="mb-3">
                    <label className="form-label small fw-semibold text-muted mb-1">
                      Quick-Select Saved Device (Optional)
                    </label>
                    <select 
                      className="form-select form-select-sm" 
                      value={selectedDeviceId} 
                      onChange={handleDeviceSelect}
                    >
                      <option value="">-- Enter device manually or choose saved --</option>
                      {userDevices.map((d) => (
                        <option key={d.id} value={d.id}>
                          {d.brand} {d.model} ({d.category || 'Device'})
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                {/* Device Category & Brand/Model */}
                <div className="row g-2 mb-3">
                  <div className="col-12 col-md-5">
                    <label className="form-label small fw-semibold">
                      Device Category <span className="text-danger">*</span>
                    </label>
                    <select 
                      className={`form-select ${errors.deviceCategory ? 'is-invalid' : ''}`}
                      value={deviceCategory}
                      onChange={(e) => {
                        setDeviceCategory(e.target.value);
                        if (errors.deviceCategory) setErrors({ ...errors, deviceCategory: null });
                      }}
                      required
                    >
                      <option value="">Select category...</option>
                      {categories.map((c) => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                    </select>
                    {errors.deviceCategory && (
                      <div className="invalid-feedback small">{errors.deviceCategory}</div>
                    )}
                  </div>

                  <div className="col-12 col-md-7">
                    <label className="form-label small fw-semibold">
                      Device Brand & Model <span className="text-danger">*</span>
                    </label>
                    <input 
                      type="text" 
                      className={`form-control ${errors.deviceModel ? 'is-invalid' : ''}`}
                      placeholder="e.g. Apple iPhone 13 Pro Max"
                      value={deviceModel}
                      onChange={(e) => {
                        setDeviceModel(e.target.value);
                        if (errors.deviceModel) setErrors({ ...errors, deviceModel: null });
                      }}
                      required
                    />
                    {errors.deviceModel && (
                      <div className="invalid-feedback small">{errors.deviceModel}</div>
                    )}
                  </div>
                </div>

                {/* Appointment Date & Time */}
                <div className="row g-2 mb-3">
                  <div className="col-12 col-md-6">
                    <label className="form-label small fw-semibold">
                      Appointment Date <span className="text-danger">*</span>
                    </label>
                    <div className="input-group">
                      <span className="input-group-text bg-white border-end-0">
                        <Calendar size={16} className="text-muted" />
                      </span>
                      <input 
                        type="date" 
                        className={`form-control border-start-0 ${errors.appointmentDate ? 'is-invalid' : ''}`}
                        min={todayStr}
                        value={appointmentDate}
                        onChange={(e) => {
                          setAppointmentDate(e.target.value);
                          if (errors.appointmentDate) setErrors({ ...errors, appointmentDate: null });
                        }}
                        required
                      />
                      {errors.appointmentDate && (
                        <div className="invalid-feedback small">{errors.appointmentDate}</div>
                      )}
                    </div>
                  </div>

                  <div className="col-12 col-md-6">
                    <label className="form-label small fw-semibold">
                      Preferred Time Slot <span className="text-danger">*</span>
                    </label>
                    <div className="input-group">
                      <span className="input-group-text bg-white border-end-0">
                        <Clock size={16} className="text-muted" />
                      </span>
                      <select 
                        className={`form-select border-start-0 ${errors.appointmentTime ? 'is-invalid' : ''}`}
                        value={appointmentTime}
                        onChange={(e) => {
                          setAppointmentTime(e.target.value);
                          if (errors.appointmentTime) setErrors({ ...errors, appointmentTime: null });
                        }}
                        required
                      >
                        <option value="09:00">09:00 AM — Morning Slot</option>
                        <option value="10:00">10:00 AM — Morning Slot</option>
                        <option value="11:30">11:30 AM — Midday Slot</option>
                        <option value="13:30">01:30 PM — Afternoon Slot</option>
                        <option value="15:00">03:00 PM — Afternoon Slot</option>
                        <option value="16:30">04:30 PM — Late Afternoon</option>
                        <option value="18:00">06:00 PM — Evening Slot</option>
                      </select>
                      {errors.appointmentTime && (
                        <div className="invalid-feedback small">{errors.appointmentTime}</div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Problem Description */}
                <div className="mb-3">
                  <label className="form-label small fw-semibold">
                    Problem Description <span className="text-danger">*</span>
                  </label>
                  <textarea 
                    className={`form-control ${errors.issueDescription ? 'is-invalid' : ''}`}
                    rows={3}
                    placeholder="Describe the defect, damage, or symptoms (e.g. cracked OLED screen, touch unresponsive, battery drain)..."
                    value={issueDescription}
                    onChange={(e) => {
                      setIssueDescription(e.target.value);
                      if (errors.issueDescription) setErrors({ ...errors, issueDescription: null });
                    }}
                    required
                  />
                  {errors.issueDescription && (
                    <div className="invalid-feedback small">{errors.issueDescription}</div>
                  )}
                </div>

                {/* Optional Additional Notes */}
                <div className="mb-3">
                  <label className="form-label small fw-semibold text-muted">
                    Additional Instructions / Access Notes (Optional)
                  </label>
                  <input 
                    type="text" 
                    className="form-control form-control-sm"
                    placeholder="e.g. Home visit requested, please call on arrival, etc."
                    value={additionalNotes}
                    onChange={(e) => setAdditionalNotes(e.target.value)}
                  />
                </div>

                {/* Pricing Summary */}
                <div className="p-3 bg-white rounded-3 border d-flex align-items-center justify-content-between mb-4 shadow-sm">
                  <div>
                    <span className="extra-small fw-semibold text-muted text-uppercase d-block">Base Service Fee</span>
                    <span className="small text-muted">Diagnosis & first hour of technician labor</span>
                  </div>
                  <div className="text-end">
                    <span className="fs-5 fw-bold text-success">{formatRate(technician.hourlyRate)}</span>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="d-flex align-items-center justify-content-end gap-2 pt-2 border-top">
                  <button 
                    type="button" 
                    className="btn btn-light px-3 fw-semibold border"
                    onClick={onClose}
                    disabled={submitting}
                  >
                    Cancel
                  </button>
                  <button 
                    type="submit" 
                    className="btn btn-primary px-4 fw-bold d-flex align-items-center gap-2"
                    disabled={submitting || technician.isDemo}
                  >
                    {submitting ? (
                      <>
                        <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true" />
                        <span>Confirming...</span>
                      </>
                    ) : (
                      <>
                        <Calendar size={16} />
                        <span>Confirm Booking</span>
                      </>
                    )}
                  </button>
                </div>

              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingModal;
