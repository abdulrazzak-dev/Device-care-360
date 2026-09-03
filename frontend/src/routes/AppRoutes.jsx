import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

import ProtectedRoute from './ProtectedRoute';
import RoleBasedRoute from './RoleBasedRoute';

// Public Pages
import LandingPage from '../pages/public/LandingPage';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';

// User Pages
import DashboardPage from '../pages/user/DashboardPage';
import MyDevicesPage from '../pages/user/MyDevicesPage';
import ProfilePage from '../pages/user/ProfilePage';
import RepairHistoryPage from '../pages/user/RepairHistoryPage';
import NotificationsPage from '../pages/user/NotificationsPage';

// Troubleshooting & Guides Pages
import DiagnosticWizardPage from '../pages/troubleshooting/DiagnosticWizardPage';
import RepairGuideListPage from '../pages/repair-guides/RepairGuideListPage';

// Technician Pages
import TechnicianDirectoryPage from '../pages/technicians/TechnicianDirectoryPage';
import TechnicianDashboardPage from '../pages/technicians/TechnicianDashboardPage';

// Bookings & Payments
import CreateBookingPage from '../pages/bookings/CreateBookingPage';
import MyBookingsPage from '../pages/bookings/MyBookingsPage';
import PaymentPage from '../pages/payments/PaymentPage';

// Admin Pages
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import UserManagementPage from '../pages/admin/UserManagementPage';
import TechnicianManagementPage from '../pages/admin/TechnicianManagementPage';
import ReportsPage from '../pages/admin/ReportsPage';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/diagnose" element={<DiagnosticWizardPage />} />
      <Route path="/repair-guides" element={<RepairGuideListPage />} />
      <Route path="/technicians" element={<TechnicianDirectoryPage />} />

      {/* Protected User Routes */}
      <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      <Route path="/devices" element={<ProtectedRoute><MyDevicesPage /></ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
      <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />
      <Route path="/bookings" element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>} />
      <Route path="/bookings/new" element={<ProtectedRoute><CreateBookingPage /></ProtectedRoute>} />
      <Route path="/payments/:bookingId" element={<ProtectedRoute><PaymentPage /></ProtectedRoute>} />
      <Route path="/repair-history" element={<ProtectedRoute><RepairHistoryPage /></ProtectedRoute>} />

      {/* Protected Technician Routes */}
      <Route
        path="/technician/dashboard"
        element={
          <RoleBasedRoute allowedRoles={['TECHNICIAN', 'ADMIN']}>
            <TechnicianDashboardPage />
          </RoleBasedRoute>
        }
      />

      {/* Protected Admin Routes */}
      <Route
        path="/admin"
        element={
          <RoleBasedRoute allowedRoles={['ADMIN']}>
            <AdminDashboardPage />
          </RoleBasedRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <RoleBasedRoute allowedRoles={['ADMIN']}>
            <UserManagementPage />
          </RoleBasedRoute>
        }
      />
      <Route
        path="/admin/technicians"
        element={
          <RoleBasedRoute allowedRoles={['ADMIN']}>
            <TechnicianManagementPage />
          </RoleBasedRoute>
        }
      />
      <Route
        path="/admin/reports"
        element={
          <RoleBasedRoute allowedRoles={['ADMIN']}>
            <ReportsPage />
          </RoleBasedRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
