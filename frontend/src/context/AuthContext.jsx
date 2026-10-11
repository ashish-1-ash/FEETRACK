import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { getMe } from '../services/feetrackService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount, rehydrate session from sessionStorage
  useEffect(() => {
    const token = sessionStorage.getItem('ft_token');
    const stored = sessionStorage.getItem('ft_user');
    if (token && stored) {
      try {
        const parsed = JSON.parse(stored);
        setUser(parsed);
      } catch {
        sessionStorage.clear();
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback((token, userData) => {
    sessionStorage.setItem('ft_token', token);
    sessionStorage.setItem('ft_user', JSON.stringify(userData));
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem('ft_token');
    sessionStorage.removeItem('ft_user');
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const res = await getMe();
      sessionStorage.setItem('ft_user', JSON.stringify(res.data));
      setUser(res.data);
    } catch {
      logout();
    }
  }, [logout]);

  const isAuthenticated = !!user;
  const isStudent = user?.role === 'ROLE_STUDENT';
  const isAdmin = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_ACCOUNTANT';

  return (
    <AuthContext.Provider value={{ user, loading, isAuthenticated, isStudent, isAdmin, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
