import React from 'react';
import { Zap } from 'lucide-react';

const HighVoltageWarning = () => {
  return (
    <div className="alert alert-danger d-flex align-items-center rounded-3 shadow-sm border-danger p-3 my-3" role="alert">
      <Zap size={32} className="me-3 flex-shrink-0 text-danger" />
      <div>
        <h6 className="fw-bold mb-1">DANGER: High Voltage / Fire Hazard Identified</h6>
        <p className="mb-0 small">
          This device condition carries extreme risk of electrocution, battery explosion, or structural damage. Disconnect mains power immediately and seek certified professional technician service.
        </p>
      </div>
    </div>
  );
};

export default HighVoltageWarning;
