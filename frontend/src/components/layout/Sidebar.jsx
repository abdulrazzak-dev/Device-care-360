import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Smartphone, Cpu, Wrench, Calendar, CreditCard, Star, ShieldAlert, Users, FileText } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const Sidebar = () => {
  const { role } = useAuth();

  return (
    <div className="bg-white border-end p-3 min-vh-100 shadow-sm" style={{ width: '260px' }}>
      <div className="text-uppercase small fw-bold text-muted mb-3 px-2">Navigation</div>
      <div className="nav flex-column nav-pills gap-1">
        {role === 'ADMIN' ? (
          <>
            <NavLink to="/admin" end className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <LayoutDashboard size={18} /> Admin Overview
            </NavLink>
            <NavLink to="/admin/users" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Users size={18} /> User Management
            </NavLink>
            <NavLink to="/admin/technicians" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Wrench size={18} /> Technician Approvals
            </NavLink>
            <NavLink to="/admin/reports" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <FileText size={18} /> Reports & Analytics
            </NavLink>
          </>
        ) : role === 'TECHNICIAN' ? (
          <>
            <NavLink to="/technician/dashboard" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <LayoutDashboard size={18} /> Tech Dashboard
            </NavLink>
            <NavLink to="/bookings" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Calendar size={18} /> Assigned Jobs
            </NavLink>
          </>
        ) : (
          <>
            <NavLink to="/dashboard" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <LayoutDashboard size={18} /> User Dashboard
            </NavLink>
            <NavLink to="/diagnose" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Cpu size={18} /> AI Diagnose
            </NavLink>
            <NavLink to="/devices" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Smartphone size={18} /> My Devices
            </NavLink>
            <NavLink to="/technicians" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Wrench size={18} /> Book Technician
            </NavLink>
            <NavLink to="/bookings" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <Calendar size={18} /> My Appointments
            </NavLink>
            <NavLink to="/repair-history" className={({ isActive }) => `nav-link d-flex align-items-center gap-2 ${isActive ? 'active' : 'text-dark'}`}>
              <FileText size={18} /> Repair History
            </NavLink>
          </>
        )}
      </div>
    </div>
  );
};

export default Sidebar;
