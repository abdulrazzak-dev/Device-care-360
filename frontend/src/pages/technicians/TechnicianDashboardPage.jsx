import React, { useEffect, useState, useMemo } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { technicianService } from '../../services/technicianService';
import { bookingService } from '../../services/bookingService';
import StatusBadge from '../../components/common/StatusBadge';
import Loader from '../../components/common/Loader';
import { 
  Wrench, Calendar, Star, CheckCircle, ToggleLeft, ToggleRight, 
  Upload, FileText, AlertTriangle, Clock, ShieldCheck, ShieldAlert,
  Edit3, MapPin, DollarSign, Award, X, Check
} from 'lucide-react';
import { CANONICAL_SPECIALIZATIONS, getCategoryDisplayLabel } from '../../utils/categoryConstants';

const TechnicianDashboardPage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  // Edit Profile Modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({
    fullName: '',
    phone: '',
    experienceYears: 1,
    hourlyRate: 2500,
    city: '',
    serviceArea: '',
    professionalBio: '',
    specializations: [],
    isAvailable: true
  });
  const [savingProfile, setSavingProfile] = useState(false);
  const [editError, setEditError] = useState('');

  // Certificate upload state
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState('');

  const loadTechData = async () => {
    if (!user?.userId) return;
    try {
      const [profRes, bookRes] = await Promise.all([
        technicianService.getMyProfile().catch(() => technicianService.getById(user.userId)).catch(() => ({ data: null })),
        bookingService.getByTechnicianId(user.userId).catch(() => ({ data: [] }))
      ]);
      const techProfile = profRes.data;
      setProfile(techProfile);
      setBookings(bookRes.data || []);

      if (techProfile) {
        setEditFormData({
          fullName: techProfile.fullName || '',
          phone: techProfile.phone || '',
          experienceYears: techProfile.yearsOfExperience || techProfile.experienceYears || 1,
          hourlyRate: techProfile.hourlyRate || 2500,
          city: techProfile.city || '',
          serviceArea: techProfile.serviceArea || (techProfile.serviceAreas?.[0] || ''),
          professionalBio: techProfile.professionalBio || '',
          specializations: techProfile.specializations || techProfile.categories || [],
          isAvailable: techProfile.available !== undefined ? techProfile.available : true
        });
      }
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
      await loadTechData();
    } catch (err) {
      alert('Failed to update availability: ' + err.message);
    }
  };

  const handleUpdateBookingStatus = async (bookingId, status) => {
    try {
      await bookingService.updateStatus(bookingId, status, 'Technician updated status');
      await loadTechData();
    } catch (err) {
      alert('Failed to update status: ' + err.message);
    }
  };

  // Profile completion calculation
  const completionPercentage = useMemo(() => {
    if (!profile) return 0;
    let score = 0;
    if (profile.fullName) score += 20;
    if (profile.specializations && profile.specializations.length > 0) score += 20;
    if (profile.city || profile.serviceArea) score += 20;
    if (profile.hourlyRate) score += 10;
    if (profile.professionalBio) score += 10;
    if (profile.certificateUrl) score += 20;
    return score;
  }, [profile]);

  // Specialization toggle in Edit Modal
  const handleSpecializationToggle = (categoryKey) => {
    setEditFormData(prev => {
      const exists = prev.specializations.includes(categoryKey);
      const updated = exists
        ? prev.specializations.filter(k => k !== categoryKey)
        : [...prev.specializations, categoryKey];
      return { ...prev, specializations: updated };
    });
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setEditError('');

    if (editFormData.specializations.length === 0) {
      setEditError('Please select at least one repair specialization.');
      return;
    }
    const exp = Number(editFormData.experienceYears);
    if (isNaN(exp) || exp < 0 || exp > 50) {
      setEditError('Experience years must be between 0 and 50.');
      return;
    }
    const rate = Number(editFormData.hourlyRate);
    if (isNaN(rate) || rate < 0) {
      setEditError('Hourly rate must be a valid non-negative number.');
      return;
    }

    setSavingProfile(true);
    try {
      await technicianService.updateMyProfile({
        ...editFormData,
        experienceYears: exp,
        hourlyRate: rate,
        serviceAreas: editFormData.serviceArea ? [editFormData.serviceArea] : []
      });
      setShowEditModal(false);
      await loadTechData();
    } catch (err) {
      setEditError(err.message || 'Failed to update professional profile.');
    } finally {
      setSavingProfile(false);
    }
  };

  // Certificate file selection & upload
  const handleFileChange = (e) => {
    setUploadError('');
    setUploadSuccess('');
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      setUploadError('Selected document exceeds the 5 MB maximum size limit.');
      return;
    }
    const allowed = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!allowed.includes(file.type.toLowerCase())) {
      setUploadError('Invalid file format. Only PDF, JPG, and PNG documents are allowed.');
      return;
    }
    setSelectedFile(file);
  };

  const handleUploadCertificate = async () => {
    if (!selectedFile) return;
    setUploading(true);
    setUploadProgress(0);
    setUploadError('');
    setUploadSuccess('');

    try {
      const res = await technicianService.uploadCertificate(selectedFile, (progressEvent) => {
        if (progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percent);
        }
      });
      setUploadSuccess(res.message || 'Certificate uploaded successfully! Your profile is under review.');
      setSelectedFile(null);
      await loadTechData();
    } catch (err) {
      setUploadError(err.message || 'Upload failed. Please ensure file is under 5 MB and try again.');
    } finally {
      setUploading(false);
    }
  };

  if (loading) return <DashboardLayout><Loader text="Loading technician workspace..." /></DashboardLayout>;

  const currentStatus = (profile?.verificationStatus || 'PENDING').toUpperCase();
  const isApproved = currentStatus === 'APPROVED' || currentStatus === 'VERIFIED';
  const isPending = currentStatus === 'PENDING';
  const isRejected = currentStatus === 'REJECTED';
  const isSuspended = currentStatus === 'SUSPENDED';

  return (
    <DashboardLayout>
      {/* Header bar */}
      <div className="d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-3 mb-4">
        <div>
          <h3 className="fw-bold mb-1 d-flex align-items-center gap-2">
            <Wrench className="text-primary" size={26} />
            <span>Technician Workspace</span>
          </h3>
          <p className="text-muted small mb-0">
            Manage your certified profile, upload credentials, and review client repair requests.
          </p>
        </div>
        <div className="d-flex align-items-center gap-2">
          <button 
            className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1.5 shadow-sm"
            onClick={() => setShowEditModal(true)}
          >
            <Edit3 size={15} />
            <span>Edit Profile</span>
          </button>
          {profile && (
            <button 
              className={`btn btn-sm ${profile.available ? 'btn-success' : 'btn-outline-secondary'} d-flex align-items-center gap-2 shadow-sm`} 
              onClick={toggleAvailability}
            >
              {profile.available ? <ToggleRight size={20} /> : <ToggleLeft size={20} />}
              <span>{profile.available ? 'Available for Jobs' : 'Offline / Unavailable'}</span>
            </button>
          )}
        </div>
      </div>

      {/* Verification Status Panel */}
      <div className="mb-4">
        {isPending && (
          <div className="alert alert-warning border-warning border-start border-4 shadow-sm p-3 rounded-3 mb-0 d-flex align-items-start gap-3">
            <Clock size={24} className="text-warning flex-shrink-0 mt-0.5" />
            <div>
              <h6 className="fw-bold text-dark mb-1">Verification Status: PENDING REVIEW</h6>
              <p className="mb-0 small text-muted">
                Your credentials and certificate are under review by the admin team. You will be notified when your verification status changes.
              </p>
            </div>
          </div>
        )}

        {isApproved && (
          <div className="alert alert-success border-success border-start border-4 shadow-sm p-3 rounded-3 mb-0 d-flex align-items-start gap-3">
            <CheckCircle size={24} className="text-success flex-shrink-0 mt-0.5" />
            <div>
              <h6 className="fw-bold text-success mb-1">Verification Status: VERIFIED & APPROVED</h6>
              <p className="mb-0 small text-dark">
                Your technician profile has been verified. You can receive eligible repair bookings.
              </p>
            </div>
          </div>
        )}

        {isRejected && (
          <div className="alert alert-danger border-danger border-start border-4 shadow-sm p-3 rounded-3 mb-0 d-flex align-items-start gap-3">
            <AlertTriangle size={24} className="text-danger flex-shrink-0 mt-0.5" />
            <div>
              <h6 className="fw-bold text-danger mb-1">Verification Status: NOT APPROVED</h6>
              <p className="mb-1 small">
                Your verification was not approved. Review the feedback and update your documents before resubmitting.
              </p>
              {profile?.rejectionReason && (
                <div className="bg-white p-2 rounded border border-danger-subtle small text-danger mt-1">
                  <strong>Reviewer Feedback:</strong> {profile.rejectionReason}
                </div>
              )}
            </div>
          </div>
        )}

        {isSuspended && (
          <div className="alert alert-secondary border-secondary border-start border-4 shadow-sm p-3 rounded-3 mb-0 d-flex align-items-start gap-3">
            <ShieldAlert size={24} className="text-secondary flex-shrink-0 mt-0.5" />
            <div>
              <h6 className="fw-bold text-dark mb-1">Verification Status: TEMPORARILY SUSPENDED</h6>
              <p className="mb-0 small text-muted">
                Your technician account is temporarily unavailable. Contact support for further information.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Professional Summary & Stats Cards */}
      <div className="row g-3 mb-4">
        {/* Profile Details Card */}
        <div className="col-lg-8">
          <div className="card p-4 border-0 shadow-sm rounded-4 h-100">
            <div className="d-flex align-items-center justify-content-between mb-3">
              <div>
                <h5 className="fw-bold mb-0 text-dark">{profile?.fullName || user?.fullName || 'Technician'}</h5>
                <span className="text-muted small">{profile?.email || user?.email}</span>
              </div>
              <div className="text-end">
                <span className="badge bg-light text-dark border extra-small px-2.5 py-1.5 rounded-pill">
                  Profile Completion: <strong>{completionPercentage}%</strong>
                </span>
              </div>
            </div>

            <div className="progress mb-3" style={{ height: '6px' }}>
              <div 
                className={`progress-bar ${completionPercentage === 100 ? 'bg-success' : 'bg-primary'}`} 
                style={{ width: `${completionPercentage}%` }}
              />
            </div>

            {profile?.professionalBio && (
              <p className="text-muted small mb-3 fst-italic bg-light p-2.5 rounded-3">
                "{profile.professionalBio}"
              </p>
            )}

            <div className="row g-2 pt-2 border-top">
              <div className="col-sm-6 col-md-3">
                <div className="text-muted extra-small text-uppercase fw-semibold">Experience</div>
                <div className="fw-bold fs-6 mt-0.5 text-dark">
                  {profile?.yearsOfExperience || profile?.experienceYears || 1} Years
                </div>
              </div>
              <div className="col-sm-6 col-md-3">
                <div className="text-muted extra-small text-uppercase fw-semibold">Rate / Hour</div>
                <div className="fw-bold fs-6 mt-0.5 text-success">
                  LKR {Number(profile?.hourlyRate || 2500).toLocaleString()}
                </div>
              </div>
              <div className="col-sm-6 col-md-3">
                <div className="text-muted extra-small text-uppercase fw-semibold">Service Location</div>
                <div className="fw-bold fs-6 mt-0.5 text-dark text-truncate">
                  {profile?.city || profile?.serviceArea || 'Not specified'}
                </div>
              </div>
              <div className="col-sm-6 col-md-3">
                <div className="text-muted extra-small text-uppercase fw-semibold">Availability</div>
                <div className="fw-bold fs-6 mt-0.5">
                  <span className={`badge ${profile?.available ? 'bg-success' : 'bg-secondary'}`}>
                    {profile?.available ? 'Online' : 'Offline'}
                  </span>
                </div>
              </div>
            </div>

            {/* Specializations Badges */}
            <div className="mt-3 pt-3 border-top">
              <div className="text-muted extra-small text-uppercase fw-semibold mb-2">Supported Device Categories</div>
              <div className="d-flex flex-wrap gap-1.5">
                {(profile?.specializations || []).length > 0 ? (
                  profile.specializations.map((spec, i) => (
                    <span key={i} className="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 py-1.5 px-2.5 rounded-pill small">
                      {getCategoryDisplayLabel(spec)}
                    </span>
                  ))
                ) : (
                  <span className="text-muted small fst-italic">No specializations configured yet. Click "Edit Profile" to add categories.</span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Rating & Jobs Stats */}
        <div className="col-lg-4">
          <div className="d-flex flex-column gap-3 h-100">
            <div className="card p-3 border-0 shadow-sm border-start border-4 border-warning rounded-3 flex-fill">
              <div className="text-muted small fw-semibold">Client Rating</div>
              <div className="fs-3 fw-bold mt-1 text-warning d-flex align-items-center gap-1">
                <span>★ {profile?.averageRating || 5.0}</span>
                <span className="text-muted fs-6 fw-normal">({profile?.totalReviews || 0} reviews)</span>
              </div>
            </div>

            <div className="card p-3 border-0 shadow-sm border-start border-4 border-primary rounded-3 flex-fill">
              <div className="text-muted small fw-semibold">Assigned Repair Jobs</div>
              <div className="fs-3 fw-bold mt-1 text-primary">{bookings.length}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Credentials & Certificate Upload Card */}
      <div className="card border-0 shadow-sm p-4 rounded-4 mb-4">
        <div className="d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-2 mb-3">
          <div>
            <h5 className="fw-bold mb-0 d-flex align-items-center gap-2">
              <Award className="text-primary" size={20} />
              <span>Professional Credentials & Certificates</span>
            </h5>
            <p className="text-muted small mb-0">
              Upload your electronics repair certification, trade license, or diploma (PDF, JPG, PNG up to 5 MB).
            </p>
          </div>
        </div>

        {profile?.certificateUrl ? (
          <div className="p-3 bg-light rounded-3 border d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between gap-3 mb-3">
            <div className="d-flex align-items-center gap-2.5">
              <div className="bg-primary bg-opacity-10 text-primary p-2.5 rounded-circle">
                <FileText size={22} />
              </div>
              <div>
                <div className="fw-semibold small text-dark">
                  {profile.certificateFilename || 'Technical Certificate Document'}
                </div>
                <div className="text-muted extra-small">
                  Uploaded: {profile.certificateUploadedAt ? new Date(profile.certificateUploadedAt).toLocaleString() : 'Registered'}
                </div>
              </div>
            </div>
            <div className="d-flex align-items-center gap-2">
              <a 
                href={profile.certificateUrl} 
                target="_blank" 
                rel="noopener noreferrer" 
                className="btn btn-sm btn-outline-primary"
              >
                View Stored Certificate
              </a>
            </div>
          </div>
        ) : (
          <div className="alert alert-secondary small d-flex align-items-center gap-2 mb-3 rounded-3">
            <AlertTriangle size={18} className="text-warning flex-shrink-0" />
            <span>No certification document on file. Please upload your credential to be eligible for admin approval.</span>
          </div>
        )}

        {/* Upload Form */}
        <div className="p-3 bg-light rounded-3 border">
          <label className="form-label small fw-semibold mb-1">
            {profile?.certificateUrl ? 'Replace / Update Certificate Document' : 'Upload Verification Certificate'}
          </label>
          <div className="d-flex flex-column flex-sm-row align-items-stretch gap-2">
            <input 
              type="file" 
              className="form-control form-control-sm" 
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={handleFileChange}
              disabled={uploading}
            />
            <button 
              type="button" 
              className="btn btn-sm btn-primary d-flex align-items-center justify-content-center gap-1.5 px-3 py-1.5 flex-shrink-0"
              onClick={handleUploadCertificate}
              disabled={!selectedFile || uploading}
            >
              <Upload size={16} />
              <span>{uploading ? `Uploading (${uploadProgress}%)` : 'Upload File'}</span>
            </button>
          </div>

          {uploadProgress > 0 && uploading && (
            <div className="progress mt-2" style={{ height: '4px' }}>
              <div className="progress-bar progress-bar-striped progress-bar-animated" style={{ width: `${uploadProgress}%` }} />
            </div>
          )}

          {uploadSuccess && (
            <div className="alert alert-success small py-2 px-3 mt-2 mb-0 d-flex align-items-center gap-2 rounded-2">
              <CheckCircle size={16} />
              <span>{uploadSuccess}</span>
            </div>
          )}

          {uploadError && (
            <div className="alert alert-danger small py-2 px-3 mt-2 mb-0 d-flex align-items-center gap-2 rounded-2">
              <AlertTriangle size={16} />
              <span>{uploadError}</span>
            </div>
          )}

          <div className="text-muted extra-small mt-2">
            * Note: Replacing or uploading credentials initiates the re-verification workflow by administrators.
          </div>
        </div>
      </div>

      {/* Assigned Bookings Table */}
      <div className="card border-0 shadow-sm p-4 rounded-4">
        <h5 className="fw-bold mb-3 d-flex align-items-center gap-2">
          <Calendar className="text-primary" size={20} />
          <span>Assigned Client Repair Jobs</span>
        </h5>
        {bookings.length === 0 ? (
          <p className="text-muted small mb-0">No repair jobs assigned currently.</p>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead>
                <tr className="text-muted small">
                  <th>Date</th>
                  <th>Client Issue</th>
                  <th>Category</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map(b => (
                  <tr key={b.id}>
                    <td className="small">{new Date(b.appointmentTime).toLocaleString()}</td>
                    <td className="small fw-semibold">{b.issueDescription}</td>
                    <td className="small">
                      <span className="badge bg-light text-dark border">
                        {getCategoryDisplayLabel(b.deviceCategory)}
                      </span>
                    </td>
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

      {/* Edit Professional Profile Modal */}
      {showEditModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <div className="modal-header border-bottom px-4 py-3">
                <h5 className="modal-title fw-bold d-flex align-items-center gap-2">
                  <Edit3 size={18} className="text-primary" />
                  <span>Edit Professional Profile</span>
                </h5>
                <button type="button" className="btn-close" onClick={() => setShowEditModal(false)}></button>
              </div>

              <form onSubmit={handleSaveProfile}>
                <div className="modal-body p-4">
                  {editError && (
                    <div className="alert alert-danger small p-2.5 mb-3 rounded-2">
                      {editError}
                    </div>
                  )}

                  <div className="row g-3 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Full Name</label>
                      <input 
                        type="text" 
                        className="form-control" 
                        value={editFormData.fullName} 
                        onChange={e => setEditFormData({ ...editFormData, fullName: e.target.value })} 
                        required 
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Phone Number</label>
                      <input 
                        type="tel" 
                        className="form-control" 
                        value={editFormData.phone} 
                        onChange={e => setEditFormData({ ...editFormData, phone: e.target.value })} 
                        required 
                      />
                    </div>
                  </div>

                  <div className="row g-3 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Years of Experience (0 - 50)</label>
                      <input 
                        type="number" 
                        className="form-control" 
                        min="0" 
                        max="50" 
                        value={editFormData.experienceYears} 
                        onChange={e => setEditFormData({ ...editFormData, experienceYears: e.target.value })} 
                        required 
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Base Diagnostic Fee / Hourly Rate (LKR)</label>
                      <input 
                        type="number" 
                        className="form-control" 
                        min="0" 
                        step="50"
                        value={editFormData.hourlyRate} 
                        onChange={e => setEditFormData({ ...editFormData, hourlyRate: e.target.value })} 
                        required 
                      />
                    </div>
                  </div>

                  <div className="row g-3 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Operating City</label>
                      <input 
                        type="text" 
                        className="form-control" 
                        value={editFormData.city} 
                        onChange={e => setEditFormData({ ...editFormData, city: e.target.value })} 
                        required 
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Service Area / Province</label>
                      <input 
                        type="text" 
                        className="form-control" 
                        value={editFormData.serviceArea} 
                        onChange={e => setEditFormData({ ...editFormData, serviceArea: e.target.value })} 
                        required 
                      />
                    </div>
                  </div>

                  {/* Specializations Selector */}
                  <div className="mb-3">
                    <label className="form-label small fw-semibold d-block mb-2">
                      Device Specializations <span className="text-danger">*</span> (Select all you service)
                    </label>
                    <div className="d-flex flex-wrap gap-2">
                      {CANONICAL_SPECIALIZATIONS.map(spec => {
                        const isSelected = editFormData.specializations.includes(spec.key);
                        return (
                          <button
                            type="button"
                            key={spec.key}
                            className={`btn btn-sm rounded-pill px-3 py-1.5 d-flex align-items-center gap-1.5 transition-all ${
                              isSelected ? 'btn-primary shadow-sm' : 'btn-outline-secondary'
                            }`}
                            onClick={() => handleSpecializationToggle(spec.key)}
                          >
                            {isSelected && <Check size={14} />}
                            <span>{spec.label}</span>
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Professional Bio</label>
                    <textarea 
                      className="form-control" 
                      rows="3" 
                      value={editFormData.professionalBio} 
                      onChange={e => setEditFormData({ ...editFormData, professionalBio: e.target.value })} 
                    />
                  </div>

                  <div className="alert alert-warning small p-2.5 mb-0 rounded-2">
                    <strong>Important Policy Note:</strong> Modifying your technical specializations on an approved profile triggers re-verification by administrators to ensure certified standards.
                  </div>
                </div>

                <div className="modal-footer border-top px-4 py-3">
                  <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => setShowEditModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary btn-sm px-4 fw-bold" disabled={savingProfile}>
                    {savingProfile ? 'Saving Changes...' : 'Save Profile Changes'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
};

export default TechnicianDashboardPage;
