import axios from 'axios';

// Ensure baseURL always ends with /api/v1
const getBaseURL = () => {
  let envUrl = import.meta.env.VITE_API_URL;
  if (!envUrl) {
    return '/api/v1';
  }
  // Trim trailing slash
  envUrl = envUrl.trim().replace(/\/+$/, '');
  // If the user provided just the host e.g. https://feetrack-cqrw.onrender.com
  // ensure /api/v1 is appended
  if (!envUrl.endsWith('/api/v1')) {
    envUrl = `${envUrl}/api/v1`;
  }
  return envUrl;
};

const api = axios.create({
  baseURL: getBaseURL(),
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
