import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { userService } from '../../services/userService';
import { technicianService } from '../../services/technicianService';
import { User, Mail, Phone, MapPin, Wrench, ShieldCheck, CheckCircle2 } from 'lucide-react';

const ProfilePage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({ fullName: '', phone: '', address: '', preferredLanguage: 'English' });
  const [techProfile, setTechProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statusMsg, setStatusMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const isTechnician = user?.role === 'TECHNICIAN';

  useEffect(() => {
    if (user?.userId) {
      const promises = [
        userService.getUserById(user.userId).catch(() => ({ data: {} }))
      ];
      if (isTechnician) {
        promises.push(
          technicianService.getMyProfile().catch(() => technicianService.getById(user.userId)).catch(() => ({ data: null }))
        );
      }

      Promise.all(promises).then(([userRes, techRes]) => {
        setProfile(userRes?.data || {});
        if (techRes?.data) {
          setTechProfile(techRes.data);
        }
      }).finally(() => setLoading(false));
    }
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatusMsg('');
    setErrorMsg('');
    try {
      await userService.updateProfile(user.userId, profile);

      // If user is a technician, sync matching fields with technician-service
      if (isTechnician) {
        await technicianService.updateMyProfile({
          fullName: profile.fullName,
          phone: profile.phone,
          serviceArea: profile.address
        }).catch(() => {});
      }

      setStatusMsg('Profile changes saved successfully.');
    } catch (err) {
      setErrorMsg('Failed to update profile: ' + (err.message || 'Unknown error'));
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-lg mx-auto">
        {/* Technician Banner if user is a technician */}
        {isTechnician && (
          <div className="card p-3 border-0 shadow-sm rounded-4 mb-4 bg-primary bg-opacity-10 border-start border-4 border-primary">
            <div className="d-flex align-items-start justify-content-between gap-3">
              <div className="d-flex align-items-center gap-2.5">
                <Wrench className="text-primary flex-shrink-0" size={24} />
                <div>
                  <h6 className="fw-bold mb-0 text-dark">Certified Technician Account</h6>
                  <p className="text-muted extra-small mb-0">
                    Verification Status: <span className="fw-bold text-uppercase">{techProfile?.verificationStatus || 'PENDING'}</span>
                  </p>
                </div>
              </div>
              <Link to="/technician/dashboard" className="btn btn-sm btn-primary py-1 px-2.5 fw-semibold flex-shrink-0">
                Tech Dashboard
              </Link>
            </div>
          </div>
        )}

        <div className="card p-4 border-0 shadow-sm rounded-4">
          <h4 className="fw-bold mb-3 d-flex align-items-center gap-2">
            <User className="text-primary" size={24} /> Profile Settings
          </h4>

          {statusMsg && (
            <div className="alert alert-success small py-2 px-3 mb-3 rounded-3 d-flex align-items-center gap-2">
              <CheckCircle2 size={16} />
              <span>{statusMsg}</span>
            </div>
          )}

          {errorMsg && (
            <div className="alert alert-danger small py-2 px-3 mb-3 rounded-3">
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label small fw-semibold">Full Name</label>
              <input 
                type="text" 
                className="form-control" 
                value={profile.fullName || ''} 
                onChange={e => setProfile({ ...profile, fullName: e.target.value })} 
                required 
              />
            </div>

            <div className="mb-3">
              <label className="form-label small fw-semibold">Phone Number</label>
              <input 
                type="tel" 
                className="form-control" 
                value={profile.phone || ''} 
                onChange={e => setProfile({ ...profile, phone: e.target.value })} 
              />
            </div>

            <div className="mb-4">
              <label className="form-label small fw-semibold">Address / Service Area</label>
              <textarea 
                className="form-control" 
                rows={3} 
                value={profile.address || ''} 
                onChange={e => setProfile({ ...profile, address: e.target.value })} 
              />
            </div>

            <button type="submit" className="btn btn-primary w-100 py-2.5 fw-bold">
              Save Profile Changes
            </button>
          </form>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ProfilePage;
