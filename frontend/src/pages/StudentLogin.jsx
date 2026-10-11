import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { IndianRupee, Mail, Lock, Eye, EyeOff, AlertCircle, CheckCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { loginStudent } from '../services/feetrackService';
import { getErrorMessage } from '../utils/helpers';

export default function StudentLogin() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [formData, setFormData] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  const expired = new URLSearchParams(location.search).get('expired') === '1';
  const registered = location.state?.registered;

  const validate = () => {
    const errs = {};
    if (!formData.email) errs.email = 'Email address is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) errs.email = 'Enter a valid email address';
    if (!formData.password) errs.password = 'Password is required';
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;

    setLoading(true);
    try {
      const res = await loginStudent({ email: formData.email.trim().toLowerCase(), password: formData.password });
      const { token, ...userData } = res.data;
      login(token, userData);
      navigate('/student/dashboard', { replace: true });
    } catch (err) {
      setApiError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        {/* Logo */}
        <div className="auth-logo">
          <div className="auth-logo-icon">
            <IndianRupee size={24} strokeWidth={2.5} />
          </div>
          <div>
            <div className="auth-logo-text">FeeTrack</div>
            <div className="auth-logo-sub">Student Portal</div>
          </div>
        </div>

        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-subtitle">Sign in to access your student fee portal</p>

        {/* Alerts */}
        {expired && (
          <div className="alert alert-warning">
            <AlertCircle size={16} />
            Your session has expired. Please sign in again.
          </div>
        )}
        {registered && (
          <div className="alert alert-success">
            <CheckCircle size={16} />
            {registered}
          </div>
        )}
        {apiError && (
          <div className="alert alert-error">
            <AlertCircle size={16} />
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label required" htmlFor="email">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
              <input
                id="email"
                name="email"
                type="email"
                className={`form-input${errors.email ? ' error' : ''}`}
                style={{ paddingLeft: 36 }}
                placeholder="student@institution.edu"
                value={formData.email}
                onChange={handleChange}
                autoComplete="email"
                autoFocus
              />
            </div>
            {errors.email && <div className="field-error">{errors.email}</div>}
          </div>

          <div className="form-group">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="form-label required" htmlFor="password">Password</label>
            </div>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
              <input
                id="password"
                name="password"
                type={showPass ? 'text' : 'password'}
                className={`form-input${errors.password ? ' error' : ''}`}
                style={{ paddingLeft: 36, paddingRight: 40 }}
                placeholder="Enter your password"
                value={formData.password}
                onChange={handleChange}
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPass(v => !v)}
                style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--slate-400)', cursor: 'pointer', padding: 0 }}
                aria-label={showPass ? 'Hide password' : 'Show password'}
              >
                {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            {errors.password && <div className="field-error">{errors.password}</div>}
          </div>

          <button type="submit" className="btn btn-primary btn-full btn-lg" style={{ marginTop: 8 }} disabled={loading}>
            {loading ? <><span className="spinner-sm" /> Signing in…</> : 'Sign In to Portal'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 24, fontSize: '0.875rem', color: 'var(--slate-500)' }}>
          Don&apos;t have an account?{' '}
          <Link to="/student/register" style={{ color: 'var(--emerald-600)', fontWeight: 600 }}>
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
}
