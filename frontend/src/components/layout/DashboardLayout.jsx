import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import Footer from './Footer';

const DashboardLayout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  // Automatically close mobile sidebar whenever the route changes
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  // Prevent background scrolling when mobile sidebar is open
  useEffect(() => {
    if (sidebarOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [sidebarOpen]);

  return (
    <div className="d-flex flex-column min-vh-100 bg-light">
      <Navbar onToggleSidebar={() => setSidebarOpen((prev) => !prev)} isDashboard={true} />

      <div className="d-flex flex-grow-1 position-relative w-100" style={{ minWidth: 0 }}>
        {/* Desktop Fixed/Static Sidebar (>= 992px) */}
        <div className="d-none d-lg-block flex-shrink-0">
          <Sidebar />
        </div>

        {/* Mobile Sidebar Overlay Backdrop (< 992px) */}
        {sidebarOpen && (
          <div
            className="sidebar-backdrop d-lg-none position-fixed top-0 start-0 w-100 h-100 bg-dark bg-opacity-50"
            style={{ zIndex: 1040, transition: 'opacity 0.2s ease' }}
            onClick={() => setSidebarOpen(false)}
            aria-hidden="true"
          />
        )}

        {/* Mobile Slide-out Drawer (< 992px) */}
        <div
          className="sidebar-mobile-drawer d-lg-none position-fixed top-0 start-0 h-100 bg-white shadow-lg"
          style={{
            zIndex: 1050,
            width: '280px',
            maxWidth: '85vw',
            transform: sidebarOpen ? 'translateX(0)' : 'translateX(-100%)',
            transition: 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            overflowY: 'auto'
          }}
        >
          <Sidebar isMobile={true} onClose={() => setSidebarOpen(false)} />
        </div>

        {/* Main Content (100% full-width on mobile, flex-1 on desktop) */}
        <main className="flex-grow-1 p-3 p-sm-4 w-100 overflow-x-hidden" style={{ minWidth: 0 }}>
          <div className="container-fluid px-0 px-sm-2">{children}</div>
        </main>
      </div>

      <Footer />
    </div>
  );
};

export default DashboardLayout;
