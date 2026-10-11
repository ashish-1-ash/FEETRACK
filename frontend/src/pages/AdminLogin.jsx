import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { IndianRupee, Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { loginAdmin } from '../services/feetrackService';
import { getErrorMessage } from '../utils/helpers';

export default function AdminLogin() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  const validate = () => {
    const errs = {};
    if (!form.email) errs.email = 'Email is required';
    if (!form.password) errs.password = 'Password is required';
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
      const res = await loginAdmin({ email: form.email.trim().toLowerCase(), password: form.password });
      const { token, ...userData } = res.data;
      if (userData.role !== 'ROLE_ADMIN' && userData.role !== 'ROLE_ACCOUNTANT') {
        setApiError('This login is for administrators only. Students should use the student portal.');
        setLoading(false);
        return;
      }
      login(token, userData);
      navigate('/admin/dashboard', { replace: true });
    } catch (err) {
      setApiError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    if (errors[e.target.name]) setErrors(prev => ({ ...prev, [e.target.name]: '' }));
  };

  return (
    <div className="auth-page" style={{ background: 'linear-gradient(135deg, var(--slate-900) 0%, var(--slate-700) 100%)' }}>
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon" style={{ background: 'linear-gradient(135deg, var(--slate-700), var(--slate-500))' }}>
            <IndianRupee size={24} strokeWidth={2.5} />
          </div>
          <div>
            <div className="auth-logo-text">FeeTrack</div>
            <div className="auth-logo-sub" style={{ color: 'var(--slate-500)' }}>Administrator Portal</div>
          </div>
        </div>
        <h1 className="auth-title">Admin Sign In</h1>
        <p className="auth-subtitle">Access the institutional fee management dashboard</p>

        {apiError && <div className="alert alert-error"><AlertCircle size={16} /> {apiError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label className="form-label required" htmlFor="admin-email">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
              <input id="admin-email" name="email" type="email" className={`form-input${errors.email ? ' error' : ''}`} style={{ paddingLeft: 36 }} placeholder="admin@institution.edu" value={form.email} onChange={handleChange} autoFocus />
            </div>
            {errors.email && <div className="field-error">{errors.email}</div>}
          </div>

          <div className="form-group">
            <label className="form-label required" htmlFor="admin-password">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
              <input id="admin-password" name="password" type={showPass ? 'text' : 'password'} className={`form-input${errors.password ? ' error' : ''}`} style={{ paddingLeft: 36, paddingRight: 40 }} placeholder="Enter password" value={form.password} onChange={handleChange} />
              <button type="button" onClick={() => setShowPass(v => !v)} style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--slate-400)', padding: 0 }}>
                {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            {errors.password && <div className="field-error">{errors.password}</div>}
          </div>

          <button type="submit" className="btn btn-full btn-lg" style={{ marginTop: 8, background: 'var(--slate-800)', color: 'white' }} disabled={loading}>
            {loading ? <><span className="spinner-sm" /> Signing in…</> : 'Sign In as Administrator'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, fontSize: '0.85rem', color: 'var(--slate-500)' }}>
          <Link to="/student/login" style={{ color: 'var(--emerald-600)', fontWeight: 600 }}>← Student Portal Login</Link>
        </p>
      </div>
    </div>
  );
}
