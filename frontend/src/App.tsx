import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import Customers from "./pages/Customers";
import Accounts from "./pages/Accounts";
import Transactions from "./pages/Transactions";
import AdminDashboard from "./pages/AdminDashboard";
import NotFound from "./pages/NotFound";

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

          {/* Admin Only */}
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminDashboard />
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

          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
