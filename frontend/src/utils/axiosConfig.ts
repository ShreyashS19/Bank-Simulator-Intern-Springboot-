// import axios from 'axios';

// const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// // ─── Request Interceptor ── attach JWT token to every request ───────────────
// axios.interceptors.request.use(
//   (config) => {
//     const token = localStorage.getItem('token');
//     if (token) {
//       config.headers['Authorization'] = `Bearer ${token}`;
//     }
//     return config;
//   },
//   (error) => Promise.reject(error)
// );

// // ─── Response Interceptor ── handle 401 Unauthorized ────────────────────────
// axios.interceptors.response.use(
//   (response) => response,
//   (error) => {
//     if (error.response?.status === 401) {
//       // Token expired or invalid — clear session and redirect to login
//       localStorage.removeItem('token');
//       localStorage.removeItem('user');
//       localStorage.removeItem('isAuthenticated');
//       localStorage.removeItem('isAdmin');
//       localStorage.removeItem('userEmail');
//       localStorage.removeItem('hasCustomerRecord');

//       if (window.location.pathname !== '/login' && window.location.pathname !== '/signup') {
//         window.location.href = '/login';
//       }
//     }
//     return Promise.reject(error);
//   }
// );

// export { API_BASE_URL };
// export default axios;
import axios from 'axios';
import * as Sentry from "@sentry/react";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// ─── Request Interceptor ── attach JWT token to every request ───────────────
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response Interceptor ── handle errors ──────────────────────────────────
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    // Report 500+ server errors or connection/timeout errors to Sentry
    const isNetworkError = !error.response;
    const isServerError = error.response && error.response.status >= 500;
    
    if (isNetworkError || isServerError) {
      Sentry.captureException(error, {
        tags: {
          errorType: isNetworkError ? 'network' : 'server',
        },
        extra: {
          url: error.config?.url,
          method: error.config?.method,
          status: error.response?.status,
          message: error.message,
        },
      });
    }

    // Handle 401 Unauthorized — clear session and redirect
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('isAuthenticated');
      localStorage.removeItem('isAdmin');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('hasCustomerRecord');

      if (window.location.pathname !== '/login' && window.location.pathname !== '/signup') {
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

export { API_BASE_URL };
export default axios;