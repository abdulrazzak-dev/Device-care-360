import React, { useState, useEffect } from 'react';
import RiskBadge from '../../components/safety/RiskBadge';
import {
  X,
  Clock,
  Wrench,
  ShieldAlert,
  CheckCircle2,
  Circle,
  ExternalLink,
  Printer,
  RotateCcw,
  BookOpen,
  Info
} from 'lucide-react';

const RepairGuideModal = ({ guide, isOpen, onClose }) => {
  const [checkedTools, setCheckedTools] = useState({});
  const [completedSteps, setCompletedSteps] = useState({});

  useEffect(() => {
    // Reset checklists when viewing a different guide
    setCheckedTools({});
    setCompletedSteps({});
  }, [guide?.id]);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen || !guide) return null;

  const tools = guide.toolsRequired || [];
  const instructions = guide.safeInstructions || guide.steps || [];
  const warnings = guide.safetyWarnings || [];
  const tips = guide.maintenanceTips || [];
  const difficulty = guide.difficulty || guide.skillLevel || 'INTERMEDIATE';
  const estimatedTime = guide.estimatedTime || '30-45 minutes';

  const toggleTool = (idx) => {
    setCheckedTools((prev) => ({
      ...prev,
      [idx]: !prev[idx]
    }));
  };

  const toggleAllTools = () => {
    const allChecked = tools.length > 0 && tools.every((_, idx) => checkedTools[idx]);
    if (allChecked) {
      setCheckedTools({});
    } else {
      const next = {};
      tools.forEach((_, idx) => {
        next[idx] = true;
      });
      setCheckedTools(next);
    }
  };

  const toggleStep = (idx) => {
    setCompletedSteps((prev) => ({
      ...prev,
      [idx]: !prev[idx]
    }));
  };

  const checkedCount = Object.values(checkedTools).filter(Boolean).length;
  const stepsCompletedCount = Object.values(completedSteps).filter(Boolean).length;

  return (
    <div
      className="modal fade show d-block"
      tabIndex="-1"
      role="dialog"
      aria-modal="true"
      style={{ backgroundColor: 'rgba(15, 23, 42, 0.65)', backdropFilter: 'blur(4px)', zIndex: 1055 }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="modal-dialog modal-lg modal-dialog-scrollable modal-dialog-centered my-4" role="document">
        <div className="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
          {/* Modal Header */}
          <div className="modal-header border-bottom bg-light px-4 py-3 d-flex align-items-center justify-content-between">
            <div className="d-flex align-items-center gap-2 flex-wrap">
              <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-1 rounded-pill fw-semibold">
                {guide.category}
              </span>
              <RiskBadge riskLevel={guide.riskLevel} />
              <span className="badge bg-secondary bg-opacity-10 text-secondary px-3 py-1 rounded-pill d-inline-flex align-items-center gap-1">
                <Clock size={13} /> {estimatedTime}
              </span>
              <span className="badge bg-dark bg-opacity-10 text-dark px-3 py-1 rounded-pill">
                Skill: {difficulty}
              </span>
            </div>
            <button
              type="button"
              className="btn-close shadow-none"
              aria-label="Close"
              onClick={onClose}
            />
          </div>

          {/* Modal Body */}
          <div className="modal-body px-4 py-4" style={{ maxHeight: 'calc(85vh - 140px)' }}>
            {/* Title & Brand */}
            <div className="mb-4">
              <h4 className="fw-bold text-dark mb-1">{guide.title}</h4>
              <p className="text-muted small mb-0">
                <strong className="text-secondary">Applies to:</strong> {guide.brand || 'Multi-Brand / Universal Models'}
              </p>
              {(guide.issue || guide.description) && (
                <div className="mt-2 p-3 bg-light rounded-3 text-secondary small">
                  <strong className="text-dark">Reported Symptom: </strong>
                  {guide.issue || guide.description}
                </div>
              )}
            </div>

            {/* Safety Warnings Callout */}
            {warnings.length > 0 && (
              <div className="card border-danger-subtle bg-danger bg-opacity-10 p-3 mb-4 rounded-3 shadow-none">
                <div className="d-flex align-items-center gap-2 mb-2 text-danger">
                  <ShieldAlert size={20} className="flex-shrink-0" />
                  <h6 className="fw-bold mb-0">Crucial Safety & Hazard Protocols</h6>
                </div>
                <ul className="mb-0 ps-3 small text-danger-emphasis">
                  {warnings.map((warn, idx) => (
                    <li key={idx} className="mb-1">{warn}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* Interactive Tools Checklist */}
            {tools.length > 0 && (
              <div className="card border p-3 mb-4 rounded-3 bg-white shadow-none">
                <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
                  <div className="d-flex align-items-center gap-2">
                    <Wrench size={18} className="text-primary" />
                    <h6 className="fw-bold mb-0 text-dark">Required Tools & Consumables</h6>
                    <span className="badge bg-primary bg-opacity-10 text-primary rounded-pill small">
                      {checkedCount} of {tools.length} ready
                    </span>
                  </div>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-secondary d-inline-flex align-items-center gap-1 py-1 px-2"
                    onClick={toggleAllTools}
                  >
                    <RotateCcw size={12} />
                    {tools.every((_, idx) => checkedTools[idx]) ? 'Uncheck All' : 'Check All Tools'}
                  </button>
                </div>

                <div className="row g-2">
                  {tools.map((tool, idx) => {
                    const isChecked = !!checkedTools[idx];
                    return (
                      <div className="col-12 col-md-6" key={idx}>
                        <div
                          className={`d-flex align-items-center p-2 rounded-2 border transition-all cursor-pointer ${
                            isChecked ? 'bg-success bg-opacity-10 border-success-subtle text-success-emphasis' : 'bg-light border-light-subtle text-dark'
                          }`}
                          onClick={() => toggleTool(idx)}
                          style={{ cursor: 'pointer', userSelect: 'none' }}
                        >
                          <div className="me-2 flex-shrink-0">
                            {isChecked ? (
                              <CheckCircle2 size={18} className="text-success" />
                            ) : (
                              <Circle size={18} className="text-muted" />
                            )}
                          </div>
                          <span className={`small ${isChecked ? 'text-decoration-line-through text-muted' : 'fw-medium'}`}>
                            {tool}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Step-by-Step Instructions */}
            <div className="mb-4">
              <div className="d-flex align-items-center justify-content-between mb-3">
                <div className="d-flex align-items-center gap-2">
                  <BookOpen size={18} className="text-primary" />
                  <h6 className="fw-bold mb-0 text-dark">Standard Operating Procedure</h6>
                </div>
                {instructions.length > 0 && (
                  <span className="text-muted small">
                    {stepsCompletedCount} of {instructions.length} steps completed
                  </span>
                )}
              </div>

              <div className="d-flex flex-column gap-3">
                {instructions.map((step, idx) => {
                  const isDone = !!completedSteps[idx];
                  const stepNumber = idx + 1;
                  // Split "Title: Description" if colon exists
                  const colonIdx = step.indexOf(':');
                  const hasPrefix = colonIdx > 0 && colonIdx < 40;
                  const stepTitle = hasPrefix ? step.substring(0, colonIdx) : null;
                  const stepBody = hasPrefix ? step.substring(colonIdx + 1).trim() : step;

                  return (
                    <div
                      key={idx}
                      className={`card border p-3 rounded-3 shadow-none transition-all ${
                        isDone ? 'bg-light border-success-subtle' : 'bg-white border-light-subtle'
                      }`}
                    >
                      <div className="d-flex align-items-start gap-3">
                        <button
                          type="button"
                          className={`btn p-0 border-0 flex-shrink-0 mt-1 ${isDone ? 'text-success' : 'text-primary'}`}
                          onClick={() => toggleStep(idx)}
                          title={isDone ? 'Mark as incomplete' : 'Mark as completed'}
                        >
                          {isDone ? (
                            <CheckCircle2 size={24} className="text-success" />
                          ) : (
                            <div
                              className="rounded-circle bg-primary bg-opacity-10 text-primary fw-bold d-flex align-items-center justify-content-center"
                              style={{ width: 28, height: 28, fontSize: '0.85rem' }}
                            >
                              {stepNumber}
                            </div>
                          )}
                        </button>

                        <div className="flex-grow-1" onClick={() => toggleStep(idx)} style={{ cursor: 'pointer' }}>
                          {stepTitle && (
                            <h6 className={`fw-bold mb-1 small ${isDone ? 'text-decoration-line-through text-muted' : 'text-dark'}`}>
                              {stepTitle}
                            </h6>
                          )}
                          <p className={`mb-0 small ${isDone ? 'text-muted text-decoration-line-through' : 'text-secondary'}`}>
                            {stepBody}
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Maintenance Tips */}
            {tips.length > 0 && (
              <div className="card border-info-subtle bg-info bg-opacity-10 p-3 mb-3 rounded-3 shadow-none">
                <div className="d-flex align-items-center gap-2 mb-2 text-info-emphasis">
                  <Info size={18} className="flex-shrink-0" />
                  <h6 className="fw-bold mb-0">Preventive Maintenance & Longevity Tips</h6>
                </div>
                <ul className="mb-0 ps-3 small text-secondary">
                  {tips.map((tip, idx) => (
                    <li key={idx} className="mb-1">{tip}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          {/* Modal Footer */}
          <div className="modal-footer border-top bg-light px-4 py-3 d-flex align-items-center justify-content-between flex-wrap gap-2">
            <div>
              {guide.officialDocumentationUrl && (
                <a
                  href={guide.officialDocumentationUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-outline-secondary btn-sm d-inline-flex align-items-center gap-1"
                >
                  <ExternalLink size={14} /> Official Reference Documentation
                </a>
              )}
            </div>

            <div className="d-flex align-items-center gap-2">
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm d-inline-flex align-items-center gap-1"
                onClick={() => window.print()}
              >
                <Printer size={14} /> Print Manual
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm px-4"
                onClick={onClose}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RepairGuideModal;
