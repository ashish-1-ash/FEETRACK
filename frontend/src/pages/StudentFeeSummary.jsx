import { useEffect, useState } from 'react';
import { AlertTriangle, Download, RefreshCw, FileText, Printer } from 'lucide-react';
import StudentLayout from '../components/StudentLayout';
import { getFeeSummary, downloadFeeSummaryPdf } from '../services/feetrackService';
import { formatINR, formatDate, formatDateTime, getStatusBadgeClass, formatStatus, getErrorMessage } from '../utils/helpers';

const YEAR_OPTIONS = ['', '2026-2027', '2025-2026', '2024-2025'];

export default function StudentFeeSummary() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [academicYear, setAcademicYear] = useState('');
  const [downloading, setDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getFeeSummary(academicYear || undefined);
      setData(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [academicYear]);

  const handleDownloadPdf = async () => {
    setDownloading(true);
    setDownloadError('');
    try {
      await downloadFeeSummaryPdf(academicYear || undefined);
    } catch (err) {
      setDownloadError(getErrorMessage(err));
    } finally {
      setDownloading(false);
    }
  };

  const handlePrint = () => window.print();

  return (
    <StudentLayout>
      <div className="page-header-row">
        <div className="page-header">
          <h1 className="page-title">Fee Summary Statement</h1>
          <p className="page-subtitle">Comprehensive fee statement for your records</p>
        </div>
        <div className="page-actions">
          <select className="filter-select" value={academicYear} onChange={e => setAcademicYear(e.target.value)} aria-label="Academic year">
            {YEAR_OPTIONS.map(y => <option key={y} value={y}>{y || 'All Years'}</option>)}
          </select>
          <button className="btn btn-secondary btn-sm" onClick={load}><RefreshCw size={15} /> Refresh</button>
          <button className="btn btn-secondary btn-sm" onClick={handlePrint}><Printer size={15} /> Print</button>
          <button className="btn btn-primary btn-sm" onClick={handleDownloadPdf} disabled={downloading || loading}>
            {downloading ? <><span className="spinner-sm" /> Generating…</> : <><Download size={15} /> Download Fee Summary PDF</>}
          </button>
        </div>
      </div>

      {downloadError && <div className="alert alert-error"><AlertTriangle size={16} /> {downloadError}</div>}
      {loading && <div className="loading-page"><div className="spinner" /><p>Loading fee summary…</p></div>}
      {!loading && error && <div className="alert alert-error"><AlertTriangle size={16} /> {error}</div>}

      {!loading && data && (
        <div id="fee-summary-print" style={{ maxWidth: 900, margin: '0 auto' }}>
          {/* Institution Header */}
          <div className="card" style={{ marginBottom: 20, textAlign: 'center', background: 'linear-gradient(135deg, var(--emerald-700), var(--emerald-500))', color: 'white', border: 'none' }}>
            <h2 style={{ fontSize: '1.3rem', fontWeight: 700, marginBottom: 4 }}>{data.institutionName}</h2>
            <p style={{ fontSize: '0.85rem', opacity: 0.85 }}>{data.institutionAddress}</p>
            <p style={{ fontSize: '0.8rem', opacity: 0.75 }}>{data.institutionContact}</p>
            <div style={{ marginTop: 16, borderTop: '1px solid rgba(255,255,255,0.2)', paddingTop: 12 }}>
              <FileText size={20} style={{ display: 'inline-block', verticalAlign: 'middle', marginRight: 8 }} />
              <span style={{ fontSize: '1.05rem', fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase' }}>Student Fee Statement</span>
            </div>
          </div>

          {/* Student Details */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-title" style={{ marginBottom: 14 }}>Student Information</div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16 }}>
              {[
                { label: 'Full Name', value: data.studentFullName },
                { label: 'Student ID', value: data.studentIdNumber },
                { label: 'Programme', value: data.course },
                { label: 'Academic Year', value: data.academicYear },
                { label: 'Report Generated', value: formatDateTime(data.reportGeneratedAt) },
              ].map(({ label, value }) => (
                <div key={label}>
                  <div style={{ fontSize: '0.72rem', fontWeight: 600, color: 'var(--slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 2 }}>{label}</div>
                  <div style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--slate-800)' }}>{value || 'N/A'}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Financial Overview */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-title" style={{ marginBottom: 14 }}>Financial Summary</div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(170px, 1fr))', gap: 12 }}>
              {[
                { label: 'Total Assigned Fees', value: formatINR(data.totalAssignedFees), color: 'var(--slate-700)' },
                { label: 'Authorized Discounts', value: formatINR(data.totalDiscounts), color: 'var(--emerald-600)' },
                { label: 'Net Fee Obligation', value: formatINR(data.netObligation), color: 'var(--slate-800)', bold: true },
                { label: 'Total Verified Paid', value: formatINR(data.totalPaid), color: 'var(--emerald-600)' },
                { label: 'Outstanding Balance', value: formatINR(data.outstandingBalance), color: Number(data.outstandingBalance) > 0 ? 'var(--red-600)' : 'var(--emerald-600)', bold: true },
                { label: 'Upcoming (30 days)', value: formatINR(data.upcomingFees), color: 'var(--amber-600)' },
                { label: 'Overdue Amount', value: formatINR(data.overdueFees), color: 'var(--red-700)' },
                { label: 'Payment Progress', value: `${Number(data.paymentProgress || 0).toFixed(1)}%`, color: 'var(--emerald-600)', bold: true },
              ].map(({ label, value, color, bold }) => (
                <div key={label} style={{ padding: '12px', background: 'var(--slate-50)', borderRadius: 'var(--radius)', borderLeft: `3px solid ${color}` }}>
                  <div style={{ fontSize: '0.7rem', fontWeight: 600, color: 'var(--slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 4 }}>{label}</div>
                  <div style={{ fontSize: bold ? '1.1rem' : '1rem', fontWeight: bold ? 700 : 600, color }}>{value}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Fee Allocations Table */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-title" style={{ marginBottom: 14 }}>Fee Allocation Details</div>
            {data.feeAllocations?.length > 0 ? (
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Fee Category</th>
                      <th>Original Fee</th>
                      <th>Discounts</th>
                      <th>Net Fee</th>
                      <th>Paid</th>
                      <th>Balance</th>
                      <th>Due Date</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.feeAllocations.map(fa => (
                      <tr key={fa.id}>
                        <td style={{ fontWeight: 600 }}>{fa.feeCategory}</td>
                        <td className="amount">{formatINR(fa.originalAmount)}</td>
                        <td className="amount" style={{ color: 'var(--emerald-600)' }}>{formatINR(fa.discountAmount)}</td>
                        <td className="amount" style={{ fontWeight: 700 }}>{formatINR(fa.netObligation)}</td>
                        <td className="amount" style={{ color: 'var(--emerald-600)' }}>{formatINR(fa.amountPaid)}</td>
                        <td className="amount" style={{ color: Number(fa.remainingBalance) > 0 ? 'var(--red-600)' : 'var(--emerald-600)', fontWeight: 700 }}>{formatINR(fa.remainingBalance)}</td>
                        <td>{formatDate(fa.dueDate)}</td>
                        <td><span className={`badge ${getStatusBadgeClass(fa.status)}`}>{formatStatus(fa.status)}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state"><div className="empty-state-title">No fee allocations recorded.</div></div>
            )}
          </div>

          {/* Payment History Table */}
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-title" style={{ marginBottom: 14 }}>Payment Transaction History</div>
            {data.paymentHistory?.length > 0 ? (
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Date & Time</th>
                      <th>Transaction Ref</th>
                      <th>Fee Category</th>
                      <th>Method</th>
                      <th>Amount</th>
                      <th>Receipt</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.paymentHistory.map(pay => (
                      <tr key={pay.id}>
                        <td style={{ fontSize: '0.825rem' }}>{formatDateTime(pay.paymentDate)}</td>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{pay.transactionReference}</td>
                        <td>{pay.feeCategory || 'Fee Payment'}</td>
                        <td><span className="badge badge-neutral">{pay.paymentMethod}</span></td>
                        <td className="amount" style={{ color: 'var(--emerald-700)' }}>{formatINR(pay.amount)}</td>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--slate-500)' }}>{pay.receiptReference || '—'}</td>
                        <td><span className={`badge ${getStatusBadgeClass(pay.status)}`}>{formatStatus(pay.status)}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state"><div className="empty-state-title">No payment transactions recorded.</div></div>
            )}
          </div>

          {/* Footer Notice */}
          <div style={{ textAlign: 'center', padding: '16px', fontSize: '0.8rem', color: 'var(--slate-400)', fontStyle: 'italic' }}>
            This is a system-generated fee statement from FeeTrack. For discrepancies or queries, contact the accounts department.
          </div>
        </div>
      )}
    </StudentLayout>
  );
}
