import React from 'react';
import { Inbox } from 'lucide-react';

const EmptyState = ({ title = 'No Data Found', message = 'There are no records to display at this time.', actionLabel, onAction }) => {
  return (
    <div className="text-center p-5 card border-dashed">
      <div className="bg-light d-inline-flex p-3 rounded-circle mb-3 mx-auto">
        <Inbox size={40} className="text-muted" />
      </div>
      <h5 className="fw-bold text-dark">{title}</h5>
      <p className="text-muted small mx-auto" style={{ maxWidth: '400px' }}>{message}</p>
      {actionLabel && onAction && (
        <button className="btn btn-primary btn-sm mt-2" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
};

export default EmptyState;
