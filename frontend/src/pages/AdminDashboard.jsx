import { useEffect, useState } from 'react';
import { useNavigate, NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Users, Wallet, CreditCard, LogOut,
  IndianRupee, Menu, X, ChevronRight, AlertTriangle,
  RefreshCw, CheckCircle, Plus, Trash2, Eye, UserCheck
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { logout as apiLogout, getAdminStats, getAllStudents, getAllFeeAllocations, approveStudent, createFeeAllocation, deleteAdminStudent, deleteFeeAllocation } from '../services/feetrackService';
import { formatINR, formatDate, getStatusBadgeClass, formatStatus, getErrorMessage, getInitials } from '../utils/helpers';

const adminNavLinks = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
];

function AdminLayout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = async () => {
    try { await apiLogout(); } catch {}
    logout();
    navigate('/admin/login', { replace: true });
  };

  return (
    <div className="student-layout">
      <button className="sidebar-toggle" onClick={() => setSidebarOpen(!sidebarOpen)}>
        {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
      </button>
      {sidebarOpen && <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 99 }} onClick={() => setSidebarOpen(false)} />}
      <aside className={`sidebar${sidebarOpen ? ' open' : ''}`} style={{ background: 'var(--slate-800)' }}>
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon" style={{ background: 'linear-gradient(135deg, var(--slate-600), var(--slate-400))' }}>
            <IndianRupee size={20} strokeWidth={2.5} />
          </div>
          <div>
            <div className="sidebar-logo-text">FeeTrack</div>
            <div className="sidebar-logo-sub" style={{ color: 'var(--slate-400)' }}>Admin Portal</div>
          </div>
        </div>
        <div className="sidebar-user">
          <div className="sidebar-user-info">
            <div className="sidebar-user-avatar" style={{ background: 'var(--slate-600)' }}>{getInitials(user?.name)}</div>
            <div>
              <div className="sidebar-user-name">{user?.name || 'Admin'}</div>
              <div className="sidebar-user-role" style={{ color: 'var(--slate-400)' }}>{user?.role?.replace('ROLE_', '')}</div>
            </div>
          </div>
        </div>
        <nav className="sidebar-nav">
          <div className="sidebar-section-title">Management</div>
          {adminNavLinks.map(({ to, icon: Icon, label }) => (
            <NavLink key={to} to={to} className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`} onClick={() => setSidebarOpen(false)}>
              <Icon size={18} /> {label} <ChevronRight size={14} style={{ marginLeft: 'auto', opacity: 0.5 }} />
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button className="sidebar-logout" onClick={handleLogout}><LogOut size={18} /> Logout</button>
        </div>
      </aside>
      <main className="main-content">{children}</main>
    </div>
  );
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [students, setStudents] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('overview');

  // New Fee Allocation form
  const [showFeeForm, setShowFeeForm] = useState(false);
  const [feeForm, setFeeForm] = useState({ studentIdNumber: '', feeCategory: 'Tuition Fee', academicYear: '2026-2027', originalAmount: '', discountAmount: '0', dueDate: '' });
  const [feeFormLoading, setFeeFormLoading] = useState(false);
  const [feeFormError, setFeeFormError] = useState('');

  const load = async () => {
    setLoading(true); setError('');
    try {
      const [statsRes, studentsRes, allocRes] = await Promise.all([getAdminStats(), getAllStudents(), getAllFeeAllocations()]);
      setStats(statsRes.data);
      setStudents(studentsRes.data);
      setAllocations(allocRes.data);
    } catch (err) { setError(getErrorMessage(err)); } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleApprove = async (studentIdNumber) => {
    try { await approveStudent(studentIdNumber); load(); } catch (err) { alert(getErrorMessage(err)); }
  };

  const handleDeleteStudent = async (id) => {
    if (!window.confirm('Are you sure?')) return;
    try { await deleteAdminStudent(id); load(); } catch (err) { alert(getErrorMessage(err)); }
  };

  const handleCreateFee = async (e) => {
    e.preventDefault(); setFeeFormError(''); setFeeFormLoading(true);
    try {
      await createFeeAllocation({
        studentIdNumber: feeForm.studentIdNumber,
        feeCategory: feeForm.feeCategory,
        academicYear: feeForm.academicYear,
        originalAmount: Number(feeForm.originalAmount),
        discountAmount: Number(feeForm.discountAmount || 0),
        dueDate: feeForm.dueDate || null,
      });
      setShowFeeForm(false);
      setFeeForm({ studentIdNumber: '', feeCategory: 'Tuition Fee', academicYear: '2026-2027', originalAmount: '', discountAmount: '0', dueDate: '' });
      load();
    } catch (err) { setFeeFormError(getErrorMessage(err)); } finally { setFeeFormLoading(false); }
  };

  const handleDeleteAllocation = async (id) => {
    if (!window.confirm('Delete this fee allocation?')) return;
    try { await deleteFeeAllocation(id); load(); } catch (err) { alert(getErrorMessage(err)); }
  };

  const pendingStudents = students.filter(s => s.status === 'PENDING_VERIFICATION');

  return (
    <AdminLayout>
      <div className="page-header-row">
        <div className="page-header">
          <h1 className="page-title">Admin Dashboard</h1>
          <p className="page-subtitle">Institutional fee management overview</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}><RefreshCw size={15} /> Refresh</button>
      </div>

      {loading && <div className="loading-page"><div className="spinner" /><p>Loading dashboard…</p></div>}
      {!loading && error && <div className="alert alert-error"><AlertTriangle size={16} /> {error}</div>}

      {!loading && stats && (
        <>
          {/* Stats cards */}
          <div className="summary-cards">
            <div className="summary-card">
              <div className="summary-card-icon" style={{ background: 'rgba(5,150,105,0.1)', color: 'var(--emerald-600)' }}><Users size={20} /></div>
              <div className="summary-card-label">Total Students</div>
              <div className="summary-card-value">{stats.totalStudents}</div>
              <div className="summary-card-accent" />
            </div>
            <div className="summary-card">
              <div className="summary-card-icon" style={{ background: 'rgba(5,150,105,0.1)', color: 'var(--emerald-600)' }}><IndianRupee size={20} /></div>
              <div className="summary-card-label">Total Collected</div>
              <div className="summary-card-value">{formatINR(stats.totalFeesCollected)}</div>
              <div className="summary-card-accent" />
            </div>
            <div className="summary-card">
              <div className="summary-card-icon" style={{ background: 'rgba(220,38,38,0.1)', color: 'var(--red-600)' }}><Wallet size={20} /></div>
              <div className="summary-card-label">Total Outstanding</div>
              <div className="summary-card-value" style={{ color: 'var(--red-600)' }}>{formatINR(stats.totalOutstanding)}</div>
              <div className="summary-card-accent" style={{ background: 'var(--red-500)' }} />
            </div>
            <div className="summary-card">
              <div className="summary-card-icon" style={{ background: 'rgba(217,119,6,0.1)', color: 'var(--amber-600)' }}><UserCheck size={20} /></div>
              <div className="summary-card-label">Pending Approvals</div>
              <div className="summary-card-value" style={{ color: 'var(--amber-600)' }}>{stats.pendingEnrollmentApprovals}</div>
              <div className="summary-card-accent" style={{ background: 'var(--amber-500)' }} />
            </div>
          </div>

          {/* Tabs */}
          <div style={{ display: 'flex', gap: 6, marginBottom: 20 }}>
            {['overview', 'students', 'fees'].map(t => (
              <button key={t} className={`btn btn-sm ${tab === t ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab(t)} style={{ textTransform: 'capitalize' }}>{t}</button>
            ))}
          </div>

          {/* Pending Approvals */}
          {tab === 'overview' && pendingStudents.length > 0 && (
            <div className="card" style={{ marginBottom: 20, borderLeft: '4px solid var(--amber-500)' }}>
              <div className="card-title" style={{ marginBottom: 12 }}>⚠️ Pending Enrollment Approvals ({pendingStudents.length})</div>
              <div className="table-container">
                <table className="data-table">
                  <thead><tr><th>Student ID</th><th>Name</th><th>Email</th><th>Course</th><th>Action</th></tr></thead>
                  <tbody>
                    {pendingStudents.map(s => (
                      <tr key={s.id}>
                        <td style={{ fontFamily: 'monospace' }}>{s.studentIdNumber}</td>
                        <td style={{ fontWeight: 600 }}>{s.fullName}</td>
                        <td>{s.email}</td>
                        <td>{s.course}</td>
                        <td>
                          <button className="btn btn-primary btn-sm" onClick={() => handleApprove(s.studentIdNumber)}>
                            <CheckCircle size={14} /> Approve
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {tab === 'overview' && pendingStudents.length === 0 && (
            <div className="card" style={{ marginBottom: 20 }}>
              <div className="empty-state" style={{ padding: 24 }}>
                <div className="empty-state-icon">✅</div>
                <div className="empty-state-title">No pending approvals</div>
                <div className="empty-state-text">All student registrations are verified.</div>
              </div>
            </div>
          )}

          {/* Students Tab */}
          {tab === 'students' && (
            <div className="card">
              <div className="card-title" style={{ marginBottom: 12 }}>All Students ({students.length})</div>
              <div className="table-container">
                <table className="data-table">
                  <thead><tr><th>Student ID</th><th>Name</th><th>Email</th><th>Course</th><th>Year</th><th>Status</th><th>Actions</th></tr></thead>
                  <tbody>
                    {students.map(s => (
                      <tr key={s.id}>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{s.studentIdNumber}</td>
                        <td style={{ fontWeight: 600 }}>{s.fullName}</td>
                        <td>{s.email}</td>
                        <td>{s.course}</td>
                        <td>{s.academicYear}</td>
                        <td><span className={`badge ${getStatusBadgeClass(s.status)}`}>{formatStatus(s.status)}</span></td>
                        <td style={{ display: 'flex', gap: 4 }}>
                          {s.status === 'PENDING_VERIFICATION' && (
                            <button className="btn btn-primary btn-sm btn-icon" title="Approve" onClick={() => handleApprove(s.studentIdNumber)}><CheckCircle size={14} /></button>
                          )}
                          <button className="btn btn-danger btn-sm btn-icon" title="Delete" onClick={() => handleDeleteStudent(s.id)}><Trash2 size={14} /></button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Fees Tab */}
          {tab === 'fees' && (
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <div className="card-title">Fee Allocations ({allocations.length})</div>
                <button className="btn btn-primary btn-sm" onClick={() => setShowFeeForm(!showFeeForm)}>
                  <Plus size={14} /> {showFeeForm ? 'Cancel' : 'Allocate Fee'}
                </button>
              </div>

              {showFeeForm && (
                <form onSubmit={handleCreateFee} style={{ background: 'var(--slate-50)', padding: 20, borderRadius: 'var(--radius)', marginBottom: 20 }}>
                  {feeFormError && <div className="alert alert-error"><AlertTriangle size={14} /> {feeFormError}</div>}
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label required">Student ID</label>
                      <input className="form-input" placeholder="STU-2026-001" value={feeForm.studentIdNumber} onChange={e => setFeeForm(f => ({ ...f, studentIdNumber: e.target.value }))} required />
                    </div>
                    <div className="form-group">
                      <label className="form-label required">Fee Category</label>
                      <select className="form-select" value={feeForm.feeCategory} onChange={e => setFeeForm(f => ({ ...f, feeCategory: e.target.value }))}>
                        {['Tuition Fee', 'Hostel Fee', 'Examination Fee', 'Library & Laboratory', 'Transport Fee', 'Miscellaneous'].map(c => <option key={c} value={c}>{c}</option>)}
                      </select>
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label required">Original Amount (₹)</label>
                      <input className="form-input" type="number" min="0" step="0.01" placeholder="50000" value={feeForm.originalAmount} onChange={e => setFeeForm(f => ({ ...f, originalAmount: e.target.value }))} required />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Discount (₹)</label>
                      <input className="form-input" type="number" min="0" step="0.01" placeholder="0" value={feeForm.discountAmount} onChange={e => setFeeForm(f => ({ ...f, discountAmount: e.target.value }))} />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">Academic Year</label>
                      <select className="form-select" value={feeForm.academicYear} onChange={e => setFeeForm(f => ({ ...f, academicYear: e.target.value }))}>
                        <option value="2026-2027">2026–2027</option>
                        <option value="2025-2026">2025–2026</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Due Date</label>
                      <input className="form-input" type="date" value={feeForm.dueDate} onChange={e => setFeeForm(f => ({ ...f, dueDate: e.target.value }))} />
                    </div>
                  </div>
                  <button type="submit" className="btn btn-primary" disabled={feeFormLoading}>
                    {feeFormLoading ? <><span className="spinner-sm" /> Saving…</> : 'Create Fee Allocation'}
                  </button>
                </form>
              )}

              <div className="table-container">
                <table className="data-table">
                  <thead><tr><th>Student ID</th><th>Category</th><th>Year</th><th>Original</th><th>Discount</th><th>Net</th><th>Paid</th><th>Balance</th><th>Status</th><th></th></tr></thead>
                  <tbody>
                    {allocations.map(a => (
                      <tr key={a.id}>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{a.studentIdNumber}</td>
                        <td>{a.feeCategory}</td>
                        <td>{a.academicYear}</td>
                        <td className="amount">{formatINR(a.originalAmount)}</td>
                        <td className="amount">{formatINR(a.discountAmount)}</td>
                        <td className="amount" style={{ fontWeight: 700 }}>{formatINR(a.netObligation)}</td>
                        <td className="amount" style={{ color: 'var(--emerald-600)' }}>{formatINR(a.amountPaid)}</td>
                        <td className="amount" style={{ color: Number(a.remainingBalance) > 0 ? 'var(--red-600)' : 'var(--emerald-600)' }}>{formatINR(a.remainingBalance)}</td>
                        <td><span className={`badge ${getStatusBadgeClass(a.status)}`}>{formatStatus(a.status)}</span></td>
                        <td><button className="btn btn-danger btn-sm btn-icon" title="Delete" onClick={() => handleDeleteAllocation(a.id)}><Trash2 size={14} /></button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </AdminLayout>
  );
}
