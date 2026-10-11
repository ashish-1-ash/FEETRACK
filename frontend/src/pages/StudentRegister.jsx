import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { IndianRupee, Mail, Lock, Eye, EyeOff, User, Phone, BookOpen, Hash, AlertCircle, CheckCircle } from 'lucide-react';
import { registerStudent } from '../services/feetrackService';
import { getErrorMessage } from '../utils/helpers';

const COURSE_OPTIONS = [
  'B.Tech Computer Science & Engineering',
  'B.Tech Electronics & Communication Engineering',
  'B.Tech Mechanical Engineering',
  'B.Tech Civil Engineering',
  'B.Tech Information Technology',
  'B.Tech Electrical Engineering',
  'B.Sc Computer Science',
  'B.Com',
  'B.B.A',
  'M.Tech Computer Science',
  'M.B.A',
  'M.Sc Data Science',
  'Other',
];

export default function StudentRegister() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    studentId: '',
    course: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const validate = () => {
    const errs = {};
    if (!form.fullName.trim()) errs.fullName = 'Full name is required';
    else if (form.fullName.trim().length < 2) errs.fullName = 'Name must be at least 2 characters';

    if (!form.email.trim()) errs.email = 'Email address is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errs.email = 'Enter a valid email address';

    if (!form.phone.trim()) errs.phone = 'Phone number is required';
    else if (!/^[6-9]\d{9}$/.test(form.phone.replace(/\D/g, ''))) errs.phone = 'Enter a valid 10-digit mobile number';

    if (!form.password) errs.password = 'Password is required';
    else if (form.password.length < 6) errs.password = 'Password must be at least 6 characters';

    if (!form.confirmPassword) errs.confirmPassword = 'Please confirm your password';
    else if (form.password !== form.confirmPassword) errs.confirmPassword = 'Passwords do not match';

    return errs;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;

    setLoading(true);
    try {
      const res = await registerStudent({
        fullName: form.fullName.trim(),
        email: form.email.trim().toLowerCase(),
        phone: form.phone.trim(),
        studentId: form.studentId.trim() || undefined,
        course: form.course || undefined,
        password: form.password,
        confirmPassword: form.confirmPassword,
      });
      navigate('/student/login', {
        state: { registered: res.data.message || 'Registration successful! Please sign in.' }
      });
    } catch (err) {
      setApiError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const passwordStrength = () => {
    const p = form.password;
    if (!p) return null;
    let score = 0;
    if (p.length >= 8) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/\d/.test(p)) score++;
    if (/[^A-Za-z0-9]/.test(p)) score++;
    if (score <= 1) return { label: 'Weak', color: 'var(--red-500)' };
    if (score === 2) return { label: 'Fair', color: 'var(--amber-500)' };
    if (score === 3) return { label: 'Good', color: 'var(--emerald-500)' };
    return { label: 'Strong', color: 'var(--emerald-600)' };
  };

  const strength = passwordStrength();

  return (
    <div className="auth-page" style={{ paddingTop: 48, paddingBottom: 48 }}>
      <div className="auth-card" style={{ maxWidth: 560 }}>
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

        <h1 className="auth-title">Create your account</h1>
        <p className="auth-subtitle">Register to access the student fee management portal</p>

        {apiError && (
          <div className="alert alert-error">
            <AlertCircle size={16} />
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          {/* Full Name */}
          <div className="form-group">
            <label className="form-label required" htmlFor="fullName">Full Name</label>
            <div style={{ position: 'relative' }}>
              <User size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
              <input id="fullName" name="fullName" type="text" className={`form-input${errors.fullName ? ' error' : ''}`} style={{ paddingLeft: 36 }} placeholder="Rahul Sharma" value={form.fullName} onChange={handleChange} autoComplete="name" autoFocus />
            </div>
            {errors.fullName && <div className="field-error">{errors.fullName}</div>}
          </div>

          {/* Email + Phone */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label required" htmlFor="email">Email Address</label>
              <div style={{ position: 'relative' }}>
                <Mail size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
                <input id="email" name="email" type="email" className={`form-input${errors.email ? ' error' : ''}`} style={{ paddingLeft: 36 }} placeholder="student@institute.edu" value={form.email} onChange={handleChange} autoComplete="email" />
              </div>
              {errors.email && <div className="field-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="phone">Phone Number</label>
              <div style={{ position: 'relative' }}>
                <Phone size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
                <input id="phone" name="phone" type="tel" className={`form-input${errors.phone ? ' error' : ''}`} style={{ paddingLeft: 36 }} placeholder="9876543210" value={form.phone} onChange={handleChange} autoComplete="tel" />
              </div>
              {errors.phone && <div className="field-error">{errors.phone}</div>}
            </div>
          </div>

          {/* Student ID + Course */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="studentId">Student ID (if issued)</label>
              <div style={{ position: 'relative' }}>
                <Hash size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
                <input id="studentId" name="studentId" type="text" className="form-input" style={{ paddingLeft: 36 }} placeholder="STU-2026-001" value={form.studentId} onChange={handleChange} />
              </div>
              <div className="form-hint">Optional — leave blank if not yet issued</div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="course">Programme / Course</label>
              <div style={{ position: 'relative' }}>
                <BookOpen size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
                <select id="course" name="course" className="form-select" style={{ paddingLeft: 36 }} value={form.course} onChange={handleChange}>
                  <option value="">Select programme</option>
                  {COURSE_OPTIONS.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
            </div>
          </div>

          {/* Password */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label required" htmlFor="password">Password</label>
              <div style={{ position: 'relative' }}>
                <Lock size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
                <input id="password" name="password" type={showPass ? 'text' : 'password'} className={`form-input${errors.password ? ' error' : ''}`} style={{ paddingLeft: 36, paddingRight: 40 }} placeholder="Min. 6 characters" value={form.password} onChange={handleChange} autoComplete="new-password" />
                <button type="button" onClick={() => setShowPass(v => !v)} style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--slate-400)', padding: 0 }}>
                  {showPass ? <EyeOff size={15} /> : <Eye size={15} />}
                </button>
              </div>
              {strength && <div style={{ fontSize: '0.75rem', marginTop: 4, color: strength.color, fontWeight: 600 }}>Strength: {strength.label}</div>}
              {errors.password && <div className="field-error">{errors.password}</div>}
            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="confirmPassword">Confirm Password</label>
              <div style={{ position: 'relative' }}>
                <Lock size={15} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--slate-400)' }} />
                <input id="confirmPassword" name="confirmPassword" type={showConfirm ? 'text' : 'password'} className={`form-input${errors.confirmPassword ? ' error' : ''}`} style={{ paddingLeft: 36, paddingRight: 40 }} placeholder="Re-enter password" value={form.confirmPassword} onChange={handleChange} autoComplete="new-password" />
                <button type="button" onClick={() => setShowConfirm(v => !v)} style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--slate-400)', padding: 0 }}>
                  {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
                </button>
              </div>
              {errors.confirmPassword && <div className="field-error">{errors.confirmPassword}</div>}
            </div>
          </div>

          {/* Note */}
          <div className="alert alert-warning" style={{ fontSize: '0.8rem', marginBottom: 16 }}>
            <AlertCircle size={14} />
            If your Student ID is already in our enrollment records, your account may require administrator verification before activation.
          </div>

          <button type="submit" className="btn btn-primary btn-full btn-lg" disabled={loading}>
            {loading ? <><span className="spinner-sm" /> Creating account…</> : 'Create Student Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, fontSize: '0.875rem', color: 'var(--slate-500)' }}>
          Already have an account?{' '}
          <Link to="/student/login" style={{ color: 'var(--emerald-600)', fontWeight: 600 }}>Sign in here</Link>
        </p>
      </div>
    </div>
  );
}
