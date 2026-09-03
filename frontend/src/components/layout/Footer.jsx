import React from 'react';
import { Cpu, ShieldCheck } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="bg-dark text-light py-5 mt-auto border-top border-secondary">
      <div className="container">
        <div className="row g-4">
          <div className="col-lg-4">
            <div className="d-flex align-items-center fw-bold fs-4 text-white mb-2">
              <Cpu className="text-primary me-2" size={28} />
              DeviceCare <span className="text-primary ms-1">360</span>
            </div>
            <p className="text-muted small">
              AI-Powered Electronic Device Troubleshooting, Safety Engineering & Certified Technician Booking Platform.
            </p>
            <div className="d-flex align-items-center text-success small">
              <ShieldCheck size={18} className="me-2" /> Deterministic Electrical Safety Engine Active
            </div>
          </div>
          <div className="col-lg-2 col-6">
            <h6 className="fw-bold text-white mb-3">Platform</h6>
            <ul className="list-unstyled small text-muted">
              <li><a href="/diagnose" className="text-muted">AI Troubleshoot</a></li>
              <li><a href="/technicians" className="text-muted">Find Technicians</a></li>
              <li><a href="/repair-guides" className="text-muted">Repair Guides</a></li>
            </ul>
          </div>
          <div className="col-lg-2 col-6">
            <h6 className="fw-bold text-white mb-3">Account</h6>
            <ul className="list-unstyled small text-muted">
              <li><a href="/dashboard" className="text-muted">Dashboard</a></li>
              <li><a href="/devices" className="text-muted">My Devices</a></li>
              <li><a href="/bookings" className="text-muted">My Bookings</a></li>
            </ul>
          </div>
          <div className="col-lg-4">
            <h6 className="fw-bold text-white mb-3">Safety Disclaimer</h6>
            <p className="text-muted small mb-0">
              Never attempt repairs involving high-voltage mains power, swollen lithium batteries, or pressure gas lines without qualified professional technicians.
            </p>
          </div>
        </div>
        <hr className="border-secondary my-4" />
        <div className="text-center text-muted small">
          © {new Date().getFullYear()} DeviceCare 360 Inc. All rights reserved.
        </div>
      </div>
    </footer>
  );
};

export default Footer;
