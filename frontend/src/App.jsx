import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { StudentRoute, AdminRoute, PublicRoute } from './routes/ProtectedRoutes';

// Pages
import StudentLogin from './pages/StudentLogin';
import StudentRegister from './pages/StudentRegister';
import StudentDashboard from './pages/StudentDashboard';
import StudentFees from './pages/StudentFees';
import StudentPayments from './pages/StudentPayments';
import StudentFeeSummary from './pages/StudentFeeSummary';
import StudentProfile from './pages/StudentProfile';
import AdminLogin from './pages/AdminLogin';
import AdminDashboard from './pages/AdminDashboard';
import Unauthorized from './pages/Unauthorized';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Root redirect */}
          <Route path="/" element={<Navigate to="/student/login" replace />} />

          {/* Student Auth (Public) */}
          <Route path="/student/login" element={
            <PublicRoute redirectIfAuth>
              <StudentLogin />
            </PublicRoute>
          } />
          <Route path="/student/register" element={
            <PublicRoute redirectIfAuth>
              <StudentRegister />
            </PublicRoute>
          } />

          {/* Student Portal (Protected) */}
          <Route path="/student/dashboard" element={
            <StudentRoute>
              <StudentDashboard />
            </StudentRoute>
          } />
          <Route path="/student/fees" element={
            <StudentRoute>
              <StudentFees />
            </StudentRoute>
          } />
          <Route path="/student/payments" element={
            <StudentRoute>
              <StudentPayments />
            </StudentRoute>
          } />
          <Route path="/student/fee-summary" element={
            <StudentRoute>
              <StudentFeeSummary />
            </StudentRoute>
          } />
          <Route path="/student/profile" element={
            <StudentRoute>
              <StudentProfile />
            </StudentRoute>
          } />

          {/* Admin Auth (Public) */}
          <Route path="/admin/login" element={
            <PublicRoute redirectIfAuth>
              <AdminLogin />
            </PublicRoute>
          } />

          {/* Admin Portal (Protected) */}
          <Route path="/admin/dashboard" element={
            <AdminRoute>
              <AdminDashboard />
            </AdminRoute>
          } />

          {/* Unauthorized */}
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/student/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
