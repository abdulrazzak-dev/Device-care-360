import React, { useEffect, useState, useMemo } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { repairGuideService } from '../../services/repairGuideService';
import { FALLBACK_REPAIR_GUIDES } from './seedGuidesFallback';
import RepairGuideModal from './RepairGuideModal';
import Loader from '../../components/common/Loader';
import EmptyState from '../../components/common/EmptyState';
import RiskBadge from '../../components/safety/RiskBadge';
import {
  Search,
  X,
  Filter,
  Wrench,
  ShieldAlert,
  Clock,
  BookOpen,
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  Sliders,
  ExternalLink
} from 'lucide-react';

const CATEGORIES = [
  { id: 'ALL', label: 'All Appliances' },
  { id: 'Smartphone', label: 'Smartphone' },
  { id: 'Laptop / PC', label: 'Laptop / PC' },
  { id: 'Air Conditioner', label: 'Air Conditioner' },
  { id: 'Washing Machine', label: 'Washing Machine' },
  { id: 'Refrigerator', label: 'Refrigerator' }
];

const DIFFICULTIES = [
  { id: 'ALL', label: 'All Levels' },
  { id: 'BEGINNER', label: 'Beginner / Easy' },
  { id: 'INTERMEDIATE', label: 'Intermediate' },
  { id: 'ADVANCED', label: 'Advanced' }
];

const RepairGuideListPage = () => {
  const [guides, setGuides] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [usingFallback, setUsingFallback] = useState(false);

  // Filters & Search
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedDifficulty, setSelectedDifficulty] = useState('ALL');

  // Modal State
  const [activeModalGuide, setActiveModalGuide] = useState(null);

  const fetchGuides = async (isManualRefresh = false) => {
    if (isManualRefresh) setRefreshing(true);
    else setLoading(true);

    setApiError(null);

    try {
      const res = await repairGuideService.getGuides();
      // Defensive parsing: handles { success: true, data: [...] } or plain array [...]
      const dataList = Array.isArray(res)
        ? res
        : Array.isArray(res?.data)
        ? res.data
        : Array.isArray(res?.data?.data)
        ? res.data.data
        : [];

      if (dataList.length > 0) {
        setGuides(dataList);
        setUsingFallback(false);
      } else {
        // If remote database collection is empty, load certified fallback manuals
        console.warn('Backend returned 0 guides. Loading verified default manuals.');
        setGuides(FALLBACK_REPAIR_GUIDES);
        setUsingFallback(true);
      }
    } catch (err) {
      console.warn('Unable to reach backend repair-guide-service. Falling back to official local guides:', err);
      setApiError(err?.message || 'Backend microservice is waking up or temporarily unavailable.');
      setGuides(FALLBACK_REPAIR_GUIDES);
      setUsingFallback(true);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchGuides();
  }, []);

  // Filtered guides calculation
  const filteredGuides = useMemo(() => {
    return guides.filter((g) => {
      // 1. Search Query Filter
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim();
        const titleMatch = (g.title || '').toLowerCase().includes(query);
        const issueMatch = (g.issue || g.description || '').toLowerCase().includes(query);
        const categoryMatch = (g.category || '').toLowerCase().includes(query);
        const brandMatch = (g.brand || '').toLowerCase().includes(query);
        const toolsMatch = Array.isArray(g.toolsRequired) && g.toolsRequired.some((t) => t.toLowerCase().includes(query));

        if (!titleMatch && !issueMatch && !categoryMatch && !brandMatch && !toolsMatch) {
          return false;
        }
      }

      // 2. Category Filter
      if (selectedCategory !== 'ALL') {
        const guideCat = (g.category || '').toLowerCase();
        const selectedCat = selectedCategory.toLowerCase();
        const matchesCategory =
          guideCat.includes(selectedCat) ||
          (selectedCat.includes('smartphone') && guideCat.includes('mobile')) ||
          (selectedCat.includes('laptop') && (guideCat.includes('pc') || guideCat.includes('computer'))) ||
          (selectedCat.includes('air conditioner') && guideCat.includes('ac'));

        if (!matchesCategory) return false;
      }

      // 3. Difficulty Filter
      if (selectedDifficulty !== 'ALL') {
        const guideDiff = (g.difficulty || g.skillLevel || '').toUpperCase();
        if (selectedDifficulty === 'BEGINNER') {
          if (guideDiff !== 'BEGINNER' && guideDiff !== 'EASY') return false;
        } else if (selectedDifficulty === 'INTERMEDIATE') {
          if (guideDiff !== 'INTERMEDIATE' && guideDiff !== 'MEDIUM') return false;
        } else if (selectedDifficulty === 'ADVANCED') {
          if (guideDiff !== 'ADVANCED' && guideDiff !== 'PROFESSIONAL_ONLY' && guideDiff !== 'HIGH') return false;
        }
      }

      return true;
    });
  }, [guides, searchQuery, selectedCategory, selectedDifficulty]);

  const hasActiveFilters = searchQuery.trim() !== '' || selectedCategory !== 'ALL' || selectedDifficulty !== 'ALL';

  const resetFilters = () => {
    setSearchQuery('');
    setSelectedCategory('ALL');
    setSelectedDifficulty('ALL');
  };

  if (loading) {
    return (
      <DashboardLayout>
        <Loader text="Loading certified repair guides and diagnostic manuals..." />
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      {/* Page Header */}
      <div className="d-flex flex-column flex-md-row align-items-start align-items-md-center justify-content-between mb-4 gap-3">
        <div>
          <div className="d-flex align-items-center gap-2 mb-1">
            <h3 className="fw-bold mb-0 text-dark">Official Repair Guides & Manuals</h3>
            {usingFallback ? (
              <span className="badge bg-warning bg-opacity-10 text-warning border border-warning-subtle rounded-pill small">
                Offline Verified
              </span>
            ) : (
              <span className="badge bg-success bg-opacity-10 text-success border border-success-subtle rounded-pill small d-inline-flex align-items-center gap-1">
                <CheckCircle2 size={12} /> Cloud Synced
              </span>
            )}
          </div>
          <p className="text-muted small mb-0">
            Certified step-by-step diagnostic procedures, hazard mitigations, and preventative maintenance tips.
          </p>
        </div>

        <div className="d-flex align-items-center gap-2">
          <button
            className="btn btn-outline-primary btn-sm d-inline-flex align-items-center gap-1 shadow-sm"
            onClick={() => fetchGuides(true)}
            disabled={refreshing}
          >
            <RefreshCw size={14} className={refreshing ? 'spin' : ''} />
            {refreshing ? 'Syncing...' : 'Sync Cloud'}
          </button>
        </div>
      </div>

      {/* Cloud Wake-up Alert / Notification */}
      {apiError && (
        <div className="alert alert-warning alert-dismissible fade show border-warning-subtle shadow-sm mb-4 d-flex align-items-center gap-2 py-2 px-3" role="alert">
          <AlertTriangle size={18} className="text-warning flex-shrink-0" />
          <div className="small flex-grow-1">
            <strong>Microservice Notice:</strong> {apiError} Displaying pre-loaded certified manuals so you can continue your repair.
          </div>
          <button
            type="button"
            className="btn btn-link btn-sm text-dark fw-semibold p-0 text-decoration-underline"
            onClick={() => fetchGuides(true)}
          >
            Retry Connection
          </button>
        </div>
      )}

      {/* Search and Filter Controls */}
      <div className="card p-3 border-0 shadow-sm mb-4 bg-white">
        <div className="row g-3 align-items-center">
          {/* Keyword Search */}
          <div className="col-12 col-lg-5">
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0 text-muted">
                <Search size={16} />
              </span>
              <input
                type="text"
                className="form-control bg-light border-start-0 ps-0 shadow-none"
                placeholder="Search by appliance, symptom, tool, or keyword..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              {searchQuery && (
                <button
                  className="btn btn-light border-start-0 text-muted"
                  type="button"
                  onClick={() => setSearchQuery('')}
                >
                  <X size={15} />
                </button>
              )}
            </div>
          </div>

          {/* Difficulty Dropdown */}
          <div className="col-6 col-lg-3">
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0 text-muted">
                <Sliders size={15} />
              </span>
              <select
                className="form-select bg-light border-start-0 shadow-none text-secondary small"
                value={selectedDifficulty}
                onChange={(e) => setSelectedDifficulty(e.target.value)}
              >
                {DIFFICULTIES.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Clear Filters Button */}
          <div className="col-6 col-lg-4 text-lg-end">
            {hasActiveFilters && (
              <button
                className="btn btn-sm btn-link text-danger text-decoration-none p-0 d-inline-flex align-items-center gap-1"
                onClick={resetFilters}
              >
                <X size={14} /> Clear all filters
              </button>
            )}
            <span className="text-muted small ms-2">
              Showing <strong>{filteredGuides.length}</strong> of {guides.length} manuals
            </span>
          </div>
        </div>

        {/* Category Filter Pills */}
        <div className="d-flex align-items-center gap-2 overflow-x-auto pt-3 mt-2 border-top wizard-steps-container">
          {CATEGORIES.map((cat) => {
            const isSelected = selectedCategory === cat.id;
            return (
              <button
                key={cat.id}
                type="button"
                className={`btn btn-sm rounded-pill px-3 py-1 text-nowrap fw-medium transition-all ${
                  isSelected
                    ? 'btn-primary text-white shadow-sm'
                    : 'btn-outline-secondary border-light-subtle bg-light text-secondary'
                }`}
                onClick={() => setSelectedCategory(cat.id)}
              >
                {cat.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Guide Cards Grid */}
      {filteredGuides.length === 0 ? (
        <EmptyState
          title="No Repair Guides Found"
          message={
            hasActiveFilters
              ? "We couldn't find any repair manuals matching your current filters. Try resetting your search or category."
              : "No repair guides are currently available. Check back soon for official updates."
          }
          actionLabel={hasActiveFilters ? "Reset Filters" : "Reload Guides"}
          onAction={hasActiveFilters ? resetFilters : () => fetchGuides(true)}
        />
      ) : (
        <div className="row g-4">
          {filteredGuides.map((g) => {
            const tools = g.toolsRequired || [];
            const difficulty = g.difficulty || g.skillLevel || 'INTERMEDIATE';
            const estimatedTime = g.estimatedTime || '30-45 minutes';
            const warnings = g.safetyWarnings || [];
            const primaryWarning = warnings.length > 0 ? warnings[0] : null;

            return (
              <div className="col-12 col-md-6 col-lg-4" key={g.id || g.title}>
                <div className="card p-4 border-0 shadow-sm card-hover h-100 d-flex flex-column bg-white rounded-3 position-relative">
                  {/* Top Badges */}
                  <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
                    <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-1 rounded-pill fw-semibold small">
                      {g.category}
                    </span>
                    <RiskBadge riskLevel={g.riskLevel} />
                  </div>

                  {/* Title & Issue */}
                  <h5 className="fw-bold text-dark mb-2" style={{ lineHeight: '1.3' }}>
                    {g.title}
                  </h5>

                  <p className="text-secondary small mb-3 flex-grow-1" style={{ minHeight: '40px' }}>
                    <strong>Issue:</strong> {g.issue || g.description}
                  </p>

                  {/* Metadata Row: Difficulty & Time */}
                  <div className="d-flex align-items-center gap-3 py-2 px-3 bg-light rounded-2 mb-3 text-secondary small">
                    <span className="d-flex align-items-center gap-1">
                      <Clock size={14} className="text-primary" /> {estimatedTime}
                    </span>
                    <span className="text-muted">|</span>
                    <span className="d-flex align-items-center gap-1">
                      <Sliders size={14} className="text-primary" /> {difficulty}
                    </span>
                  </div>

                  {/* Tools Preview */}
                  {tools.length > 0 && (
                    <div className="mb-3">
                      <div className="d-flex align-items-center gap-1 text-muted small mb-2">
                        <Wrench size={13} />
                        <span className="fw-semibold">Key Tools:</span>
                      </div>
                      <div className="d-flex flex-wrap gap-1">
                        {tools.slice(0, 3).map((tool, idx) => (
                          <span
                            key={idx}
                            className="badge bg-white text-secondary border px-2 py-1 rounded-2 small text-truncate"
                            style={{ maxWidth: '200px' }}
                            title={tool}
                          >
                            {tool}
                          </span>
                        ))}
                        {tools.length > 3 && (
                          <span className="badge bg-light text-muted border px-2 py-1 rounded-2 small">
                            +{tools.length - 3} more
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Safety Warning Snippet */}
                  {primaryWarning && (
                    <div className="p-2 px-3 rounded-2 bg-danger bg-opacity-10 text-danger small mb-3 d-flex align-items-start gap-2">
                      <ShieldAlert size={16} className="flex-shrink-0 mt-1" />
                      <span className="text-truncate-2" style={{ fontSize: '0.78rem', lineHeight: '1.3' }}>
                        {primaryWarning}
                      </span>
                    </div>
                  )}

                  {/* Action Button */}
                  <button
                    type="button"
                    className="btn btn-primary w-100 mt-auto d-inline-flex align-items-center justify-content-center gap-2 py-2 fw-semibold rounded-2 shadow-sm"
                    onClick={() => setActiveModalGuide(g)}
                  >
                    <BookOpen size={16} /> View Step-by-Step Manual
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Interactive Modal */}
      <RepairGuideModal
        guide={activeModalGuide}
        isOpen={Boolean(activeModalGuide)}
        onClose={() => setActiveModalGuide(null)}
      />
    </DashboardLayout>
  );
};

export default RepairGuideListPage;
