import api from './api';

// Auth
export const registerStudent = (data) => api.post('/auth/student/register', data);
export const loginStudent = (data) => api.post('/auth/student/login', data);
export const loginAdmin = (data) => api.post('/auth/login', data);
export const logout = () => api.post('/auth/logout');
export const getMe = () => api.get('/auth/me');

// Student Portal
export const getDashboardSummary = (academicYear) =>
  api.get('/student/dashboard/summary', { params: { academicYear } });

export const getStudentProfile = () => api.get('/student/profile');

export const getStudentFees = (params) =>
  api.get('/student/fees', { params });

export const getStudentPayments = (params) =>
  api.get('/student/payments', { params });

export const makeStudentPayment = (data) =>
  api.post('/student/payments', data);

export const getPaymentById = (paymentId) =>
  api.get(`/student/payments/${paymentId}`);

export const getReceiptByPaymentId = (paymentId) =>
  api.get(`/student/payments/${paymentId}/receipt`);

export const getFeeSummary = (academicYear) =>
  api.get('/student/fee-summary', { params: { academicYear } });

export const downloadFeeSummaryPdf = async (academicYear) => {
  const response = await api.get('/student/fee-summary/pdf', {
    params: { academicYear },
    responseType: 'blob',
  });
  const safeYear = academicYear ? academicYear.replace(/[^a-zA-Z0-9-]/g, '_') : 'All-Years';
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `FeeTrack-Fee-Summary-${safeYear}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

// Admin
export const getAdminStats = () => api.get('/admin/dashboard/stats');
export const getAllStudents = () => api.get('/admin/students');
export const getAdminStudentById = (id) => api.get(`/admin/students/${id}`);
export const updateAdminStudent = (id, data) => api.put(`/admin/students/${id}`, data);
export const approveStudent = (studentIdNumber) => api.post(`/admin/students/${studentIdNumber}/approve`);
export const deleteAdminStudent = (id) => api.delete(`/admin/students/${id}`);
export const getAllFeeAllocations = () => api.get('/admin/fee-allocations');
export const getFeeAllocationsForStudent = (sid) => api.get(`/admin/fee-allocations/student/${sid}`);
export const createFeeAllocation = (data) => api.post('/admin/fee-allocations', data);
export const updateFeeAllocation = (id, data) => api.put(`/admin/fee-allocations/${id}`, data);
export const deleteFeeAllocation = (id) => api.delete(`/admin/fee-allocations/${id}`);
export const getAllPayments = () => api.get('/admin/payments');
export const recordPayment = (data) => api.post('/admin/payments', data);
export const deletePayment = (id) => api.delete(`/admin/payments/${id}`);
