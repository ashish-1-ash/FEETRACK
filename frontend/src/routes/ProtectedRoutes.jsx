import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function StudentRoute({ children }) {
  const { isAuthenticated, isStudent, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="loading-page">
        <div className="spinner" />
        <p>Verifying session…</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/student/login" state={{ from: location.pathname }} replace />;
  }
  if (!isStudent) {
    return <Navigate to="/unauthorized" replace />;
  }
  return children;
}

export function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="loading-page">
        <div className="spinner" />
        <p>Verifying session…</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/admin/login" state={{ from: location.pathname }} replace />;
  }
  if (!isAdmin) {
    return <Navigate to="/unauthorized" replace />;
  }
  return children;
}

export function PublicRoute({ children, redirectIfAuth = null }) {
  const { isAuthenticated, isStudent, isAdmin } = useAuth();

  if (isAuthenticated && redirectIfAuth) {
    if (isStudent) return <Navigate to="/student/dashboard" replace />;
    if (isAdmin) return <Navigate to="/admin/dashboard" replace />;
  }
  return children;
}
