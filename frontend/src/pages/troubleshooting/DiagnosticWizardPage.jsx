import React, { useState, useEffect, useCallback } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { deviceService } from '../../services/deviceService';
import { troubleshootingService } from '../../services/troubleshootingService';
import SafetyAlert from '../../components/safety/SafetyAlert';
import RiskBadge from '../../components/safety/RiskBadge';
import HighVoltageWarning from '../../components/safety/HighVoltageWarning';
import Loader from '../../components/common/Loader';
import { Cpu, ArrowRight, ArrowLeft, AlertTriangle, Wrench } from 'lucide-react';
import { Link } from 'react-router-dom';

const DEFAULT_CATEGORIES = [
  'Smartphone', 'Laptop', 'Television', 'Refrigerator',
  'Washing Machine', 'Air Conditioner', 'Printer', 'Computer',
  'Tablet', 'Audio Device', 'Other'
];

const DEFAULT_BRANDS = ['Apple', 'Samsung', 'Dell', 'HP', 'LG', 'Sony', 'Lenovo', 'Other'];

const DEFAULT_ISSUES = [
  "Won't turn on", 'Battery draining fast', 'Overheating',
  'No display / Black screen', 'Water damage', 'Unusual noise',
  'Smoke / Burning smell', 'Swollen battery'
];

const DiagnosticWizardPage = () => {
  const [step, setStep] = useState(1);
  const [categories, setCategories] = useState(DEFAULT_CATEGORIES);
  const [brands, setBrands] = useState([]);
  const [commonIssues, setCommonIssues] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('Smartphone');
  const [selectedBrand, setSelectedBrand] = useState('');
  const [model, setModel] = useState('');
  const [selectedIssue, setSelectedIssue] = useState('');
  const [customIssue, setCustomIssue] = useState('');
  
  const [loadingMetadata, setLoadingMetadata] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  // Cache for loaded brands and issues per category to avoid duplicate network calls
  const [metadataCache, setMetadataCache] = useState({});

  useEffect(() => {
    let isMounted = true;
    deviceService.getCategories()
      .then((res) => {
        if (isMounted && res.data && Array.isArray(res.data) && res.data.length > 0) {
          setCategories(res.data);
        }
      })
      .catch((err) => {
        console.warn('Could not load categories from server, using defaults:', err.message);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const fetchCategoryDetails = useCallback(async (cat) => {
    if (metadataCache[cat]) {
      setBrands(metadataCache[cat].brands || []);
      setCommonIssues(metadataCache[cat].issues || []);
      return;
    }

    setLoadingMetadata(true);
    try {
      const [brandsRes, issuesRes] = await Promise.allSettled([
        deviceService.getBrands(cat),
        deviceService.getCommonIssues(cat)
      ]);

      const fetchedBrands = brandsRes.status === 'fulfilled' && brandsRes.value?.data ? brandsRes.value.data : [];
      const fetchedIssues = issuesRes.status === 'fulfilled' && issuesRes.value?.data ? issuesRes.value.data : [];

      setBrands(fetchedBrands);
      setCommonIssues(fetchedIssues);

      setMetadataCache((prev) => ({
        ...prev,
        [cat]: {
          brands: fetchedBrands,
          issues: fetchedIssues
        }
      }));
    } finally {
      setLoadingMetadata(false);
    }
  }, [metadataCache]);

  const handleSelectCategory = (cat) => {
    setSelectedCategory(cat);
    setErrorMessage('');
    fetchCategoryDetails(cat);
    setStep(2);
  };

  const handleSelectBrand = (brand) => {
    setSelectedBrand(brand);
    setErrorMessage('');
    setStep(3);
  };

  const handleStep3Next = (e) => {
    e.preventDefault();
    setErrorMessage('');
    setStep(4);
  };

  const handleRunAnalysis = async () => {
    if (analyzing) return;

    setErrorMessage('');
    setAnalyzing(true);
    setStep(5);

    const finalProblem = (customIssue && customIssue.trim()) || selectedIssue || 'Device malfunction';
    try {
      const res = await troubleshootingService.analyze({
        category: selectedCategory,
        brand: selectedBrand || 'Generic',
        model: model.trim(),
        issueDescription: finalProblem
      });

      if (res && res.data) {
        setAnalysisResult(res.data);
        setStep(6);
      } else {
        throw new Error('No diagnostic data returned from the analysis service.');
      }
    } catch (err) {
      console.error('Diagnosis submission failed:', err);
      const msg = err.message || 'Diagnostic analysis failed. Please verify your connection and try again.';
      setErrorMessage(msg);
      setStep(4);
    } finally {
      setAnalyzing(false);
    }
  };

  const handleStartNewDiagnosis = () => {
    setStep(1);
    setSelectedBrand('');
    setModel('');
    setSelectedIssue('');
    setCustomIssue('');
    setAnalysisResult(null);
    setErrorMessage('');
  };

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1 text-break">AI Device Diagnostic Wizard</h3>
        <p className="text-muted small mb-0">Multi-step safety risk evaluation for your electronic devices.</p>
      </div>

      {/* Progress Indicator */}
      <div className="card p-3 border-0 shadow-sm mb-4">
        <div className="d-flex align-items-center gap-2 text-center overflow-x-auto pb-1 justify-content-start justify-content-md-between flex-nowrap wizard-steps-container">
          {['01 Category', '02 Brand', '03 Model', '04 Symptoms', '05 AI Analysis', '06 Result'].map((label, idx) => (
            <div 
              key={idx} 
              className={`px-3 py-1.5 rounded-pill small fw-bold text-nowrap flex-shrink-0 ${step === idx + 1 ? 'bg-primary text-white' : step > idx + 1 ? 'bg-success text-white' : 'bg-light text-muted'}`}
            >
              {label}
            </div>
          ))}
        </div>
      </div>

      {/* Error notification banner */}
      {errorMessage && (
        <div className="alert alert-danger d-flex align-items-center gap-2 mb-4 shadow-sm" role="alert">
          <AlertTriangle size={20} className="flex-shrink-0 text-danger" />
          <div className="flex-grow-1 small">{errorMessage}</div>
          <button type="button" className="btn-close btn-sm" aria-label="Close" onClick={() => setErrorMessage('')}></button>
        </div>
      )}

      {/* STEP 1: CATEGORY */}
      {step === 1 && (
        <div className="card p-3 p-sm-4 border-0 shadow-sm">
          <h5 className="fw-bold mb-3">Step 1: Select Device Category</h5>
          <div className="row g-2 g-sm-3">
            {(categories.length > 0 ? categories : DEFAULT_CATEGORIES).map((cat) => (
              <div className="col-6 col-sm-4 col-md-3" key={cat}>
                <button
                  type="button"
                  className={`btn category-card w-100 p-2.5 p-sm-3 rounded-3 text-start card-hover h-100 d-flex flex-column justify-content-between ${selectedCategory === cat ? 'selected active' : ''}`}
                  onClick={() => handleSelectCategory(cat)}
                >
                  <Cpu size={24} className="category-icon mb-2 flex-shrink-0" />
                  <div className="fw-bold small category-title text-break">{cat}</div>
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* STEP 2: BRAND */}
      {step === 2 && (
        <div className="card p-3 p-sm-4 border-0 shadow-sm">
          <div className="d-flex align-items-center justify-content-between mb-3 gap-2 flex-wrap">
            <h5 className="fw-bold mb-0 text-break">Step 2: Select Brand for {selectedCategory}</h5>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setStep(1)}><ArrowLeft size={16} /> Back</button>
          </div>
          {loadingMetadata ? (
            <div className="py-4 text-center">
              <Loader text={`Loading brands for ${selectedCategory}...`} />
            </div>
          ) : (
            <div className="row g-2 g-sm-3">
              {(brands.length > 0 ? brands : DEFAULT_BRANDS).map((b) => (
                <div className="col-6 col-sm-4 col-md-3" key={b}>
                  <button className="btn btn-outline-primary w-100 p-2.5 p-sm-3 fw-bold rounded-3 text-break" onClick={() => handleSelectBrand(b)}>
                    {b}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* STEP 3: MODEL */}
      {step === 3 && (
        <div className="card p-3 p-sm-4 border-0 shadow-sm mx-auto" style={{ maxWidth: '540px', width: '100%' }}>
          <div className="d-flex align-items-center justify-content-between mb-3 gap-2">
            <h5 className="fw-bold mb-0">Step 3: Enter Device Model</h5>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setStep(2)}><ArrowLeft size={16} /> Back</button>
          </div>
          <form onSubmit={handleStep3Next}>
            <div className="mb-3">
              <label className="form-label small fw-semibold">Model Name / Number</label>
              <input type="text" className="form-control" placeholder="e.g. Galaxy S22, XPS 13, OLED55..." value={model} onChange={(e) => setModel(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary w-100 py-2.5 fw-bold d-flex align-items-center justify-content-center gap-2">
              Next: Select Symptoms <ArrowRight size={18} />
            </button>
          </form>
        </div>
      )}

      {/* STEP 4: PROBLEM */}
      {step === 4 && (
        <div className="card p-3 p-sm-4 border-0 shadow-sm">
          <div className="d-flex align-items-center justify-content-between mb-3 gap-2">
            <h5 className="fw-bold mb-0">Step 4: Select or Describe Problem</h5>
            <button className="btn btn-sm btn-outline-secondary" onClick={() => setStep(3)} disabled={analyzing}><ArrowLeft size={16} /> Back</button>
          </div>
          <div className="mb-4">
            <label className="form-label small fw-semibold">Common Issues for {selectedCategory}:</label>
            <div className="d-flex flex-wrap gap-2">
              {(commonIssues.length > 0 ? commonIssues : DEFAULT_ISSUES).map((iss) => (
                <button
                  key={iss}
                  type="button"
                  className={`btn btn-sm ${selectedIssue === iss ? 'btn-primary' : 'btn-outline-secondary'} text-break`}
                  onClick={() => setSelectedIssue(iss)}
                  disabled={analyzing}
                >
                  {iss}
                </button>
              ))}
            </div>
          </div>
          <div className="mb-4">
            <label className="form-label small fw-semibold">Custom Problem Description:</label>
            <textarea
              className="form-control"
              rows={3}
              placeholder="Describe exact symptoms, sounds, or visual defects..."
              value={customIssue}
              onChange={(e) => setCustomIssue(e.target.value)}
              disabled={analyzing}
            ></textarea>
          </div>
          <button
            className="btn btn-success btn-lg w-100 py-3 fw-bold d-flex align-items-center justify-content-center gap-2 text-break"
            onClick={handleRunAnalysis}
            disabled={analyzing}
          >
            <Cpu size={20} className="flex-shrink-0" /> {analyzing ? 'Analyzing Device Symptoms...' : 'Run AI Safety & Fault Diagnosis'}
          </button>
        </div>
      )}

      {/* STEP 5: ANALYSIS IN PROGRESS */}
      {step === 5 && (
        <div className="card p-4 p-sm-5 border-0 shadow-sm text-center">
          <Loader text="AI Safety Engine & Gemini API analyzing device symptoms..." />
          <div className="mt-3 text-muted small">Evaluating electrical shock, thermal battery runaway & component hazards...</div>
        </div>
      )}

      {/* STEP 6: DIAGNOSIS RESULT */}
      {step === 6 && analysisResult && (
        <div className="card p-3 p-sm-4 border-0 shadow-sm">
          <div className="d-flex flex-column flex-sm-row align-items-start align-items-sm-center justify-content-between border-bottom pb-3 mb-4 gap-2">
            <div>
              <h4 className="fw-bold mb-1 text-break">Diagnostic Report: {selectedBrand} {model}</h4>
              <div className="text-muted small">Category: {selectedCategory}</div>
            </div>
            <RiskBadge riskLevel={analysisResult.riskLevel} />
          </div>

          {/* CRITICAL SAFETY OVERRIDE ALERT */}
          {(analysisResult.riskLevel === 'CRITICAL' || analysisResult.riskLevel === 'HIGH' || analysisResult.requiresProfessional) && (
            <>
              <HighVoltageWarning />
              <SafetyAlert
                title="DANGER: HIGH HAZARD ISSUE DETECTED"
                message={analysisResult.recommendedAction}
                warnings={analysisResult.safetyWarnings}
                doNotAttempt={analysisResult.doNotAttempt}
              />
              <div className="my-4 text-center">
                <Link to="/technicians" className="btn btn-danger btn-lg px-3 px-sm-5 py-3 fw-bold d-inline-flex align-items-center justify-content-center gap-2 shadow w-100 w-sm-auto text-break">
                  <Wrench size={24} className="flex-shrink-0" /> Book Qualified Professional Technician Now
                </Link>
              </div>
            </>
          )}

          <div className="row g-4 mt-2">
            <div className="col-12 col-md-6">
              <h6 className="fw-bold text-dark">Problem Summary:</h6>
              <p className="text-muted small text-break">{analysisResult.problemSummary}</p>

              <h6 className="fw-bold text-dark mt-3">Possible Causes:</h6>
              <ul className="small text-muted ps-3 text-break">
                {analysisResult.possibleCauses?.map((c, i) => <li key={i}>{c}</li>)}
              </ul>
            </div>

            <div className="col-12 col-md-6">
              <h6 className="fw-bold text-dark">Safe Troubleshooting Steps (If Safe):</h6>
              <ul className="small text-muted ps-3 text-break">
                {analysisResult.safeTroubleshootingSteps?.map((s, i) => <li key={i}>{s}</li>)}
              </ul>

              <h6 className="fw-bold text-dark mt-3">Maintenance Tips:</h6>
              <ul className="small text-muted ps-3 text-break">
                {analysisResult.maintenanceTips?.map((m, i) => <li key={i}>{m}</li>)}
              </ul>
            </div>
          </div>

          <div className="mt-4 pt-3 border-top d-flex flex-column flex-sm-row justify-content-between gap-2">
            <button className="btn btn-outline-secondary w-100 w-sm-auto" onClick={handleStartNewDiagnosis}>Start New Diagnosis</button>
            <Link to="/technicians" className="btn btn-primary fw-bold w-100 w-sm-auto text-center">Find Technicians</Link>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
};

export default DiagnosticWizardPage;
