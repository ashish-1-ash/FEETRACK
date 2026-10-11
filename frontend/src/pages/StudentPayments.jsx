import { useEffect, useState } from 'react';
import { AlertTriangle, RefreshCw, Download, Eye, Receipt } from 'lucide-react';
import StudentLayout from '../components/StudentLayout';
import { getStudentPayments, getReceiptByPaymentId } from '../services/feetrackService';
import { formatINR, formatDateTime, getStatusBadgeClass, formatStatus, getErrorMessage } from '../utils/helpers';

const YEAR_OPTIONS = ['', '2026-2027', '2025-2026', '2024-2025'];
const PAGE_SIZE = 10;

export default function StudentPayments() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [academicYear, setAcademicYear] = useState('');
  const [page, setPage] = useState(1);
  const [receiptLoading, setReceiptLoading] = useState({});
  const [receiptError, setReceiptError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    setPage(1);
    try {
      const res = await getStudentPayments(academicYear ? { academicYear } : {});
      setPayments(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [academicYear]);

  const handleViewReceipt = async (paymentId, txnRef) => {
    setReceiptLoading(prev => ({ ...prev, [paymentId]: true }));
    setReceiptError('');
    try {
      const res = await getReceiptByPaymentId(paymentId);
      const receipt = res.data;
      // Display receipt info in an alert (in production this would open a modal or download PDF)
      alert(
        `📄 Fee Receipt\n\n` +
        `Receipt No: ${receipt.receiptNumber}\n` +
        `Transaction Ref: ${receipt.transactionReference}\n` +
        `Student: ${receipt.studentName}\n` +
        `Category: ${receipt.feeCategory}\n` +
        `Amount: ₹${receipt.amount}\n` +
        `Payment Method: ${receipt.paymentMethod}\n` +
        `Academic Year: ${receipt.academicYear}\n` +
        `Issued: ${new Date(receipt.issueDate).toLocaleString('en-IN')}`
      );
    } catch (err) {
      setReceiptError(`Receipt unavailable for transaction ${txnRef}: ${getErrorMessage(err)}`);
    } finally {
      setReceiptLoading(prev => ({ ...prev, [paymentId]: false }));
    }
  };

  // Pagination
  const totalPages = Math.ceil(payments.length / PAGE_SIZE);
  const paginated = payments.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <StudentLayout>
      <div className="page-header-row">
        <div className="page-header">
          <h1 className="page-title">Payment History</h1>
          <p className="page-subtitle">All payment transactions on your student account</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}>
          <RefreshCw size={15} /> Refresh
        </button>
      </div>

      {/* Filters */}
      <div className="filters-row">
        <select className="filter-select" value={academicYear} onChange={e => setAcademicYear(e.target.value)} aria-label="Academic year filter">
          {YEAR_OPTIONS.map(y => <option key={y} value={y}>{y || 'All Years'}</option>)}
        </select>
      </div>

      {receiptError && (
        <div className="alert alert-error">
          <AlertTriangle size={16} /> {receiptError}
        </div>
      )}

      {loading && <div className="loading-page"><div className="spinner" /><p>Loading transactions…</p></div>}
      {!loading && error && <div className="alert alert-error"><AlertTriangle size={16} /> {error}</div>}

      {!loading && !error && (
        <div className="card">
          {payments.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">🧾</div>
              <div className="empty-state-title">No transactions found</div>
              <div className="empty-state-text">
                {academicYear ? `No payments recorded for ${academicYear}.` : 'No payment history on your account yet.'}
              </div>
            </div>
          ) : (
            <>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <div className="card-title">Transactions ({payments.length})</div>
              </div>
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Date & Time</th>
                      <th>Transaction Ref</th>
                      <th>Fee Category</th>
                      <th>Payment Method</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Receipt</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginated.map(pay => (
                      <tr key={pay.id}>
                        <td style={{ fontSize: '0.825rem', whiteSpace: 'nowrap' }}>{formatDateTime(pay.paymentDate)}</td>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: 'var(--slate-600)' }}>{pay.transactionReference}</td>
                        <td>{pay.feeCategory || 'Fee Payment'}</td>
                        <td>
                          <span className="badge badge-neutral">{pay.paymentMethod || 'N/A'}</span>
                        </td>
                        <td className="amount" style={{ color: 'var(--emerald-700)' }}>{formatINR(pay.amount)}</td>
                        <td>
                          <span className={`badge ${getStatusBadgeClass(pay.status)}`}>{formatStatus(pay.status)}</span>
                        </td>
                        <td>
                          {pay.status === 'SUCCESS' ? (
                            <button
                              className="btn btn-ghost btn-sm btn-icon"
                              title="View receipt"
                              onClick={() => handleViewReceipt(pay.id, pay.transactionReference)}
                              disabled={receiptLoading[pay.id]}
                              aria-label="View receipt"
                            >
                              {receiptLoading[pay.id] ? <span className="spinner-sm" style={{ borderTopColor: 'var(--emerald-600)', borderColor: 'var(--slate-200)' }} /> : <Receipt size={16} color="var(--emerald-600)" />}
                            </button>
                          ) : (
                            <span style={{ fontSize: '0.75rem', color: 'var(--slate-400)' }}>—</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="pagination">
                  <button className="pagination-btn" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>← Prev</button>
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                    <button key={n} className={`pagination-btn${page === n ? ' active' : ''}`} onClick={() => setPage(n)}>{n}</button>
                  ))}
                  <button className="pagination-btn" onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>Next →</button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </StudentLayout>
  );
}
