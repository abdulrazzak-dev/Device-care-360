import React, { createContext, useContext, useState } from 'react';
import { deviceService } from '../services/deviceService';
import { useAuth } from './AuthContext';

const DeviceContext = createContext(null);

export const DeviceProvider = ({ children }) => {
  const { user } = useAuth();
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchUserDevices = async () => {
    if (!user?.userId) return;
    setLoading(true);
    try {
      const res = await deviceService.getUserDevices(user.userId);
      if (res.data) {
        setDevices(res.data);
      }
    } catch (err) {
      console.error('Failed to fetch user devices:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <DeviceContext.Provider value={{ devices, loading, fetchUserDevices }}>
      {children}
    </DeviceContext.Provider>
  );
};

export const useDevice = () => useContext(DeviceContext);
