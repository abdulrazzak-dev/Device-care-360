import React from 'react';

const RiskBadge = ({ riskLevel }) => {
  const level = (riskLevel || 'LOW').toUpperCase();

  const getBadgeClass = () => {
    switch (level) {
      case 'CRITICAL':
        return 'badge-risk-critical';
      case 'HIGH':
        return 'badge-risk-high';
      case 'MEDIUM':
        return 'badge-risk-medium';
      case 'LOW':
      default:
        return 'badge-risk-low';
    }
  };

  return (
    <span className={`badge px-3 py-2 fw-semibold rounded-pill ${getBadgeClass()}`}>
      Risk Level: {level}
    </span>
  );
};

export default RiskBadge;
