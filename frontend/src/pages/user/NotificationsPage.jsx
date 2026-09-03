import React from 'react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useNotification } from '../../context/NotificationContext';
import { Bell, Check, AlertTriangle } from 'lucide-react';

const NotificationsPage = () => {
  const { notifications, markRead } = useNotification();

  return (
    <DashboardLayout>
      <div className="mb-4">
        <h3 className="fw-bold mb-1">My Notifications</h3>
        <p className="text-muted small mb-0">System alerts, booking updates, and safety warnings.</p>
      </div>

      <div className="card p-4 border-0 shadow-sm">
        {notifications.length === 0 ? (
          <p className="text-muted small my-3">No notifications received.</p>
        ) : (
          <div className="d-flex flex-column gap-3">
            {notifications.map((n) => (
              <div key={n.id} className={`p-3 rounded-3 border ${n.read ? 'bg-light' : 'bg-white border-primary shadow-sm'}`}>
                <div className="d-flex align-items-center justify-content-between mb-1">
                  <span className={`badge ${n.type === 'SAFETY_ALERT' ? 'bg-danger' : 'bg-primary'}`}>{n.type}</span>
                  <span className="small text-muted">{new Date(n.createdAt).toLocaleString()}</span>
                </div>
                <h6 className="fw-bold mb-1">{n.title}</h6>
                <p className="small text-muted mb-2">{n.message}</p>
                {!n.read && (
                  <button className="btn btn-sm btn-link p-0 text-primary fw-semibold" onClick={() => markRead(n.id)}>
                    Mark as Read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default NotificationsPage;
