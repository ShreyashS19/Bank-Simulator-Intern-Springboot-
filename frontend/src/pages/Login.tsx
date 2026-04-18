import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import {
  LogIn,
  Loader2,
  Shield,
  ArrowLeft,
  KeyRound,
  CheckCircle2,
  Eye,
  EyeOff,
  Mail,
  Lock,
  Sparkles,
} from "lucide-react";
import { authService } from "@/services/authService";
import LoginTransitionOverlay from "@/components/LoginTransitionOverlay";
import {
  validateEmailField,
  validatePasswordField,
} from "@/lib/authValidation";

const GoogleIcon = () => (
  <svg aria-hidden="true" viewBox="0 0 24 24" className="h-5 w-5" role="img">
    <path
      fill="#EA4335"
      d="M12 10.2v3.9h5.4c-.2 1.3-1.6 3.9-5.4 3.9-3.3 0-6-2.7-6-6s2.7-6 6-6c1.9 0 3.1.8 3.8 1.4l2.6-2.5C16.8 3.4 14.6 2.5 12 2.5 6.9 2.5 2.8 6.6 2.8 11.7s4.1 9.2 9.2 9.2c5.3 0 8.8-3.7 8.8-8.9 0-.6-.1-1.1-.2-1.8H12z"
    />
    <path
      fill="#34A853"
      d="M3.8 7.4l3.2 2.4c.9-1.8 2.8-3 5-3 1.9 0 3.1.8 3.8 1.4l2.6-2.5C16.8 3.4 14.6 2.5 12 2.5c-3.5 0-6.6 2-8.2 4.9z"
    />
    <path
      fill="#FBBC05"
      d="M12 20.9c2.5 0 4.6-.8 6.2-2.2l-3-2.4c-.8.6-1.9 1-3.2 1-2.4 0-4.4-1.6-5.1-3.8l-3.2 2.5c1.6 3 4.7 4.9 8.3 4.9z"
    />
    <path
      fill="#4285F4"
      d="M20.8 12c0-.6-.1-1.1-.2-1.8H12v3.9h5.4c-.3 1.2-1 2.2-2.1 2.9l3 2.4c1.8-1.6 2.8-4 2.8-7.4z"
    />
  </svg>
);

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
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
    userName: "",
    target: "/dashboard",
  });
  const [authSuccess, setAuthSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [loginTouched, setLoginTouched] = useState({
    email: false,
    password: false,
  });
  const [loginErrors, setLoginErrors] = useState({
    email: "",
    password: "",
  });

  useEffect(() => {
    document.documentElement.classList.remove("custom-cursor-active");
  }, []);

  const isLoginFormValid =
    !validateEmailField(formData.email) && formData.password.trim().length > 0;

  const getLoginFieldError = (field: "email" | "password", value: string): string => {
    if (field === "email") {
      return validateEmailField(value);
    }

    return "";
  };

  const handleLoginFieldChange = (field: "email" | "password", value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setLoginTouched((prev) => ({ ...prev, [field]: true }));
    setLoginErrors((prev) => ({ ...prev, [field]: getLoginFieldError(field, value) }));
    if (field === "password") {
      setAuthSuccess(false);
    }
  };

  const handleLoginFieldBlur = (field: "email" | "password") => {
    const value = field === "email" ? formData.email : formData.password;
    setLoginTouched((prev) => ({ ...prev, [field]: true }));
    setLoginErrors((prev) => ({ ...prev, [field]: getLoginFieldError(field, value) }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const emailError = validateEmailField(formData.email);
    const passwordError = "";

    setLoginTouched({ email: true, password: true });
    setLoginErrors({ email: emailError, password: passwordError });

    if (emailError) {
      toast.error("Please fix validation errors before continuing");
      return;
    }

    if (!formData.password.trim()) {
      return;
    }

    setLoading(true);
    setAuthSuccess(false);

    try {
      // authService.login() now calls the backend JWT endpoint and auto-saves token + user
      const response = await authService.login({
        email: formData.email,
        password: formData.password
      });

      if (response.success && response.data) {
        const { user } = response.data;
        const isAdminUser = user.role === "ADMIN";

        // Check if customer record exists (for routing purposes)
        try {
          const customerCheck = await authService.checkCustomerExists(formData.email);
          const hasCustomerRec = customerCheck.success && customerCheck.data.hasCustomerRecord;
          localStorage.setItem("hasCustomerRecord", String(hasCustomerRec));
        } catch {
          localStorage.setItem("hasCustomerRecord", "false");
        }

        if (isAdminUser) {
          toast.success("Admin login successful!", {
            description: "Redirecting to admin dashboard...",
            icon: <Shield className="h-4 w-4" />,
          });
        } else {
          toast.success("Login successful!");
        }

        setAuthSuccess(true);
        const target = isAdminUser ? "/admin" : "/dashboard";
        setTimeout(() => {
          setTransition({ show: true, userName: user.fullName || user.email, target });
        }, 320);
      } else {
        setAuthSuccess(false);
        if ((response.message || "").toLowerCase().includes("invalid") || (response.message || "").toLowerCase().includes("password")) {
          setLoginTouched((prev) => ({ ...prev, password: true }));
          setLoginErrors((prev) => ({ ...prev, password: "Password does not match" }));
        }
        toast.error(response.message || "Login failed");
      }
    } catch (error: any) {
      setAuthSuccess(false);
      console.error("Login error:", error);

      const errorMessage = error.response?.data?.message || "";
      const statusCode = error.response?.status;

      if (
        statusCode === 404 ||
        errorMessage.toLowerCase().includes("no account found") ||
        errorMessage.toLowerCase().includes("sign up")
      ) {
        toast.error(
          errorMessage || "No account found with this email. Please sign up to create a new account.",
          {
            duration: 6000,
            action: {
              label: "Sign Up",
              onClick: () => navigate("/signup"),
            },
          }
        );
      } else if (
        statusCode === 403 ||
        errorMessage.toLowerCase().includes("deactivated") ||
        errorMessage.toLowerCase().includes("contact support")
      ) {
        toast.error(errorMessage, { duration: 7000 });
      } else if (errorMessage) {
        if (statusCode === 401 || errorMessage.toLowerCase().includes("invalid") || errorMessage.toLowerCase().includes("password")) {
          setLoginTouched((prev) => ({ ...prev, password: true }));
          setLoginErrors((prev) => ({ ...prev, password: "Password does not match" }));
        }
        toast.error(errorMessage);
      } else if (statusCode === 401) {
        setLoginTouched((prev) => ({ ...prev, password: true }));
        setLoginErrors((prev) => ({ ...prev, password: "Password does not match" }));
        toast.error("Invalid email or password");
      } else if (error.code === "ERR_NETWORK") {
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
    const forgotEmailError = validateEmailField(forgotEmail);
    if (forgotEmailError) {
      toast.error(forgotEmailError);
      return;
    }
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
    if (!otp || !newPassword) {
      toast.error("Please fill in all fields");
      return;
    }
    const resetPasswordError = validatePasswordField(newPassword);
    if (resetPasswordError) {
      toast.error(resetPasswordError);
      return;
    }
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

  const inputClassName =
    "h-11 rounded-xl border-[var(--glass-border)] bg-transparent text-white placeholder:text-muted-foreground focus-visible:ring-[var(--neon-cyan)] focus-visible:ring-2 focus-visible:ring-offset-0";

  const primaryButtonClassName =
    "h-11 w-full rounded-xl bg-gradient-to-r from-[var(--neon-blue)] via-[hsl(var(--primary))] to-[var(--neon-purple)] font-semibold text-white shadow-[0_0_24px_oklch(0.65_0.2_260_/_40%)] transition-all duration-300 hover:scale-[1.02] hover:shadow-[0_0_40px_oklch(0.65_0.2_260_/_65%)] active:scale-[0.99]";

  const successButtonClassName =
    "h-11 w-full rounded-xl bg-gradient-to-r from-emerald-500 to-[var(--neon-cyan)] font-semibold text-white shadow-[0_0_32px_rgba(16,185,129,0.45)] transition-all animate-pulse-glow";

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[oklch(0.09_0.02_270)] text-white">
      {transition.show && (
        <LoginTransitionOverlay
          userName={transition.userName}
          onComplete={() => navigate(transition.target)}
        />
      )}

      <div className="grid min-h-screen w-full overflow-hidden lg:grid-cols-2">
        <section className="relative hidden flex-col justify-between overflow-hidden border-r border-[var(--glass-border)] bg-gradient-to-br from-[oklch(0.12_0.05_270)] via-[oklch(0.09_0.04_290)] to-[oklch(0.08_0.05_250)] p-12 lg:flex">
          <div className="pointer-events-none absolute -left-24 -top-24 h-80 w-80 rounded-full bg-[var(--neon-blue)]/25 blur-3xl animate-float-slow" />
          <div className="pointer-events-none absolute -bottom-20 -right-16 h-96 w-96 rounded-full bg-[var(--neon-purple)]/25 blur-3xl animate-float-reverse" />
          <div className="pointer-events-none absolute left-1/2 top-1/2 h-64 w-64 -translate-x-1/2 -translate-y-1/2 rounded-full bg-[var(--neon-cyan)]/10 blur-3xl animate-pulse-glow" />

          <div className="relative z-10 flex items-center gap-3">
            <span className="grid h-10 w-10 place-items-center rounded-xl bg-gradient-to-br from-[var(--neon-blue)] to-[var(--neon-purple)] text-sm font-bold text-white">
              B
            </span>
            <Link to="/" className="text-2xl font-bold text-white">
              Bank <span className="text-[var(--neon-blue)]">Simulation</span>
            </Link>
          </div>

          <div className="relative z-10 max-w-md">
            <div className="relative h-56 w-full max-w-sm">
              <div className="absolute inset-0 rounded-2xl bg-[linear-gradient(115deg,#2f8ef7_0%,#4b7cf6_35%,#6a73ee_68%,#37b8d8_100%)] p-6 shadow-[0_30px_80px_-20px_oklch(0.65_0.2_260_/_60%)] animate-float-slow">
                <div className="mb-7 flex items-center justify-between text-xs font-semibold tracking-wide text-white/80">
                  <span>BANK SIMULATION</span>
                  <span>VISA</span>
                </div>
                <div className="mb-7 font-mono text-3xl tracking-[0.28em] text-white/80">•••• •••• •••• 4829</div>
                <div className="flex items-end justify-between text-xs text-white/70">
                  <div>
                    <div className="uppercase tracking-wide">Cardholder</div>
                    <div className="mt-1 text-sm font-semibold text-white/85">ALEX RIVERA</div>
                  </div>
                  <div>
                    <div className="uppercase tracking-wide">Expires</div>
                    <div className="mt-1 text-sm font-semibold text-white/85">09/29</div>
                  </div>
                </div>
              </div>
              <div className="absolute -bottom-6 -right-6 h-40 w-64 rounded-2xl bg-[linear-gradient(115deg,#5f74f0_0%,#38b7d8_100%)] opacity-65 blur-sm animate-float-reverse" />
            </div>

            <h2 className="mt-12 text-4xl font-bold leading-tight text-white">
              Banking, <span className="bg-gradient-to-r from-[var(--neon-blue)] to-[var(--neon-purple)] bg-clip-text text-transparent">reimagined</span> for the next generation.
            </h2>
            <p className="mt-4 text-base text-muted-foreground">
              Secure, instant, and intelligent, your finances, fully under control.
            </p>

            <div className="mt-8 space-y-3 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <Shield className="h-4 w-4 text-[var(--neon-cyan)]" />
                <span>Bank-grade encryption</span>
              </div>
              <div className="flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-[var(--neon-purple)]" />
                <span>Trusted by 2M+</span>
              </div>
            </div>
          </div>

          <p className="relative z-10 text-sm text-muted-foreground">© 2026 Bank Simulation</p>
        </section>

        <section className="flex items-center justify-center p-6 sm:p-12">
          <div className="w-full max-w-md" style={{ animation: "fadeSlideUp 0.7s cubic-bezier(0.22,1,0.36,1) both" }}>
            <div className="mb-6 text-center lg:hidden">
              <Link to="/" className="inline-flex items-center gap-2 text-2xl font-bold text-white">
                <span className="grid h-10 w-10 place-items-center rounded-xl bg-gradient-to-br from-[var(--neon-blue)] to-[var(--neon-purple)] text-sm font-bold text-white">
                  B
                </span>
                Bank <span className="text-[var(--neon-blue)]">Simulation</span>
              </Link>
            </div>

            {!showForgotPassword ? (
              <Card className="rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] text-white backdrop-blur-xl shadow-[0_30px_80px_-30px_oklch(0.65_0.2_260_/_40%)]">
                <CardHeader className="p-8 pb-2">
                  <CardTitle className="bg-gradient-to-r from-white via-[var(--neon-cyan)] to-[var(--neon-purple)] bg-clip-text text-3xl font-bold text-transparent">
                      Welcome Back
                  </CardTitle>
                  <CardDescription className="mt-2 text-sm text-muted-foreground">
                      Login to manage accounts, loans, transactions, and admin tools.
                    </CardDescription>
                </CardHeader>

                <CardContent className="p-8 pt-4">
                  <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                    <div className="space-y-2">
                      <Label htmlFor="email" className="text-sm font-medium text-white">
                        Email
                      </Label>
                      <div className="relative">
                        <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                        <Input
                          id="email"
                          type="email"
                          placeholder="you@example.com"
                          value={formData.email}
                          onChange={(e) => handleLoginFieldChange("email", e.target.value)}
                          onBlur={() => handleLoginFieldBlur("email")}
                          className={`${inputClassName} pl-10 ${
                            loginTouched.email && loginErrors.email
                              ? "border-destructive focus-visible:ring-destructive"
                              : ""
                          }`}
                          required
                        />
                      </div>
                      {loginTouched.email && loginErrors.email && (
                        <p className="text-xs text-destructive">{loginErrors.email}</p>
                      )}
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <Label htmlFor="password" className="text-sm font-medium text-white">
                          Password
                        </Label>
                        <button
                          type="button"
                          className="text-xs text-[var(--neon-cyan)] transition-colors hover:text-[var(--neon-purple)]"
                          onClick={() => setShowForgotPassword(true)}
                        >
                          Forgot password?
                        </button>
                      </div>

                      <div className="relative">
                        <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                        <Input
                          id="password"
                          type={showPassword ? "text" : "password"}
                          placeholder="••••••••"
                          value={formData.password}
                          onChange={(e) => handleLoginFieldChange("password", e.target.value)}
                          onBlur={() => handleLoginFieldBlur("password")}
                          className={`${inputClassName} pl-10 pr-10 ${
                            loginTouched.password && loginErrors.password
                              ? "border-destructive focus-visible:ring-destructive"
                              : ""
                          }`}
                          required
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword((prev) => !prev)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-[var(--neon-cyan)]"
                          aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                          {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                        </button>
                      </div>
                      {loginTouched.password && loginErrors.password && (
                        <p className="text-xs text-destructive">{loginErrors.password}</p>
                      )}
                    </div>

                    <Button
                      type="submit"
                      className={authSuccess ? successButtonClassName : primaryButtonClassName}
                      disabled={loading || !isLoginFormValid}
                    >
                      {loading ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Logging in...
                        </>
                      ) : authSuccess ? (
                        <>
                          <CheckCircle2 className="mr-2 h-4 w-4" />
                          Authenticated
                        </>
                      ) : (
                        <>
                          <LogIn className="mr-2 h-4 w-4" />
                          Login
                        </>
                      )}
                    </Button>

                    <div className="relative my-6">
                      <div className="h-px w-full bg-gradient-to-r from-transparent via-[var(--glass-border)] to-transparent" />
                      <span className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 rounded-full border border-[var(--glass-border)] bg-[var(--glass)] px-3 text-xs text-muted-foreground">
                        or
                      </span>
                    </div>

                    <Button
                      type="button"
                      variant="outline"
                      className="h-11 w-full border-[var(--glass-border)] bg-transparent text-white transition-all duration-300 hover:scale-[1.01] hover:border-[var(--neon-cyan)] hover:bg-white/5"
                      disabled={loading}
                      onClick={handleGoogleContinue}
                    >
                      <GoogleIcon />
                      <span className="ml-2">Continue with Google</span>
                    </Button>
                  </form>

                  <p className="mt-6 text-center text-sm text-muted-foreground">
                    New user?{" "}
                    <Link
                      to="/signup"
                      className="font-medium text-[var(--neon-cyan)] transition-colors hover:text-[var(--neon-purple)]"
                    >
                      Sign Up
                    </Link>
                  </p>
                </CardContent>
              </Card>
            ) : (
              <Card className="rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] text-white backdrop-blur-xl shadow-[0_30px_80px_-30px_oklch(0.65_0.2_260_/_40%)]">
                <CardHeader className="p-8 pb-2">
                  <div className="mb-2 flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 rounded-full border border-[var(--glass-border)] bg-transparent text-muted-foreground hover:bg-white/5 hover:text-white"
                      onClick={() => {
                        setShowForgotPassword(false);
                        setForgotPasswordStep(1);
                      }}
                      type="button"
                    >
                      <ArrowLeft className="h-4 w-4" />
                    </Button>
                    <CardTitle className="text-2xl font-bold text-white">Reset Password</CardTitle>
                  </div>
                  <CardDescription className="mt-2 text-sm text-muted-foreground">
                    {forgotPasswordStep === 1
                      ? "Enter your email and we will send a one-time password."
                      : "Enter the OTP and your new password."}
                  </CardDescription>
                </CardHeader>

                <CardContent className="p-8 pt-4">
                  {forgotPasswordStep === 1 ? (
                    <form onSubmit={handleForgotPasswordSubmitEmail} className="mt-8 space-y-5">
                      <div className="space-y-2">
                        <Label htmlFor="forgotEmail" className="text-sm font-medium text-white">
                          Email Address
                        </Label>
                        <div className="relative">
                          <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                          <Input
                            id="forgotEmail"
                            type="email"
                            placeholder="you@example.com"
                            value={forgotEmail}
                            onChange={(e) => setForgotEmail(e.target.value)}
                            className={`${inputClassName} pl-10`}
                            required
                          />
                        </div>
                      </div>

                      <Button type="submit" className={primaryButtonClassName} disabled={loading}>
                        {loading ? (
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        ) : (
                          <KeyRound className="mr-2 h-4 w-4" />
                        )}
                        {loading ? "Sending..." : "Send OTP"}
                      </Button>
                    </form>
                  ) : (
                    <form onSubmit={handleForgotPasswordSubmitReset} className="mt-8 space-y-5">
                      <div className="space-y-2">
                        <Label htmlFor="otp" className="text-sm font-medium text-white">
                          6-Digit OTP
                        </Label>
                        <div className="relative">
                          <KeyRound className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                          <Input
                            id="otp"
                            type="text"
                            maxLength={6}
                            placeholder="123456"
                            value={otp}
                            onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
                            className={`${inputClassName} pl-10`}
                            required
                          />
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="newPassword" className="text-sm font-medium text-white">
                          New Password
                        </Label>
                        <div className="relative">
                          <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                          <Input
                            id="newPassword"
                            type="password"
                            placeholder="••••••••"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            className={`${inputClassName} pl-10`}
                            required
                          />
                        </div>
                      </div>

                      <Button type="submit" className={primaryButtonClassName} disabled={loading}>
                        {loading ? (
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        ) : (
                          <Shield className="mr-2 h-4 w-4" />
                        )}
                        {loading ? "Verifying..." : "Reset Password"}
                      </Button>
                    </form>
                  )}
                </CardContent>
              </Card>
            )}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Login;

