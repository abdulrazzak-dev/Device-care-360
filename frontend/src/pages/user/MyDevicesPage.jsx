import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';
import { deviceService } from '../../services/deviceService';
import Loader from '../../components/common/Loader';
import EmptyState from '../../components/common/EmptyState';
import { Smartphone, Plus, Trash2, Cpu } from 'lucide-react';
import { Link } from 'react-router-dom';

const MyDevicesPage = () => {
  const { user } = useAuth();
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newDevice, setNewDevice] = useState({
    category: 'Smartphone',
    brand: 'Apple',
    model: '',
    serialNumber: '',
    warrantyInfo: 'Under Warranty'
  });

  const loadDevices = async () => {
    if (!user?.userId) return;
    try {
      const res = await deviceService.getUserDevices(user.userId);
      setDevices(res.data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDevices();
  }, [user]);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await deviceService.registerDevice(newDevice);
      setShowModal(false);
      setNewDevice({ category: 'Smartphone', brand: 'Apple', model: '', serialNumber: '', warrantyInfo: 'Under Warranty' });
      loadDevices();
    } catch (err) {
      alert('Failed to register device: ' + err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to remove this device?')) return;
    try {
      await deviceService.deleteDevice(id);
      loadDevices();
    } catch (err) {
      alert('Failed to delete device: ' + err.message);
    }
  };

  if (loading) return <DashboardLayout><Loader text="Loading your devices..." /></DashboardLayout>;

  return (
    <DashboardLayout>
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h3 className="fw-bold mb-1">My Device Inventory</h3>
          <p className="text-muted small mb-0">Manage registered electronic devices for rapid AI diagnostics.</p>
        </div>
        <button className="btn btn-primary btn-sm d-flex align-items-center gap-2" onClick={() => setShowModal(true)}>
          <Plus size={16} /> Add New Device
        </button>
      </div>

      {devices.length === 0 ? (
        <EmptyState
          title="No Registered Devices"
          message="Add your smartphone, laptop, or home appliances to quickly diagnose issues."
          actionLabel="Register First Device"
          onAction={() => setShowModal(true)}
        />
      ) : (
        <div className="row g-3">
          {devices.map((dev) => (
            <div className="col-md-4" key={dev.id}>
              <div className="card p-4 border-0 shadow-sm card-hover h-100 d-flex flex-column justify-content-between">
                <div>
                  <div className="d-flex align-items-center justify-content-between mb-3">
                    <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-1 rounded-pill">{dev.category}</span>
                    <button className="btn btn-link text-danger p-0" onClick={() => handleDelete(dev.id)}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                  <h5 className="fw-bold mb-1">{dev.brand} {dev.model}</h5>
                  <p className="text-muted small mb-2">S/N: {dev.serialNumber || 'N/A'}</p>
                  <span className="badge bg-light text-dark border">{dev.warrantyInfo}</span>
                </div>
                <div className="mt-4 pt-3 border-top d-flex gap-2">
                  <Link to="/diagnose" className="btn btn-primary btn-sm w-100 d-flex align-items-center justify-content-center gap-1">
                    <Cpu size={14} /> AI Diagnose
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="modal show d-block bg-black bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content rounded-4 border-0 shadow-lg p-3">
              <div className="modal-header border-0">
                <h5 className="modal-title fw-bold">Register Electronic Device</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleCreate}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Category</label>
                    <select className="form-select" value={newDevice.category} onChange={(e) => setNewDevice({ ...newDevice, category: e.target.value })}>
                      {['Smartphone', 'Laptop', 'Television', 'Refrigerator', 'Washing Machine', 'Air Conditioner', 'Printer', 'Computer', 'Tablet', 'Audio Device', 'Other'].map(c => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Brand</label>
                    <input type="text" className="form-control" placeholder="Apple, Samsung, Dell..." value={newDevice.brand} onChange={(e) => setNewDevice({ ...newDevice, brand: e.target.value })} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Model</label>
                    <input type="text" className="form-control" placeholder="iPhone 14 Pro, XPS 15..." value={newDevice.model} onChange={(e) => setNewDevice({ ...newDevice, model: e.target.value })} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Serial Number</label>
                    <input type="text" className="form-control" placeholder="Optional" value={newDevice.serialNumber} onChange={(e) => setNewDevice({ ...newDevice, serialNumber: e.target.value })} />
                  </div>
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary px-4 fw-bold">Save Device</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
};

export default MyDevicesPage;
