import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { LogIn, Loader2, Shield } from "lucide-react";
import { authService } from "@/services/authService";

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [loading, setLoading] = useState(false);

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
          setTimeout(() => navigate('/admin'), 500);
        } else {
          toast.success("Login successful!");
          navigate("/dashboard");
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

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
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
    </div>
  );
};

export default Login;
