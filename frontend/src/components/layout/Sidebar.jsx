import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Smartphone, Cpu, Wrench, Calendar, CreditCard, Star, ShieldAlert, Users, FileText, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const Sidebar = ({ isMobile = false, onClose }) => {
  const { role } = useAuth();

  const handleLinkClick = () => {
    if (isMobile && onClose) {
      onClose();
    }
  };

  return (
    <aside 
      className={`bg-white border-end p-3 d-flex flex-column ${isMobile ? 'h-100' : 'min-vh-100 shadow-sm'}`} 
      style={{ width: isMobile ? '100%' : '260px' }}
    >
      <div className="d-flex align-items-center justify-content-between mb-3 px-2">
        <div className="text-uppercase small fw-bold text-muted">Navigation</div>
        {isMobile && (
          <button 
            type="button" 
            className="btn btn-sm btn-light border-0 p-1 rounded-circle"
            onClick={onClose}
            aria-label="Close navigation sidebar"
          >
            <X size={20} className="text-muted" />
          </button>
        )}
      </div>

      <div className="nav flex-column nav-pills gap-1 flex-grow-1">
        {role === 'ADMIN' ? (
          <>
            <NavLink to="/admin" end className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <LayoutDashboard size={18} className="flex-shrink-0" /> <span className="text-truncate">Admin Overview</span>
            </NavLink>
            <NavLink to="/admin/users" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Users size={18} className="flex-shrink-0" /> <span className="text-truncate">User Management</span>
            </NavLink>
            <NavLink to="/admin/technicians" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Wrench size={18} className="flex-shrink-0" /> <span className="text-truncate">Technician Approvals</span>
            </NavLink>
            <NavLink to="/admin/reports" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <FileText size={18} className="flex-shrink-0" /> <span className="text-truncate">Reports & Analytics</span>
            </NavLink>
          </>
        ) : role === 'TECHNICIAN' ? (
          <>
            <NavLink to="/technician/dashboard" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <LayoutDashboard size={18} className="flex-shrink-0" /> <span className="text-truncate">Tech Dashboard</span>
            </NavLink>
            <NavLink to="/bookings" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Calendar size={18} className="flex-shrink-0" /> <span className="text-truncate">Assigned Jobs</span>
            </NavLink>
          </>
        ) : (
          <>
            <NavLink to="/dashboard" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <LayoutDashboard size={18} className="flex-shrink-0" /> <span className="text-truncate">User Dashboard</span>
            </NavLink>
            <NavLink to="/diagnose" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Cpu size={18} className="flex-shrink-0" /> <span className="text-truncate">AI Diagnose</span>
            </NavLink>
            <NavLink to="/devices" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Smartphone size={18} className="flex-shrink-0" /> <span className="text-truncate">My Devices</span>
            </NavLink>
            <NavLink to="/technicians" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Wrench size={18} className="flex-shrink-0" /> <span className="text-truncate">Book Technician</span>
            </NavLink>
            <NavLink to="/bookings" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <Calendar size={18} className="flex-shrink-0" /> <span className="text-truncate">My Appointments</span>
            </NavLink>
            <NavLink to="/repair-history" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`} onClick={handleLinkClick}>
              <FileText size={18} className="flex-shrink-0" /> <span className="text-truncate">Repair History</span>
            </NavLink>
          </>
        )}
      </div>
    </aside>
  );
};

export default Sidebar;
