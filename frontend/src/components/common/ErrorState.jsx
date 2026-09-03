import React from 'react';
import { AlertCircle } from 'lucide-react';

const ErrorState = ({ title = 'Something Went Wrong', message = 'Unable to fetch data from the server.', onRetry }) => {
  return (
    <div className="alert alert-danger text-center p-4 rounded-3 my-3">
      <AlertCircle size={36} className="text-danger mb-2" />
      <h6 className="fw-bold">{title}</h6>
      <p className="small mb-3">{message}</p>
      {onRetry && (
        <button className="btn btn-outline-danger btn-sm" onClick={onRetry}>
          Try Again
        </button>
      )}
    </div>
  );
};

export default ErrorState;
