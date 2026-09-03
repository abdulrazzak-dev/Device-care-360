import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import Footer from '../../components/layout/Footer';
import { Cpu, ShieldAlert, Wrench, Smartphone, Award, CheckCircle2, ArrowRight } from 'lucide-react';

const LandingPage = () => {
  return (
    <div className="d-flex flex-column min-vh-100 bg-white">
      <Navbar />

      {/* Hero Section */}
      <section className="bg-dark text-white py-5 position-relative overflow-hidden">
        <div className="container py-5">
          <div className="row align-items-center g-5">
            <div className="col-lg-6">
              <span className="badge bg-primary text-white px-3 py-2 rounded-pill mb-3">
                <Cpu size={14} className="me-1" /> Next-Gen AI Troubleshooting
              </span>
              <h1 className="display-4 fw-extrabold text-white mb-3">
                Diagnose Smarter.<br />
                Repair Safer.<br />
                <span className="text-primary">Connect with Experts.</span>
              </h1>
              <p className="lead text-light mb-4 opacity-75">
                AI-powered safety diagnostics for your electronic devices. Get instant symptom analysis, risk-evaluated repair steps, or book verified technicians.
              </p>
              <div className="d-flex flex-wrap gap-3">
                <Link to="/diagnose" className="btn btn-primary btn-lg px-4 py-3 fw-bold d-flex align-items-center gap-2">
                  Start AI Diagnostic Wizard <ArrowRight size={20} />
                </Link>
                <Link to="/technicians" className="btn btn-outline-light btn-lg px-4 py-3 fw-bold">
                  Find Technicians
                </Link>
              </div>
            </div>
            <div className="col-lg-6 text-center">
              <div className="p-4 bg-secondary bg-opacity-25 rounded-4 border border-secondary shadow-lg">
                <div className="d-flex align-items-center justify-content-between border-bottom border-secondary pb-3 mb-3">
                  <div className="d-flex align-items-center gap-2 text-warning fw-bold">
                    <ShieldAlert size={24} /> Deterministic Safety Engine
                  </div>
                  <span className="badge bg-success">Active</span>
                </div>
                <div className="text-start text-light small">
                  <p className="mb-2">✓ Automatic high-voltage & battery thermal runaway evaluation.</p>
                  <p className="mb-2">✓ Overrides unsafe DIY instructions for critical electrical hazards.</p>
                  <p className="mb-0">✓ Seamlessly routes severe issues to certified local technicians.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-5 bg-light">
        <div className="container py-4">
          <div className="text-center max-w-xl mx-auto mb-5">
            <h2 className="fw-bold">How DeviceCare 360 Works</h2>
            <p className="text-muted">A seamless 6-step journey from symptom discovery to professional resolution.</p>
          </div>
          <div className="row g-4">
            {[
              { step: '01', title: 'Register Device', desc: 'Select category, brand, and model details.' },
              { step: '02', title: 'Describe Symptoms', desc: 'Pick common issues or enter custom observations.' },
              { step: '03', title: 'AI Safety Evaluation', desc: 'Gemini AI + Safety Engine evaluate physical hazard risks.' },
              { step: '04', title: 'Safe Step Guidance', desc: 'Follow verified non-hazardous DIY steps or safety warnings.' },
              { step: '05', title: 'Book Verified Tech', desc: 'For high-risk problems, connect directly with expert technicians.' },
              { step: '06', title: 'Track & Review', desc: 'Track appointment progress and submit verified booking reviews.' }
            ].map((item, idx) => (
              <div className="col-md-4" key={idx}>
                <div className="card h-100 p-4 border-0 shadow-sm card-hover">
                  <div className="fs-3 fw-bold text-primary mb-2">{item.step}</div>
                  <h5 className="fw-bold mb-2">{item.title}</h5>
                  <p className="text-muted small mb-0">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Supported Devices */}
      <section className="py-5">
        <div className="container py-4">
          <div className="text-center mb-5">
            <h2 className="fw-bold">Supported Device Categories</h2>
            <p className="text-muted">Comprehensive diagnostic knowledge base spanning consumer and home electronics.</p>
          </div>
          <div className="row g-3">
            {[
              'Smartphone', 'Laptop', 'Television', 'Refrigerator',
              'Washing Machine', 'Air Conditioner', 'Printer', 'Computer',
              'Tablet', 'Audio Device', 'Microwave', 'Other Electronics'
            ].map((cat, idx) => (
              <div className="col-md-3 col-6" key={idx}>
                <div className="p-3 bg-light rounded-3 text-center border font-weight-bold fw-semibold text-dark">
                  {cat}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default LandingPage;
