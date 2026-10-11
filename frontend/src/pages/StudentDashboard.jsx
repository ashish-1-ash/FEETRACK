import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  IndianRupee, Wallet, TrendingUp, AlertTriangle, Clock,
  CreditCard, FileText, BarChart3, Eye, Download, RefreshCw
} from 'lucide-react';
import StudentLayout from '../components/StudentLayout';
import { useAuth } from '../context/AuthContext';
import { getDashboardSummary } from '../services/feetrackService';
import { formatINR, formatDate, formatDateTime, getStatusBadgeClass, formatStatus, getErrorMessage } from '../utils/helpers';

function SummaryCard({ icon: Icon, label, value, sub, color, accent }) {
  return (
    <div className="summary-card">
      <div className="summary-card-icon" style={{ background: `${color}18`, color }}>
        <Icon size={20} />
      </div>
      <div className="summary-card-label">{label}</div>
      <div className="summary-card-value" style={{ color }}>{value}</div>
      {sub && <div className="summary-card-sub">{sub}</div>}
      <div className="summary-card-accent" style={{ background: color }} />
    </div>
  );
}

export default function StudentDashboard() {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [academicYear, setAcademicYear] = useState('');

  const load = async (year) => {
    setLoading(true);
    setError('');
    try {
      const res = await getDashboardSummary(year || undefined);
      setSummary(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(academicYear); }, [academicYear]);

  const progress = summary ? Number(summary.paymentProgress || 0) : 0;

  return (
    <StudentLayout>
      {/* Header */}
      <div className="page-header-row" style={{ marginBottom: 0 }}>
        <div className="page-header">
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Your complete fee management overview</p>
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <select
            className="filter-select"
            value={academicYear}
            onChange={e => setAcademicYear(e.target.value)}
            aria-label="Filter by academic year"
          >
            <option value="">All Years</option>
            <option value="2026-2027">2026–2027</option>
            <option value="2025-2026">2025–2026</option>
            <option value="2024-2025">2024–2025</option>
          </select>
          <button className="btn btn-secondary btn-sm" onClick={() => load(academicYear)}>
            <RefreshCw size={15} /> Refresh
          </button>
        </div>
      </div>

      {/* Welcome Banner */}
      {summary && (
        <div className="welcome-banner" style={{ marginTop: 20 }}>
          <div className="welcome-greeting">Good day,</div>
          <div className="welcome-name">{summary.studentName || user?.name || 'Student'} 👋</div>
          <div className="welcome-meta">
            <span className="welcome-meta-item">🎓 {summary.course || 'N/A'}</span>
            <span className="welcome-meta-item">📅 {summary.academicYear || 'N/A'}</span>
            <span className="welcome-meta-item">🪪 {summary.studentIdNumber || 'N/A'}</span>
            <span className="welcome-meta-item">
              <span className={`badge ${getStatusBadgeClass(summary.accountStatus)}`} style={{ background: 'rgba(255,255,255,0.2)', color: 'white' }}>
                {formatStatus(summary.accountStatus)}
              </span>
            </span>
          </div>
        </div>
      )}

      {loading && (
        <div className="loading-page" style={{ minHeight: 200 }}>
          <div className="spinner" />
          <p>Loading your dashboard…</p>
        </div>
      )}

      {!loading && error && (
        <div className="alert alert-error" style={{ marginTop: 16 }}>
          <AlertTriangle size={16} /> {error}
        </div>
      )}

      {!loading && summary && (
        <>
          {/* Summary Cards */}
          <div className="summary-cards" style={{ marginTop: 20 }}>
            <SummaryCard icon={IndianRupee} label="Total Fees" value={formatINR(summary.totalFees)} sub="Assigned gross fees" color="var(--slate-700)" accent />
            <SummaryCard icon={TrendingUp} label="Total Paid" value={formatINR(summary.totalPaid)} sub="Verified payments" color="var(--emerald-600)" />
            <SummaryCard icon={Wallet} label="Outstanding" value={formatINR(summary.outstandingBalance)} sub="Remaining balance" color="var(--red-600)" />
            <SummaryCard icon={Clock} label="Upcoming (30d)" value={formatINR(summary.upcomingFees)} sub="Due within 30 days" color="var(--amber-600)" />
            <SummaryCard icon={AlertTriangle} label="Overdue" value={formatINR(summary.overdueFees)} sub="Past due date" color="var(--red-700)" />
            <SummaryCard icon={BarChart3} label="Net Obligation" value={formatINR(summary.netObligation)} sub="After discounts" color="var(--slate-600)" />
          </div>

          {/* Payment Progress */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-header">
              <div className="card-title">Payment Progress</div>
              <div className="card-subtitle">
                Paid {formatINR(summary.totalPaid)} of {formatINR(summary.netObligation)} net obligation
                {summary.totalDiscounts > 0 && ` (includes ${formatINR(summary.totalDiscounts)} in authorized discounts)`}
              </div>
            </div>
            <div className="progress-bar-wrap">
              <div className="progress-bar-label">
                <span>Progress</span>
                <span style={{ color: progress >= 100 ? 'var(--emerald-600)' : 'var(--slate-600)' }}>
                  {progress.toFixed(1)}%
                </span>
              </div>
              <div className="progress-bar-track">
                <div className="progress-bar-fill" style={{ width: `${Math.min(progress, 100)}%` }} />
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div style={{ marginBottom: 8 }}>
            <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--slate-700)', marginBottom: 12 }}>Quick Actions</h2>
          </div>
          <div className="quick-actions" style={{ marginBottom: 24 }}>
            <Link to="/student/fees" className="quick-action-card">
              <div className="quick-action-icon"><Wallet size={20} /></div>
              <div className="quick-action-label">View My Fees</div>
            </Link>
            <Link to="/student/payments" className="quick-action-card">
              <div className="quick-action-icon"><CreditCard size={20} /></div>
              <div className="quick-action-label">Payment History</div>
            </Link>
            <Link to="/student/fee-summary" className="quick-action-card">
              <div className="quick-action-icon"><FileText size={20} /></div>
              <div className="quick-action-label">Fee Summary</div>
            </Link>
            <Link to="/student/profile" className="quick-action-card">
              <div className="quick-action-icon"><BarChart3 size={20} /></div>
              <div className="quick-action-label">My Profile</div>
            </Link>
          </div>

          {/* Bottom Grid */}
          <div className="dashboard-grid">
            {/* Upcoming Installments */}
            <div className="card">
              <div className="card-header">
                <div className="card-title">📅 Upcoming Installments</div>
                <div className="card-subtitle">Due within the next 30 days</div>
              </div>
              {summary.upcomingInstallments?.length > 0 ? (
                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Fee Category</th>
                        <th>Instalment #</th>
                        <th>Amount</th>
                        <th>Due Date</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {summary.upcomingInstallments.map((inst, i) => (
                        <tr key={i}>
                          <td>{inst.feeCategory}</td>
                          <td>#{inst.installmentNumber}</td>
                          <td className="amount">{formatINR(inst.amount)}</td>
                          <td>{formatDate(inst.dueDate)}</td>
                          <td><span className={`badge ${getStatusBadgeClass(inst.status)}`}>{formatStatus(inst.status)}</span></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="empty-state">
                  <div className="empty-state-icon">✅</div>
                  <div className="empty-state-title">No upcoming instalments</div>
                  <div className="empty-state-text">No payments due in the next 30 days.</div>
                </div>
              )}
            </div>

            {/* Recent Payments */}
            <div className="card">
              <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div className="card-title">💳 Recent Transactions</div>
                  <div className="card-subtitle">Your latest payment activity</div>
                </div>
                <Link to="/student/payments" className="btn btn-ghost btn-sm">View all</Link>
              </div>
              {summary.recentPayments?.length > 0 ? (
                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Category</th>
                        <th>Amount</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {summary.recentPayments.map((pay) => (
                        <tr key={pay.id}>
                          <td style={{ fontSize: '0.8rem' }}>{formatDateTime(pay.paymentDate)}</td>
                          <td>{pay.feeCategory || 'Fee Payment'}</td>
                          <td className="amount">{formatINR(pay.amount)}</td>
                          <td><span className={`badge ${getStatusBadgeClass(pay.status)}`}>{formatStatus(pay.status)}</span></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="empty-state">
                  <div className="empty-state-icon">🧾</div>
                  <div className="empty-state-title">No transactions yet</div>
                  <div className="empty-state-text">Your payment history will appear here.</div>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </StudentLayout>
  );
}
