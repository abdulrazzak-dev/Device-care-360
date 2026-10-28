import React, { useEffect, useState, useMemo } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { technicianService } from '../../services/technicianService';
import BookingModal from '../../components/technicians/BookingModal';
import EmptyState from '../../components/common/EmptyState';
import ErrorState from '../../components/common/ErrorState';
import { 
  Star, ShieldCheck, MapPin, Wrench, Calendar, Clock, 
  DollarSign, CheckCircle2, AlertTriangle, RefreshCw, Sparkles, User
} from 'lucide-react';
import { Link } from 'react-router-dom';

import { CANONICAL_SPECIALIZATIONS, matchesCategory, getCategoryDisplayLabel } from '../../utils/categoryConstants';

// 4 Sample profiles used as demo fallback when the API returns an empty array
const SAMPLE_DEMO_TECHNICIANS = [
  {
    id: 'demo-tech-001',
    fullName: 'Kasun Perera',
    email: 'kasun.perera.demo@devicecare360.com',
    phone: '+94 77 123 4567',
    specializations: ['Smartphone Repair', 'Smartphone'],
    serviceAreas: ['Colombo', 'Western Province'],
    available: true,
    verificationStatus: 'VERIFIED',
    averageRating: 4.9,
    totalReviews: 128,
    hourlyRate: 2500.0,
    yearsOfExperience: 7,
    profileImageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=400',
    isDemo: true
  },
  {
    id: 'demo-tech-002',
    fullName: 'Mohamed Rizwan',
    email: 'mohamed.rizwan.demo@devicecare360.com',
    phone: '+94 71 987 6543',
    specializations: ['Laptop/PC Hardware', 'Laptop'],
    serviceAreas: ['Negombo', 'Western Province'],
    available: true,
    verificationStatus: 'VERIFIED',
    averageRating: 4.8,
    totalReviews: 94,
    hourlyRate: 3000.0,
    yearsOfExperience: 9,
    profileImageUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=400',
    isDemo: true
  },
  {
    id: 'demo-tech-003',
    fullName: 'Nimal Fernando',
    email: 'nimal.fernando.demo@devicecare360.com',
    phone: '+94 76 555 1234',
    specializations: ['Audio/Video Gear', 'Television'],
    serviceAreas: ['Gampaha', 'Western Province'],
    available: true,
    verificationStatus: 'VERIFIED',
    averageRating: 4.7,
    totalReviews: 82,
    hourlyRate: 2000.0,
    yearsOfExperience: 6,
    profileImageUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=400',
    isDemo: true
  },
  {
    id: 'demo-tech-004',
    fullName: 'Sahan Jayawardena',
    email: 'sahan.jayawardena.demo@devicecare360.com',
    phone: '+94 75 333 9876',
    specializations: ['Smart Home Appliances', 'Refrigerator'],
    serviceAreas: ['Kandy', 'Central Province'],
    available: true,
    verificationStatus: 'VERIFIED',
    averageRating: 4.9,
    totalReviews: 110,
    hourlyRate: 2800.0,
    yearsOfExperience: 8,
    profileImageUrl: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=400',
    isDemo: true
  }
];

const SPECIALIZATION_FILTERS = [
  { label: 'All Specializations', value: '' },
  ...CANONICAL_SPECIALIZATIONS.map(s => ({ label: s.label, value: s.key }))
];

const TechnicianDirectoryPage = () => {
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedSpecialization, setSelectedSpecialization] = useState('');
  const [isDemoMode, setIsDemoMode] = useState(false);
  const [selectedTechForBooking, setSelectedTechForBooking] = useState(null);
  const [bookingToast, setBookingToast] = useState(null);

  const fetchTechnicians = async () => {
    setLoading(true);
    setError(null);

    try {
      // Pass filter to backend if selected
      const params = selectedSpecialization ? { specialization: selectedSpecialization } : {};
      const res = await technicianService.getAll(params);

      // Support either standard ApiResponse wrapper { data: [...] } or raw array [...]
      const dataList = Array.isArray(res) ? res : Array.isArray(res?.data) ? res.data : [];

      if (dataList.length > 0) {
        setTechnicians(dataList.map((t) => ({ ...t, isDemo: false })));
        setIsDemoMode(false);
      } else {
        // Successful response but collection is empty: provide clearly labeled demo profiles
        setTechnicians(SAMPLE_DEMO_TECHNICIANS);
        setIsDemoMode(true);
      }
    } catch (err) {
      console.error('Technician fetch error:', err);
      setError(err.message || 'Unable to connect to the Technician Service.');
      // Do not silently treat error as empty response; keep technicians empty
      setTechnicians([]);
      setIsDemoMode(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTechnicians();
  }, [selectedSpecialization]);

  // Case-insensitive client filter so both demo data and API records filter consistently
  const filteredTechnicians = useMemo(() => {
    if (!selectedSpecialization) return technicians;
    return technicians.filter((tech) => {
      const specs = tech.specializations || tech.categories || [];
      return matchesCategory(specs, selectedSpecialization);
    });
  }, [technicians, selectedSpecialization]);

  const formatLkrRate = (rate) => {
    const num = Number(rate) || 2500;
    return `LKR ${num.toLocaleString()}`;
  };

  const handleBookingSuccess = (newBooking) => {
    setBookingToast({
      message: `Appointment successfully booked with ${selectedTechForBooking?.fullName}! Reference ID: ${newBooking?.id || 'Confirmed'}`,
      techName: selectedTechForBooking?.fullName
    });
    setSelectedTechForBooking(null);
  };

  return (
    <DashboardLayout>
      {/* Page Title & Controls */}
      <div className="d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-3 mb-4">
        <div>
          <div className="d-flex align-items-center gap-2">
            <h3 className="fw-bold mb-0 text-dark">Certified Technician Directory</h3>
            {isDemoMode && (
              <span className="badge bg-warning bg-opacity-25 text-warning border border-warning border-opacity-50 extra-small px-2 py-1 rounded-pill d-inline-flex align-items-center gap-1">
                <Sparkles size={12} /> Demo Preview
              </span>
            )}
          </div>
          <p className="text-muted small mb-0 mt-1">
            Browse verified electronics technicians, compare hourly rates, and schedule repairs.
          </p>
        </div>

        {/* Specialization Filter Dropdown */}
        <div className="w-100 w-sm-auto" style={{ minWidth: '240px' }}>
          <label htmlFor="specializationFilter" className="visually-hidden">Filter Specialization</label>
          <select 
            id="specializationFilter"
            className="form-select form-select-sm shadow-sm"
            value={selectedSpecialization} 
            onChange={(e) => setSelectedSpecialization(e.target.value)}
          >
            {SPECIALIZATION_FILTERS.map((f) => (
              <option key={f.value} value={f.value}>
                {f.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Success Booking Toast */}
      {bookingToast && (
        <div className="alert alert-success border-0 shadow-sm d-flex align-items-center justify-content-between mb-4 py-3">
          <div className="d-flex align-items-center gap-2">
            <CheckCircle2 size={20} className="text-success flex-shrink-0" />
            <span className="small fw-semibold">{bookingToast.message}</span>
          </div>
          <div className="d-flex align-items-center gap-2">
            <Link to="/bookings" className="btn btn-sm btn-success fw-bold text-nowrap">
              View Appointments
            </Link>
            <button 
              type="button" 
              className="btn-close" 
              aria-label="Dismiss" 
              onClick={() => setBookingToast(null)}
            />
          </div>
        </div>
      )}

      {/* Demo Mode Notice Banner */}
      {isDemoMode && !loading && !error && (
        <div className="alert alert-info border-0 shadow-sm d-flex align-items-start gap-3 mb-4 py-3">
          <Sparkles size={20} className="text-primary flex-shrink-0 mt-0.5" />
          <div className="small">
            <strong className="d-block mb-0.5">Sample Demonstration Profiles Active:</strong>
            The live database collection is currently awaiting records. Displaying 4 verified demo technician profiles to preview directory functionality.
          </div>
        </div>
      )}

      {/* API Error State */}
      {error && !loading ? (
        <div className="card border-0 shadow-sm p-4 text-center my-4">
          <div className="d-inline-flex align-items-center justify-content-center p-3 bg-danger bg-opacity-10 text-danger rounded-circle mx-auto mb-3">
            <AlertTriangle size={32} />
          </div>
          <h5 className="fw-bold mb-1">Unable to Load Technicians</h5>
          <p className="text-muted small max-w-md mx-auto mb-4">{error}</p>
          <div className="d-flex justify-content-center gap-2">
            <button className="btn btn-primary btn-sm d-inline-flex align-items-center gap-1.5 fw-semibold" onClick={fetchTechnicians}>
              <RefreshCw size={14} /> Retry Connection
            </button>
            <button 
              className="btn btn-outline-secondary btn-sm"
              onClick={() => {
                setTechnicians(SAMPLE_DEMO_TECHNICIANS);
                setIsDemoMode(true);
                setError(null);
              }}
            >
              Browse Sample Preview
            </button>
          </div>
        </div>
      ) : loading ? (
        /* Loading Skeletons */
        <div className="row g-3 g-md-4">
          {[1, 2, 3, 4].map((i) => (
            <div className="col-12 col-md-6 col-lg-4 col-xl-3" key={i}>
              <div className="card p-3 p-sm-4 border-0 shadow-sm h-100 placeholder-glow">
                <div className="d-flex align-items-center gap-3 mb-3">
                  <div className="placeholder rounded-circle" style={{ width: '56px', height: '56px' }} />
                  <div className="flex-grow-1">
                    <span className="placeholder col-8 mb-2 d-block" />
                    <span className="placeholder col-5 d-block" />
                  </div>
                </div>
                <div className="mb-3">
                  <span className="placeholder col-10 mb-2 d-block" />
                  <span className="placeholder col-6 d-block" />
                </div>
                <div className="pt-3 border-top mt-auto">
                  <span className="placeholder btn btn-primary disabled col-12" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : filteredTechnicians.length === 0 ? (
        /* Empty State */
        <EmptyState 
          title="No Technicians Found" 
          message={
            selectedSpecialization 
              ? `No technicians matched "${selectedSpecialization}". Try resetting your filter to "All Specializations".`
              : 'No technician profiles are currently registered.'
          } 
        />
      ) : (
        /* Technician Cards Grid */
        <div className="row g-3 g-md-4">
          {filteredTechnicians.map((tech) => (
            <div className="col-12 col-md-6 col-lg-4 col-xl-3" key={tech.id}>
              <div className="card p-3 p-sm-4 border-0 shadow-sm card-hover h-100 d-flex flex-column justify-content-between position-relative">
                
                {/* Demo Ribbon / Tag */}
                {tech.isDemo && (
                  <span 
                    className="position-absolute top-0 end-0 m-3 badge bg-warning bg-opacity-25 text-warning border border-warning border-opacity-50 extra-small px-2 py-0.5 rounded-pill"
                    title="Sample profile for demonstration"
                  >
                    Demo
                  </span>
                )}

                <div>
                  {/* Top: Avatar & Verification */}
                  <div className="d-flex align-items-center gap-3 mb-3">
                    <div 
                      className="rounded-circle overflow-hidden bg-light border d-flex align-items-center justify-content-center flex-shrink-0"
                      style={{ width: '54px', height: '54px' }}
                    >
                      {tech.profileImageUrl ? (
                        <img 
                          src={tech.profileImageUrl} 
                          alt={tech.fullName} 
                          className="w-100 h-100 object-fit-cover"
                          onError={(e) => {
                            e.target.style.display = 'none';
                            e.target.nextElementSibling.style.display = 'flex';
                          }}
                        />
                      ) : null}
                      <div 
                        className="w-100 h-100 d-flex align-items-center justify-content-center bg-primary text-white fw-bold fs-5"
                        style={{ display: tech.profileImageUrl ? 'none' : 'flex' }}
                      >
                        {tech.fullName?.charAt(0) || 'T'}
                      </div>
                    </div>

                    <div className="overflow-hidden">
                      <h5 className="fw-bold mb-0.5 text-dark text-truncate" title={tech.fullName}>
                        {tech.fullName}
                      </h5>
                      <div className="d-flex align-items-center gap-1.5 flex-wrap">
                        {tech.verificationStatus === 'VERIFIED' ? (
                          <span className="badge bg-success bg-opacity-10 text-success fw-bold px-2 py-0.5 rounded-pill extra-small d-inline-flex align-items-center gap-1">
                            <ShieldCheck size={12} /> Verified
                          </span>
                        ) : (
                          <span className="badge bg-secondary bg-opacity-10 text-muted extra-small px-2 py-0.5 rounded-pill">
                            {tech.verificationStatus || 'Pending'}
                          </span>
                        )}
                        <span className="badge bg-primary bg-opacity-10 text-primary extra-small px-2 py-0.5 rounded-pill">
                          {tech.available ? 'Available' : 'Busy'}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Rating & Experience */}
                  <div className="d-flex align-items-center justify-content-between mb-3 py-1.5 px-2.5 bg-light rounded-3 small">
                    <div className="d-flex align-items-center text-warning fw-bold">
                      <Star size={15} className="fill-current me-1 flex-shrink-0" />
                      <span>{Number(tech.averageRating || 4.9).toFixed(1)}</span>
                      <span className="text-muted fw-normal ms-1">({tech.totalReviews || 0})</span>
                    </div>
                    <div className="text-muted fw-medium d-flex align-items-center gap-1">
                      <Clock size={13} className="text-muted flex-shrink-0" />
                      <span>{tech.yearsOfExperience || 5} yrs exp</span>
                    </div>
                  </div>

                  {/* Location & Hourly Rate */}
                  <div className="mb-3 small">
                    <div className="d-flex align-items-center justify-content-between mb-1">
                      <span className="text-muted d-flex align-items-center gap-1">
                        <MapPin size={14} className="text-muted flex-shrink-0" />
                        <span className="text-truncate" style={{ maxWidth: '140px' }}>
                          {tech.serviceAreas?.join(', ') || 'Colombo'}
                        </span>
                      </span>
                      <span className="fw-bold text-success">
                        {formatLkrRate(tech.hourlyRate)}/hr
                      </span>
                    </div>
                  </div>

                  {/* Specializations Badges */}
                  <div className="mb-3">
                    <span className="text-muted extra-small fw-semibold d-block mb-1.5 text-uppercase">
                      Specializations
                    </span>
                    <div className="d-flex flex-wrap gap-1">
                      {tech.specializations?.slice(0, 3).map((spec, idx) => (
                        <span key={idx} className="badge bg-white text-dark border extra-small text-truncate" style={{ maxWidth: '160px' }}>
                          {spec}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Footer Action: Primary Book Appointment Button */}
                <div className="pt-3 border-top mt-auto">
                  <button 
                    type="button"
                    className="btn btn-primary btn-sm w-100 d-flex align-items-center justify-content-center gap-1.5 fw-bold py-2 shadow-sm"
                    onClick={() => setSelectedTechForBooking(tech)}
                  >
                    <Calendar size={15} className="flex-shrink-0" />
                    <span>Book Appointment</span>
                  </button>
                </div>

              </div>
            </div>
          ))}
        </div>
      )}

      {/* Integrated Reusable Booking Modal */}
      <BookingModal 
        isOpen={Boolean(selectedTechForBooking)}
        onClose={() => setSelectedTechForBooking(null)}
        technician={selectedTechForBooking}
        onBookingSuccess={handleBookingSuccess}
      />
    </DashboardLayout>
  );
};

export default TechnicianDirectoryPage;
