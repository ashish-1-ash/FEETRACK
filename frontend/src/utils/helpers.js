/**
 * Format a number as INR currency
 */
export function formatINR(amount) {
  if (amount == null || isNaN(Number(amount))) return '₹0.00';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
  }).format(Number(amount));
}

/**
 * Format a date string or ISO date to DD-MMM-YYYY
 */
export function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return String(dateStr);
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

/**
 * Format a datetime string to DD-MMM-YYYY HH:mm
 */
export function formatDateTime(dateStr) {
  if (!dateStr) return 'N/A';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return String(dateStr);
  return d.toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', hour12: true,
  });
}

/**
 * Get status badge class
 */
export function getStatusBadgeClass(status) {
  if (!status) return 'badge-neutral';
  const s = status.toUpperCase();
  if (s === 'PAID' || s === 'SUCCESS' || s === 'ACTIVE') return 'badge-success';
  if (s === 'PARTIALLY_PAID' || s === 'PENDING') return 'badge-warning';
  if (s === 'OVERDUE' || s === 'FAILED' || s === 'BLOCKED') return 'badge-danger';
  if (s === 'PENDING_VERIFICATION') return 'badge-info';
  return 'badge-neutral';
}

/**
 * Get human-readable status label
 */
export function formatStatus(status) {
  if (!status) return 'Unknown';
  return status
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

/**
 * Calculate days until or overdue
 */
export function daysUntil(dateStr) {
  if (!dateStr) return null;
  const diff = new Date(dateStr) - new Date();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

/**
 * Get initials from name
 */
export function getInitials(name) {
  if (!name) return 'U';
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

/**
 * Extract error message from axios error
 */
export function getErrorMessage(error) {
  if (!error) return 'An unexpected error occurred';
  if (error.response?.data?.message) return error.response.data.message;
  if (error.response?.data?.error) return error.response.data.error;
  if (error.message) return error.message;
  return 'An unexpected error occurred';
}
