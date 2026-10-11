import { useState, useEffect } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard, Wallet, CreditCard, FileText, User,
  LogOut, Menu, X, IndianRupee, ChevronRight, Bell
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { logout as apiLogout } from '../services/feetrackService';
import { getInitials } from '../utils/helpers';

const navLinks = [
  { to: '/student/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/student/fees', icon: Wallet, label: 'My Fees' },
  { to: '/student/payments', icon: CreditCard, label: 'Payment History' },
  { to: '/student/fee-summary', icon: FileText, label: 'Fee Summary' },
  { to: '/student/profile', icon: User, label: 'My Profile' },
];

export default function StudentLayout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  // Close sidebar on route change (mobile)
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await apiLogout();
    } catch { /* swallow */ }
    logout();
    navigate('/student/login', { replace: true });
  };

  const initials = getInitials(user?.name);

  return (
    <div className="student-layout">
      {/* Mobile sidebar toggle */}
      <button className="sidebar-toggle" onClick={() => setSidebarOpen(!sidebarOpen)} aria-label="Toggle navigation">
        {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
      </button>

      {/* Sidebar Overlay (mobile) */}
      {sidebarOpen && (
        <div
          style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 99 }}
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`sidebar${sidebarOpen ? ' open' : ''}`}>
        {/* Logo */}
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon">
            <IndianRupee size={20} strokeWidth={2.5} />
          </div>
          <div>
            <div className="sidebar-logo-text">FeeTrack</div>
            <div className="sidebar-logo-sub">Student Portal</div>
          </div>
        </div>

        {/* User Info */}
        <div className="sidebar-user">
          <div className="sidebar-user-info">
            <div className="sidebar-user-avatar">{initials}</div>
            <div>
              <div className="sidebar-user-name">{user?.name || 'Student'}</div>
              <div className="sidebar-user-role">
                {user?.studentProfileId || 'Student Portal'}
              </div>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="sidebar-nav">
          <div className="sidebar-section-title">Navigation</div>
          {navLinks.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
            >
              <Icon size={18} />
              {label}
              <ChevronRight size={14} style={{ marginLeft: 'auto', opacity: 0.5 }} />
            </NavLink>
          ))}
        </nav>

        {/* Logout */}
        <div className="sidebar-footer">
          <button className="sidebar-logout" onClick={handleLogout} disabled={loggingOut}>
            <LogOut size={18} />
            {loggingOut ? 'Logging out…' : 'Logout'}
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
