import { useEffect, useState } from 'react';
import { AlertTriangle, User, Mail, Phone, BookOpen, Calendar, Hash, Shield, RefreshCw } from 'lucide-react';
import StudentLayout from '../components/StudentLayout';
import { getStudentProfile } from '../services/feetrackService';
import { formatDate, getStatusBadgeClass, formatStatus, getErrorMessage, getInitials } from '../utils/helpers';

export default function StudentProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getStudentProfile();
      setProfile(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  return (
    <StudentLayout>
      <div className="page-header-row">
        <div className="page-header">
          <h1 className="page-title">My Profile</h1>
          <p className="page-subtitle">Your student enrollment and account details</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}>
          <RefreshCw size={15} /> Refresh
        </button>
      </div>

      {loading && <div className="loading-page"><div className="spinner" /><p>Loading profile…</p></div>}
      {!loading && error && <div className="alert alert-error"><AlertTriangle size={16} /> {error}</div>}

      {!loading && profile && (
        <div style={{ maxWidth: 700, margin: '0 auto' }}>
          {/* Avatar Card */}
          <div className="card" style={{ textAlign: 'center', padding: '36px 24px', marginBottom: 20 }}>
            <div style={{
              width: 80, height: 80, borderRadius: '50%', margin: '0 auto 16px',
              background: 'linear-gradient(135deg, var(--emerald-500), var(--emerald-400))',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: 'white', fontSize: '1.6rem', fontWeight: 700
            }}>
              {getInitials(profile.fullName)}
            </div>
            <h2 style={{ fontSize: '1.3rem', fontWeight: 700, color: 'var(--slate-800)', marginBottom: 4 }}>{profile.fullName}</h2>
            <p style={{ fontSize: '0.9rem', color: 'var(--slate-500)', marginBottom: 8 }}>{profile.course || 'Student'}</p>
            <span className={`badge ${getStatusBadgeClass(profile.status)}`} style={{ fontSize: '0.8rem' }}>
              {formatStatus(profile.status)}
            </span>
          </div>

          {/* Details Card */}
          <div className="card">
            <div className="card-title" style={{ marginBottom: 20 }}>Account Details</div>
            {[
              { icon: Hash, label: 'Student ID', value: profile.studentIdNumber },
              { icon: User, label: 'Full Name', value: profile.fullName },
              { icon: Mail, label: 'Email Address', value: profile.email },
              { icon: Phone, label: 'Phone Number', value: profile.phone },
              { icon: BookOpen, label: 'Programme / Course', value: profile.course },
              { icon: Calendar, label: 'Academic Year', value: profile.academicYear },
              { icon: Calendar, label: 'Current Semester', value: profile.currentSemester },
              { icon: Calendar, label: 'Enrollment Date', value: formatDate(profile.enrollmentDate) },
              { icon: Shield, label: 'Account Status', value: formatStatus(profile.status), badge: true, badgeClass: getStatusBadgeClass(profile.status) },
            ].map(({ icon: Icon, label, value, badge, badgeClass }) => (
              <div key={label} style={{
                display: 'flex', alignItems: 'center', gap: 14, padding: '14px 0',
                borderBottom: '1px solid var(--slate-100)'
              }}>
                <div style={{
                  width: 36, height: 36, borderRadius: 8, flexShrink: 0,
                  background: 'var(--emerald-50)', color: 'var(--emerald-600)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>
                  <Icon size={16} />
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--slate-500)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>{label}</div>
                  {badge ? (
                    <span className={`badge ${badgeClass}`} style={{ marginTop: 2 }}>{value}</span>
                  ) : (
                    <div style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--slate-800)', marginTop: 1 }}>{value || 'N/A'}</div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Notice */}
          <div className="alert alert-warning" style={{ marginTop: 20, fontSize: '0.85rem' }}>
            <AlertTriangle size={14} />
            To update your personal or enrollment details, please contact the administration office.
          </div>
        </div>
      )}
    </StudentLayout>
  );
}
