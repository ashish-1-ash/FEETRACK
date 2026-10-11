import { useEffect, useState } from 'react';
import { AlertTriangle, RefreshCw, ChevronDown } from 'lucide-react';
import StudentLayout from '../components/StudentLayout';
import { getStudentFees } from '../services/feetrackService';
import { formatINR, formatDate, getStatusBadgeClass, formatStatus, getErrorMessage } from '../utils/helpers';

const STATUS_OPTIONS = ['', 'PAID', 'PARTIALLY_PAID', 'PENDING', 'OVERDUE'];
const YEAR_OPTIONS = ['', '2026-2027', '2025-2026', '2024-2025'];

export default function StudentFees() {
  const [fees, setFees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expanded, setExpanded] = useState({});
  const [filters, setFilters] = useState({ academicYear: '', feeCategory: '', status: '' });

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (filters.academicYear) params.academicYear = filters.academicYear;
      if (filters.feeCategory) params.feeCategory = filters.feeCategory;
      if (filters.status) params.status = filters.status;
      const res = await getStudentFees(params);
      setFees(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [filters]);

  const toggleExpand = (id) => setExpanded(prev => ({ ...prev, [id]: !prev[id] }));

  return (
    <StudentLayout>
      <div className="page-header-row">
        <div className="page-header">
          <h1 className="page-title">My Fees</h1>
          <p className="page-subtitle">All fee obligations assigned to your student account</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}>
          <RefreshCw size={15} /> Refresh
        </button>
      </div>

      {/* Filters */}
      <div className="filters-row">
        <select className="filter-select" value={filters.academicYear} onChange={e => setFilters(f => ({ ...f, academicYear: e.target.value }))} aria-label="Academic year">
          {YEAR_OPTIONS.map(y => <option key={y} value={y}>{y || 'All Years'}</option>)}
        </select>
        <select className="filter-select" value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))} aria-label="Status filter">
          {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s ? formatStatus(s) : 'All Statuses'}</option>)}
        </select>
      </div>

      {loading && <div className="loading-page"><div className="spinner" /><p>Loading fee records…</p></div>}
      {!loading && error && <div className="alert alert-error"><AlertTriangle size={16} /> {error}</div>}

      {!loading && !error && fees.length === 0 && (
        <div className="card">
          <div className="empty-state">
            <div className="empty-state-icon">📂</div>
            <div className="empty-state-title">No fee records found</div>
            <div className="empty-state-text">No fee allocations have been assigned to your account yet. Contact the accounts department for assistance.</div>
          </div>
        </div>
      )}

      {!loading && fees.map(fee => (
        <div className="card" key={fee.id} style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12 }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--slate-800)' }}>{fee.feeCategory}</h3>
                <span className={`badge ${getStatusBadgeClass(fee.status)}`}>{formatStatus(fee.status)}</span>
              </div>
              <div style={{ fontSize: '0.825rem', color: 'var(--slate-500)' }}>Academic Year: <strong>{fee.academicYear}</strong></div>
              {fee.dueDate && <div style={{ fontSize: '0.825rem', color: 'var(--slate-500)' }}>Due: <strong>{formatDate(fee.dueDate)}</strong></div>}
            </div>
            <button className="btn btn-ghost btn-sm" onClick={() => toggleExpand(fee.id)} aria-expanded={expanded[fee.id]}>
              {expanded[fee.id] ? 'Collapse' : 'View Instalments'} <ChevronDown size={14} style={{ transform: expanded[fee.id] ? 'rotate(180deg)' : 'none', transition: '0.2s' }} />
            </button>
          </div>

          {/* Fee summary row */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))', gap: 12, marginTop: 16, padding: '16px', background: 'var(--slate-50)', borderRadius: 'var(--radius)' }}>
            {[
              { label: 'Original Fee', value: formatINR(fee.originalAmount) },
              { label: 'Discount', value: formatINR(fee.discountAmount), color: 'var(--emerald-600)' },
              { label: 'Net Obligation', value: formatINR(fee.netObligation), bold: true },
              { label: 'Amount Paid', value: formatINR(fee.amountPaid), color: 'var(--emerald-600)' },
              { label: 'Balance Remaining', value: formatINR(fee.remainingBalance), color: Number(fee.remainingBalance) > 0 ? 'var(--red-600)' : 'var(--emerald-600)', bold: true },
            ].map(({ label, value, color, bold }) => (
              <div key={label}>
                <div style={{ fontSize: '0.72rem', color: 'var(--slate-500)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 2 }}>{label}</div>
                <div style={{ fontSize: '1rem', fontWeight: bold ? 700 : 600, color: color || 'var(--slate-800)' }}>{value}</div>
              </div>
            ))}
          </div>

          {/* Installments */}
          {expanded[fee.id] && fee.installments?.length > 0 && (
            <div style={{ marginTop: 16 }}>
              <h4 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--slate-700)', marginBottom: 10 }}>Instalment Schedule</h4>
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Amount</th>
                      <th>Due Date</th>
                      <th>Paid</th>
                      <th>Paid On</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {fee.installments.map(inst => (
                      <tr key={inst.installmentNumber}>
                        <td>#{inst.installmentNumber}</td>
                        <td className="amount">{formatINR(inst.amount)}</td>
                        <td>{formatDate(inst.dueDate)}</td>
                        <td className="amount">{formatINR(inst.amountPaid)}</td>
                        <td>{inst.paidDate ? formatDate(inst.paidDate) : '—'}</td>
                        <td><span className={`badge ${getStatusBadgeClass(inst.status)}`}>{formatStatus(inst.status)}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
          {expanded[fee.id] && (!fee.installments || fee.installments.length === 0) && (
            <p style={{ marginTop: 12, fontSize: '0.85rem', color: 'var(--slate-400)', fontStyle: 'italic' }}>No instalment schedule configured for this fee.</p>
          )}
        </div>
      ))}
    </StudentLayout>
  );
}
