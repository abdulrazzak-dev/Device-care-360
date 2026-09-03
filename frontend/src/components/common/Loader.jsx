import React from 'react';

const Loader = ({ text = 'Loading...' }) => {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center p-5">
      <div className="spinner-border text-primary" style={{ width: '3rem', height: '3rem' }} role="status">
        <span className="visually-hidden">{text}</span>
      </div>
      <p className="mt-3 text-muted fw-medium">{text}</p>
    </div>
  );
};

export default Loader;
