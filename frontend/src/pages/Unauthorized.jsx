import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export default function Unauthorized() {
  return (
    <div className="auth-page" style={{ background: 'var(--slate-100)' }}>
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ marginBottom: 20 }}>
          <ShieldAlert size={64} color="var(--red-500)" style={{ margin: '0 auto' }} />
        </div>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--slate-800)', marginBottom: 8 }}>Access Denied</h1>
        <p style={{ color: 'var(--slate-500)', marginBottom: 24, fontSize: '0.95rem' }}>
          You do not have permission to view this page. Please check that you are signed in with the correct account role.
        </p>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
          <Link to="/student/login" className="btn btn-primary">
            <ArrowLeft size={16} /> Student Login
          </Link>
          <Link to="/admin/login" className="btn btn-secondary">
            Admin Login
          </Link>
        </div>
      </div>
    </div>
  );
}
