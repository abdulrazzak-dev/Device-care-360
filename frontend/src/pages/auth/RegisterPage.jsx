import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import Footer from '../../components/layout/Footer';
import { useAuth } from '../../context/AuthContext';
import { UserPlus, AlertCircle, Wrench, ShieldCheck, Check } from 'lucide-react';
import { CANONICAL_SPECIALIZATIONS } from '../../utils/categoryConstants';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
    role: 'USER',
    // Technician-specific fields
    experienceYears: 3,
    specializations: [],
    hourlyRate: 2500,
    city: '',
    serviceArea: '',
    professionalBio: ''
  });

  const [validationError, setValidationError] = useState('');
  const [error, setError] = useState('');
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const isTechnician = formData.role === 'TECHNICIAN';

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSpecializationToggle = (categoryKey) => {
    setFormData(prev => {
      const exists = prev.specializations.includes(categoryKey);
      const updated = exists
        ? prev.specializations.filter(k => k !== categoryKey)
        : [...prev.specializations, categoryKey];
      return { ...prev, specializations: updated };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setValidationError('');

    // Technician-specific client validations
    if (isTechnician) {
      const exp = Number(formData.experienceYears);
      if (isNaN(exp) || exp < 0 || exp > 50) {
        setValidationError('Years of experience must be a valid number between 0 and 50.');
        return;
      }
      if (formData.specializations.length === 0) {
        setValidationError('Please select at least one device specialization category.');
        return;
      }
      const rate = Number(formData.hourlyRate);
      if (isNaN(rate) || rate < 0) {
        setValidationError('Hourly rate must be a valid non-negative number.');
        return;
      }
      if (!formData.city.trim()) {
        setValidationError('Please provide your operating city.');
        return;
      }
      if (!formData.serviceArea.trim()) {
        setValidationError('Please provide your service area (e.g., Western Province).');
        return;
      }
    }

    try {
      // Build safe payload: do not send technician fields for standard users
      const payload = {
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
        fullName: formData.fullName.trim(),
        phone: formData.phone.trim(),
        role: formData.role
      };

      if (isTechnician) {
        payload.experienceYears = Number(formData.experienceYears);
        payload.specializations = formData.specializations;
        payload.hourlyRate = Number(formData.hourlyRate);
        payload.city = formData.city.trim();
        payload.serviceArea = formData.serviceArea.trim();
        payload.professionalBio = formData.professionalBio.trim();
      }

      const res = await register(payload);
      const userRole = res.data?.role;
      if (userRole === 'TECHNICIAN') {
        navigate('/technician/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.message || 'Registration failed. Please check your details and try again.');
    }
  };

  return (
    <div className="d-flex flex-column min-vh-100 bg-light">
      <Navbar />
      <div className="container py-5 my-auto">
        <div className="row justify-content-center">
          <div className={isTechnician ? "col-md-9 col-lg-8" : "col-md-6 col-lg-5"}>
            <div className="card shadow-lg border-0 p-4 p-md-5 rounded-4">
              <div className="text-center mb-4">
                <div className="bg-primary bg-opacity-10 d-inline-flex p-3 rounded-circle text-primary mb-2">
                  {isTechnician ? <Wrench size={30} /> : <UserPlus size={30} />}
                </div>
                <h4 className="fw-bold">{isTechnician ? 'Technician Onboarding Registration' : 'Create an Account'}</h4>
                <p className="text-muted small">
                  {isTechnician
                    ? 'Register your technical profile, set repair rates, and apply for verification.'
                    : 'Join the DeviceCare 360 repair and diagnostics platform.'}
                </p>
              </div>

              {(validationError || error) && (
                <div className="alert alert-danger d-flex align-items-center small p-3 mb-3 rounded-3">
                  <AlertCircle size={20} className="me-2 flex-shrink-0" />
                  <div>{validationError || error}</div>
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label small fw-semibold">Account Type</label>
                  <select
                    name="role"
                    className="form-select"
                    value={formData.role}
                    onChange={handleChange}
                  >
                    <option value="USER">Standard User (Device Owner)</option>
                    <option value="TECHNICIAN">Certified Technician</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-semibold">Full Name</label>
                  <input
                    type="text"
                    name="fullName"
                    className="form-control"
                    placeholder="John Doe"
                    value={formData.fullName}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="row g-2 mb-3">
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Username</label>
                    <input
                      type="text"
                      name="username"
                      className="form-control"
                      placeholder="johndoe"
                      value={formData.username}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Phone Number</label>
                    <input
                      type="tel"
                      name="phone"
                      className="form-control"
                      placeholder="+94 77 123 4567"
                      value={formData.phone}
                      onChange={handleChange}
                      required={isTechnician}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label small fw-semibold">Email Address</label>
                  <input
                    type="email"
                    name="email"
                    className="form-control"
                    placeholder="john@example.com"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-4">
                  <label className="form-label small fw-semibold">Password</label>
                  <input
                    type="password"
                    name="password"
                    className="form-control"
                    placeholder="At least 6 characters"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    minLength={6}
                  />
                </div>

                {/* Technician Professional Information Section */}
                {isTechnician && (
                  <div className="p-4 bg-light rounded-4 border mb-4">
                    <div className="d-flex align-items-center gap-2 mb-3">
                      <ShieldCheck className="text-primary" size={22} />
                      <h5 className="fw-bold mb-0">Professional Information</h5>
                    </div>

                    <div className="row g-3 mb-3">
                      <div className="col-md-6">
                        <label className="form-label small fw-semibold">Years of Experience (0 - 50)</label>
                        <input
                          type="number"
                          name="experienceYears"
                          className="form-control"
                          min="0"
                          max="50"
                          value={formData.experienceYears}
                          onChange={handleChange}
                          required={isTechnician}
                        />
                      </div>
                      <div className="col-md-6">
                        <label className="form-label small fw-semibold">Base Diagnostic Fee / Hourly Rate (LKR)</label>
                        <div className="input-group">
                          <span className="input-group-text small fw-semibold">LKR</span>
                          <input
                            type="number"
                            name="hourlyRate"
                            className="form-control"
                            min="0"
                            step="50"
                            value={formData.hourlyRate}
                            onChange={handleChange}
                            required={isTechnician}
                          />
                        </div>
                      </div>
                    </div>

                    <div className="row g-3 mb-3">
                      <div className="col-md-6">
                        <label className="form-label small fw-semibold">Operating City</label>
                        <input
                          type="text"
                          name="city"
                          className="form-control"
                          placeholder="e.g. Colombo"
                          value={formData.city}
                          onChange={handleChange}
                          required={isTechnician}
                        />
                      </div>
                      <div className="col-md-6">
                        <label className="form-label small fw-semibold">Service Area / Province</label>
                        <input
                          type="text"
                          name="serviceArea"
                          className="form-control"
                          placeholder="e.g. Western Province"
                          value={formData.serviceArea}
                          onChange={handleChange}
                          required={isTechnician}
                        />
                      </div>
                    </div>

                    {/* Canonical Specializations Pills */}
                    <div className="mb-3">
                      <label className="form-label small fw-semibold d-block mb-2">
                        Device Specializations <span className="text-danger">*</span> (Select all that apply)
                      </label>
                      <div className="d-flex flex-wrap gap-2">
                        {CANONICAL_SPECIALIZATIONS.map(spec => {
                          const isSelected = formData.specializations.includes(spec.key);
                          return (
                            <button
                              type="button"
                              key={spec.key}
                              className={`btn btn-sm rounded-pill px-3 py-1.5 d-flex align-items-center gap-1.5 transition-all ${
                                isSelected ? 'btn-primary shadow-sm' : 'btn-outline-secondary'
                              }`}
                              onClick={() => handleSpecializationToggle(spec.key)}
                            >
                              {isSelected && <Check size={14} className="flex-shrink-0" />}
                              <span>{spec.label}</span>
                            </button>
                          );
                        })}
                      </div>
                      {formData.specializations.length === 0 && (
                        <div className="text-muted small mt-1.5 fst-italic">
                          Click to select at least one repair specialization.
                        </div>
                      )}
                    </div>

                    <div className="mb-3">
                      <label className="form-label small fw-semibold">Professional Bio & Experience Summary</label>
                      <textarea
                        name="professionalBio"
                        className="form-control"
                        rows="3"
                        placeholder="Detail your repair background, certifications, and technical proficiencies..."
                        value={formData.professionalBio}
                        onChange={handleChange}
                      />
                    </div>

                    <div className="alert alert-info small d-flex align-items-center gap-2 mb-0 rounded-3">
                      <ShieldCheck size={18} className="flex-shrink-0 text-primary" />
                      <div>
                        <strong>Credential Verification Notice:</strong> After creating your account, you will be directed to your Technician Workspace where you can securely upload your certification documents (PDF, JPG, PNG up to 5MB) for administrator review.
                      </div>
                    </div>
                  </div>
                )}

                <button type="submit" className="btn btn-primary w-100 py-2.5 fw-bold" disabled={loading}>
                  {loading ? 'Creating Account...' : isTechnician ? 'Register as Certified Technician' : 'Register Account'}
                </button>
              </form>

              <div className="text-center mt-4 text-muted small">
                Already have an account? <Link to="/login" className="fw-bold">Sign in</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default RegisterPage;
