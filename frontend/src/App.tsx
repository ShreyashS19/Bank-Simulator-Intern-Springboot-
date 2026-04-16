import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import OAuthSuccess from "./pages/OAuthSuccess";
import Dashboard from "./pages/Dashboard";
import Customers from "./pages/Customers";
import Accounts from "./pages/Accounts";
import Transactions from "./pages/Transactions";
import AdminDashboard from "./pages/AdminDashboard";
import LoanDashboard from "./pages/LoanDashboard";
import AdminLoanManagement from "./pages/AdminLoanManagement";
import NotFound from "./pages/NotFound";
import { LoanApplicationForm } from "./components/LoanApplicationForm";
import ResetPinPage from "./pages/ResetPinPage";

// ─── Import interceptors to initialize globally ───────────────────────────────
import './utils/axiosConfig';

const queryClient = new QueryClient();

// ─── Auth helpers ─────────────────────────────────────────────────────────────
const isAuthenticated = (): boolean => !!localStorage.getItem('token');

const isAdmin = (): boolean => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user?.role === 'ADMIN';
  } catch {
    return false;
  }
};

const hasCustomerRecord = (): boolean =>
  localStorage.getItem('hasCustomerRecord') === 'true';

// ─── Route Guards ─────────────────────────────────────────────────────────────

const AdminRoute = ({ children }: { children: React.ReactNode }) => {
  if (!isAuthenticated()) return <Navigate to="/login" replace />;
  if (!isAdmin()) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
};

const ProtectedCustomerRoute = ({ children }: { children: React.ReactNode }) => {
  if (!isAuthenticated()) return <Navigate to="/login" replace />;
  if (isAdmin()) return <>{children}</>;
  if (hasCustomerRecord()) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
};

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  if (!isAuthenticated()) return <Navigate to="/login" replace />;
  if (isAdmin() && window.location.pathname === '/dashboard') {
    return <Navigate to="/admin" replace />;
  }
  return <>{children}</>;
};

// ─── App ──────────────────────────────────────────────────────────────────────

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/oauth-success" element={<OAuthSuccess />} />

          {/* Admin Only */}
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminDashboard />
              </AdminRoute>
            }
          />

          <Route
            path="/admin/loans"
            element={
              <AdminRoute>
                <AdminLoanManagement />
              </AdminRoute>
            }
          />

          {/* Protected Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/customers"
            element={
              <ProtectedCustomerRoute>
                <Customers />
              </ProtectedCustomerRoute>
            }
          />

          <Route
            path="/accounts"
            element={
              <ProtectedRoute>
                <Accounts />
              </ProtectedRoute>
            }
          />

          <Route
            path="/transactions"
            element={
              <ProtectedRoute>
                <Transactions />
              </ProtectedRoute>
            }
          />

          {/* Loan Routes */}
          <Route
            path="/loans"
            element={
              <ProtectedRoute>
                <LoanDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/loans/apply"
            element={
              <ProtectedRoute>
                <LoanApplicationForm />
              </ProtectedRoute>
            }
          />

          <Route
            path="/reset-pin"
            element={
              <ProtectedRoute>
                <ResetPinPage />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
