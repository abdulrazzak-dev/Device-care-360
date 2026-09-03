import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Shield, Bell, User, LogOut, Cpu } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { unreadCount } = useNotification();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm py-2">
      <div className="container">
        <Link className="navbar-brand d-flex align-items-center fw-bold text-white fs-4" to="/">
          <Cpu className="text-primary me-2" size={28} />
          DeviceCare <span className="text-primary ms-1">360</span>
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarContent"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarContent">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0 ms-lg-4">
            <li className="nav-item">
              <Link className="nav-link text-light" to="/diagnose">AI Troubleshoot</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link text-light" to="/technicians">Technicians</Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link text-light" to="/repair-guides">Repair Guides</Link>
            </li>
          </ul>

          <div className="d-flex align-items-center gap-3">
            {isAuthenticated ? (
              <>
                <Link to="/notifications" className="btn btn-outline-secondary position-relative p-2 text-white border-0">
                  <Bell size={20} />
                  {unreadCount > 0 && (
                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                      {unreadCount}
                    </span>
                  )}
                </Link>

                <div className="dropdown">
                  <button
                    className="btn btn-outline-light dropdown-toggle d-flex align-items-center gap-2"
                    type="button"
                    data-bs-toggle="dropdown"
                  >
                    <User size={18} />
                    <span>{user?.username || 'Account'}</span>
                  </button>
                  <ul className="dropdown-menu dropdown-menu-end shadow">
                    <li>
                      <Link className="dropdown-item" to={user?.role === 'ADMIN' ? '/admin' : user?.role === 'TECHNICIAN' ? '/technician/dashboard' : '/dashboard'}>
                        Dashboard ({user?.role})
                      </Link>
                    </li>
                    <li><Link className="dropdown-item" to="/profile">My Profile</Link></li>
                    <li><Link className="dropdown-item" to="/devices">My Devices</Link></li>
                    <li><Link className="dropdown-item" to="/bookings">My Bookings</Link></li>
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                      <button className="dropdown-item text-danger d-flex align-items-center gap-2" onClick={handleLogout}>
                        <LogOut size={16} /> Logout
                      </button>
                    </li>
                  </ul>
                </div>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-outline-light btn-sm">Sign In</Link>
                <Link to="/register" className="btn btn-primary btn-sm">Get Started</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
