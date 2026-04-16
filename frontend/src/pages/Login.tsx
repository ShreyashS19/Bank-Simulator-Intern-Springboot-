import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { Chrome, LogIn, Loader2, Shield, ArrowLeft, KeyRound } from "lucide-react";
import { authService } from "@/services/authService";
import LoginTransitionOverlay from "@/components/LoginTransitionOverlay";

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [loading, setLoading] = useState(false);
  
  // Forgot Password state
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [forgotPasswordStep, setForgotPasswordStep] = useState<1 | 2>(1);
  const [forgotEmail, setForgotEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");

  // Transition overlay state
  const [transition, setTransition] = useState<{ show: boolean; userName: string; target: string }>({
    show: false,
    userName: '',
    target: '/dashboard',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.email || !formData.password) {
      toast.error("Please fill in all fields");
      return;
    }

    setLoading(true);

    try {
      // authService.login() now calls the backend JWT endpoint and auto-saves token + user
      const response = await authService.login({
        email: formData.email,
        password: formData.password
      });

      if (response.success && response.data) {
        const { user } = response.data;
        const isAdminUser = user.role === 'ADMIN';

        // Check if customer record exists (for routing purposes)
        try {
          const customerCheck = await authService.checkCustomerExists(formData.email);
          const hasCustomerRec = customerCheck.success && customerCheck.data.hasCustomerRecord;
          localStorage.setItem('hasCustomerRecord', String(hasCustomerRec));
        } catch {
          localStorage.setItem('hasCustomerRecord', 'false');
        }

        if (isAdminUser) {
          toast.success('Admin login successful!', {
            description: 'Redirecting to admin dashboard...',
            icon: <Shield className="h-4 w-4" />
          });
          setTransition({ show: true, userName: user.fullName || user.email, target: '/admin' });
        } else {
          toast.success("Login successful!");
          setTransition({ show: true, userName: user.fullName || user.email, target: '/dashboard' });
        }
      } else {
        toast.error(response.message || "Login failed");
      }
    } 
    catch (error: any) {
      console.error("Login error:", error);
      
      const errorMessage = error.response?.data?.message || "";
      const statusCode = error.response?.status;
      
      if (statusCode === 404 || errorMessage.toLowerCase().includes("no account found") || 
          errorMessage.toLowerCase().includes("sign up")) {
        toast.error(
          errorMessage || "No account found with this email. Please sign up to create a new account.",
          { 
            duration: 6000,
            action: {
              label: "Sign Up",
              onClick: () => navigate("/signup")
            }
          }
        );
      } else if (statusCode === 403 || errorMessage.toLowerCase().includes("deactivated") || 
          errorMessage.toLowerCase().includes("contact support")) {
        toast.error(errorMessage, { duration: 7000 });
      } else if (errorMessage) {
        toast.error(errorMessage);
      } else if (statusCode === 401) {
        toast.error("Invalid email or password");
      } else if (error.code === 'ERR_NETWORK') {
        toast.error("Cannot connect to server. Ensure backend is running.");
      } else {
        toast.error("Login failed. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleContinue = async () => {
    try {
      const response = await authService.getOAuthProviders();
      if (!response.success || !response.data?.googleConfigured) {
        toast.error(response.data?.configurationMessage || "Google login is not configured on backend.");
        return;
      }

      window.location.href = authService.getGoogleOAuthUrl();
    } catch {
      toast.error("Unable to verify Google OAuth configuration. Please try again.");
    }
  };

  const handleForgotPasswordSubmitEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!forgotEmail) { toast.error("Please enter email"); return; }
    setLoading(true);
    try {
      await authService.forgotPassword(forgotEmail);
      toast.success("OTP sent to your email!");
      setForgotPasswordStep(2);
    } catch (error: any) {
      toast.error(error.message || "Failed to send OTP");
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPasswordSubmitReset = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp || !newPassword) { toast.error("Please fill in all fields"); return; }
    setLoading(true);
    try {
      await authService.resetPassword(forgotEmail, otp, newPassword);
      toast.success("Password reset successful! Please log in.");
      setShowForgotPassword(false);
      setForgotPasswordStep(1);
      setOtp("");
      setNewPassword("");
    } catch (error: any) {
      toast.error(error.message || "Failed to reset password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
      {/* Transition overlay â€” mounts on top of everything after auth */}
      {transition.show && (
        <LoginTransitionOverlay
          userName={transition.userName}
          onComplete={() => navigate(transition.target)}
        />
      )}
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md"
      >
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            Bank Simulation
          </Link>
        </div>

                <AnimatePresence mode="wait">
          {!showForgotPassword ? (
            <motion.div
              key="login"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.2 }}
            >
              <Card>
                <CardHeader>
                  <CardTitle className="text-2xl">Welcome Back</CardTitle>
                  <CardDescription>Enter your credentials to access your account</CardDescription>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="email">Email</Label>
                      <Input
                        id="email"
                        type="email"
                        placeholder="Enter your email"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        required
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="password">Password</Label>
                      <Input
                        id="password"
                        type="password"
                        placeholder="Enter your password"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        required
                      />
                    </div>
                    <div className="flex justify-end mt-1">
                      <Button variant="link" className="px-0 py-0 h-auto font-normal text-xs text-muted-foreground hover:text-primary" onClick={() => setShowForgotPassword(true)} type="button">
                        Forgot password?
                      </Button>
                    </div>
                    <Button type="submit" className="w-full" disabled={loading}>
                      {loading ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Logging in...
                        </>
                      ) : (
                        <>
                          <LogIn className="mr-2 h-4 w-4" />
                          Login
                        </>
                      )}
                    </Button>
      
                    <div className="relative py-1">
                      <div className="absolute inset-0 flex items-center">
                        <span className="w-full border-t" />
                      </div>
                      <div className="relative flex justify-center text-xs uppercase">
                        <span className="bg-background px-2 text-muted-foreground">or</span>
                      </div>
                    </div>
      
                    <Button type="button" variant="outline" className="w-full" disabled={loading} onClick={handleGoogleContinue}>
                      <Chrome className="mr-2 h-4 w-4" />
                      Continue with Google
                    </Button>
                  </form>
      
                  <div className="mt-4 text-center text-sm">
                    <span className="text-muted-foreground">New user? </span>
                    <Link to="/signup" className="text-primary hover:underline font-medium">
                      Sign Up
                    </Link>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          ) : (
            <motion.div
              key="forgot-password"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
            >
              <Card>
                <CardHeader>
                  <div className="flex items-center space-x-2 mb-2">
                    <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full" onClick={() => { setShowForgotPassword(false); setForgotPasswordStep(1); }} type="button">
                      <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <CardTitle className="text-xl">Reset Password</CardTitle>
                  </div>
                  <CardDescription>
                    {forgotPasswordStep === 1 
                      ? "Enter your email to receive a One-Time Password."
                      : "Enter the OTP sent to your email and your new password."}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {forgotPasswordStep === 1 ? (
                    <form onSubmit={handleForgotPasswordSubmitEmail} className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="forgotEmail">Email Address</Label>
                        <Input
                          id="forgotEmail"
                          type="email"
                          placeholder="name@example.com"
                          value={forgotEmail}
                          onChange={(e) => setForgotEmail(e.target.value)}
                          required
                        />
                      </div>
                      <Button type="submit" className="w-full" disabled={loading}>
                        {loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <KeyRound className="mr-2 h-4 w-4" />}
                        {loading ? "Sending..." : "Send OTP"}
                      </Button>
                    </form>
                  ) : (
                    <form onSubmit={handleForgotPasswordSubmitReset} className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="otp">6-Digit OTP</Label>
                        <Input
                          id="otp"
                          type="text"
                          maxLength={6}
                          placeholder="123456"
                          value={otp}
                          onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="newPassword">New Password</Label>
                        <Input
                          id="newPassword"
                          type="password"
                          placeholder="Strong password"
                          value={newPassword}
                          onChange={(e) => setNewPassword(e.target.value)}
                          required
                        />
                      </div>
                      <Button type="submit" className="w-full" disabled={loading}>
                        {loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Shield className="mr-2 h-4 w-4" />}
                        {loading ? "Verifying..." : "Reset Password"}
                      </Button>
                    </form>
                  )}
                </CardContent>
              </Card>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </div>
  );
};

export default Login;

