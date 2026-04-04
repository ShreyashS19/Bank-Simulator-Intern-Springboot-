import axios from 'axios';
import '../utils/axiosConfig'; // Initialize interceptors

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// ─── Interfaces ──────────────────────────────────────────────────────────────

export interface SignupRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: string;
  fullName: string;
  email: string;
  role: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    token: string;
    user: User;
  };
  timestamp: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data: User;
  timestamp: string;
}

export interface CustomerCheckResponse {
  success: boolean;
  message: string;
  data: {
    hasCustomerRecord: boolean;
    userId: string;
    email: string;
  };
  timestamp: string;
}

export interface UserStatus {
  email: string;
  active: boolean;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// ─── Token Utilities ─────────────────────────────────────────────────────────

export const tokenUtils = {
  /**
   * Get the current JWT token from localStorage.
   */
  getToken: (): string | null => localStorage.getItem('token'),

  /**
   * Check if user is currently authenticated (has a valid token in storage).
   */
  isAuthenticated: (): boolean => !!localStorage.getItem('token'),

  /**
   * Get the stored user object.
   */
  getUser: (): User | null => {
    try {
      const userStr = localStorage.getItem('user');
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },

  /**
   * Check if logged-in user is an admin (role === 'ADMIN').
   */
  isAdmin: (): boolean => {
    const user = tokenUtils.getUser();
    return user?.role === 'ADMIN';
  },

  /**
   * Clear all auth-related localStorage entries.
   */
  clearSession: (): void => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('hasCustomerRecord');
  },

  /**
   * Persist session data after successful login.
   */
  saveSession: (token: string, user: User): void => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    // Legacy compat flags (for existing route guards)
    localStorage.setItem('isAuthenticated', 'true');
    localStorage.setItem('isAdmin', user.role === 'ADMIN' ? 'true' : 'false');
    localStorage.setItem('userEmail', user.email);
  },
};

// ─── Auth Service ─────────────────────────────────────────────────────────────

export const authService = {
  signup: async (request: SignupRequest): Promise<AuthResponse> => {
    const response = await axios.post<AuthResponse>(`${API_BASE_URL}/auth/signup`, request);
    return response.data;
  },

  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await axios.post<LoginResponse>(`${API_BASE_URL}/auth/login`, request);

    if (response.data.success && response.data.data) {
      const { token, user } = response.data.data;
      tokenUtils.saveSession(token, user);
    }

    return response.data;
  },

  logout: (): void => {
    tokenUtils.clearSession();
  },

  checkCustomerExists: async (email: string): Promise<CustomerCheckResponse> => {
    const response = await axios.get<CustomerCheckResponse>(
      `${API_BASE_URL}/auth/check-customer?email=${encodeURIComponent(email)}`
    );
    return response.data;
  },

  getAllUsers: async (): Promise<ApiResponse<User[]>> => {
    const response = await axios.get<ApiResponse<User[]>>(`${API_BASE_URL}/auth/users/all`);
    return response.data;
  },

  updateUserStatus: async (email: string, active: boolean): Promise<ApiResponse> => {
    const response = await axios.put(
      `${API_BASE_URL}/auth/user/status?email=${encodeURIComponent(email)}&active=${active}`
    );
    return response.data;
  },

  getUserByEmail: async (email: string): Promise<ApiResponse<User>> => {
    const response = await axios.get(
      `${API_BASE_URL}/auth/user?email=${encodeURIComponent(email)}`
    );
    return response.data;
  },
};

export default authService;
