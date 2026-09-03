import React from 'react';

const StatusBadge = ({ status }) => {
  const s = (status || 'PENDING').toUpperCase();

  const getClass = () => {
    switch (s) {
      case 'CONFIRMED':
      case 'PAID':
      case 'COMPLETED':
      case 'VERIFIED':
      case 'ACTIVE':
        return 'bg-success text-white';
      case 'IN_PROGRESS':
      case 'PROCESSING':
        return 'bg-info text-dark';
      case 'PENDING':
        return 'bg-warning text-dark';
      case 'CANCELLED':
      case 'FAILED':
      case 'REJECTED':
      case 'DELETED':
        return 'bg-danger text-white';
      default:
        return 'bg-secondary text-white';
    }
  };

  return <span className={`badge ${getClass()} px-2.5 py-1.5 rounded-pill`}>{s}</span>;
};

export default StatusBadge;
