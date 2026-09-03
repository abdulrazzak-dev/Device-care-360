import React, { useState, useEffect } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { userService } from '../../services/userService';
import { User, Mail, Phone, MapPin } from 'lucide-react';

const ProfilePage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({ fullName: '', phone: '', address: '', preferredLanguage: 'English' });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.userId) {
      userService.getUserById(user.userId)
        .then(res => setProfile(res.data || {}))
        .catch(() => {})
        .finally(() => setLoading(false));
    }
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await userService.updateProfile(user.userId, profile);
      alert('Profile updated successfully!');
    } catch (err) {
      alert('Failed to update profile: ' + err.message);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-lg mx-auto card p-4 border-0 shadow-sm rounded-4">
        <h4 className="fw-bold mb-3 d-flex align-items-center gap-2">
          <User className="text-primary" size={24} /> Profile Settings
        </h4>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label small fw-semibold">Full Name</label>
            <input type="text" className="form-control" value={profile.fullName || ''} onChange={e => setProfile({ ...profile, fullName: e.target.value })} required />
          </div>

          <div className="mb-3">
            <label className="form-label small fw-semibold">Phone Number</label>
            <input type="tel" className="form-control" value={profile.phone || ''} onChange={e => setProfile({ ...profile, phone: e.target.value })} />
          </div>

          <div className="mb-4">
            <label className="form-label small fw-semibold">Address / Service Area</label>
            <textarea className="form-control" rows={3} value={profile.address || ''} onChange={e => setProfile({ ...profile, address: e.target.value })}></textarea>
          </div>

          <button type="submit" className="btn btn-primary w-100 py-2.5 fw-bold">
            Save Profile Changes
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
};

export default ProfilePage;
