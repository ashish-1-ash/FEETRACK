import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token from sessionStorage on every request
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('ft_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle 401 / 403 globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('ft_token');
      sessionStorage.removeItem('ft_user');
      // Only redirect if not already on auth page
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
        window.location.href = '/student/login?expired=1';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
