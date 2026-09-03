import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { technicianService } from '../../services/technicianService';
import Loader from '../../components/common/Loader';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import { Star, ShieldCheck, MapPin, Wrench, Calendar } from 'lucide-react';
import { Link } from 'react-router-dom';

const TechnicianDirectoryPage = () => {
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [specialization, setSpecialization] = useState('');

  const fetchTechs = async () => {
    setLoading(true);
    try {
      const res = specialization 
        ? await technicianService.search({ specialization }) 
        : await technicianService.getAll();
      setTechnicians(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTechs();
  }, [specialization]);

  return (
    <DashboardLayout>
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h3 className="fw-bold mb-1">Certified Technician Directory</h3>
          <p className="text-muted small mb-0">Browse verified electronics technicians and book appointments.</p>
        </div>
        <div style={{ width: '250px' }}>
          <select className="form-select form-select-sm" value={specialization} onChange={(e) => setSpecialization(e.target.value)}>
            <option value="">All Specializations</option>
            <option value="Smartphone">Smartphone Specialist</option>
            <option value="Laptop">Laptop Specialist</option>
            <option value="Television">TV & Display Specialist</option>
            <option value="Refrigerator">Refrigerator Specialist</option>
          </select>
        </div>
      </div>

      {loading ? (
        <Loader text="Searching technicians..." />
      ) : technicians.length === 0 ? (
        <EmptyState title="No Technicians Found" message="Try resetting specialization filters." />
      ) : (
        <div className="row g-4">
          {technicians.map((tech) => (
            <div className="col-md-6 col-lg-4" key={tech.id}>
              <div className="card p-4 border-0 shadow-sm card-hover h-100 d-flex flex-column justify-content-between">
                <div>
                  <div className="d-flex align-items-center justify-content-between mb-2">
                    <span className="badge bg-success bg-opacity-10 text-success fw-bold px-2.5 py-1 rounded-pill">
                      <ShieldCheck size={14} className="me-1" /> {tech.verificationStatus}
                    </span>
                    <div className="d-flex align-items-center text-warning fw-bold small">
                      <Star size={16} className="fill-current me-1" /> {tech.averageRating || 5.0} ({tech.totalReviews || 0})
                    </div>
                  </div>

                  <h5 className="fw-bold mb-1">{tech.fullName}</h5>
                  <p className="text-muted small mb-3"><MapPin size={14} className="me-1" /> {tech.serviceAreas?.join(', ') || 'Nationwide'}</p>

                  <div className="mb-3">
                    <span className="text-muted extra-small fw-semibold d-block mb-1">SPECIALIZATIONS:</span>
                    <div className="d-flex flex-wrap gap-1">
                      {tech.specializations?.map((s, idx) => (
                        <span key={idx} className="badge bg-light text-dark border">{s}</span>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-top d-flex gap-2">
                  <Link to={`/bookings/new?techId=${tech.id}`} className="btn btn-primary btn-sm w-100 d-flex align-items-center justify-content-center gap-1 fw-bold">
                    <Calendar size={14} /> Book Appointment
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </DashboardLayout>
  );
};

export default TechnicianDirectoryPage;
