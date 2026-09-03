import React from 'react';
import { AlertTriangle, ShieldAlert } from 'lucide-react';

const SafetyAlert = ({ title, message, warnings = [], doNotAttempt = [] }) => {
  return (
    <div className="critical-hazard-banner my-4">
      <div className="d-flex align-items-center mb-3">
        <ShieldAlert size={36} className="me-3 text-warning" />
        <div>
          <h4 className="fw-bold mb-0 text-white">{title || 'CRITICAL SAFETY HAZARD DETECTED'}</h4>
          <p className="mb-0 text-light small">{message || 'Immediate action required to prevent physical harm or electrical danger.'}</p>
        </div>
      </div>

      {warnings.length > 0 && (
        <div className="bg-black bg-opacity-25 p-3 rounded-3 mb-3">
          <h6 className="fw-bold text-warning mb-2">Safety Warnings:</h6>
          <ul className="mb-0 ps-3 small text-white">
            {warnings.map((w, idx) => (
              <li key={idx}>{w}</li>
            ))}
          </ul>
        </div>
      )}

      {doNotAttempt.length > 0 && (
        <div className="bg-danger bg-opacity-50 p-3 rounded-3">
          <h6 className="fw-bold text-white mb-2">Strictly Do NOT Attempt:</h6>
          <ul className="mb-0 ps-3 small text-white">
            {doNotAttempt.map((dna, idx) => (
              <li key={idx}>{dna}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default SafetyAlert;
