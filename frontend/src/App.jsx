import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { DeviceProvider } from './context/DeviceContext';
import AppRoutes from './routes/AppRoutes';
import './styles/global.css';

function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <DeviceProvider>
          <Router>
            <AppRoutes />
          </Router>
        </DeviceProvider>
      </NotificationProvider>
    </AuthProvider>
  );
}

export default App;
