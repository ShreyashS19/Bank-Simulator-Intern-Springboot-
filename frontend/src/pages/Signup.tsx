import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import {
  CheckCircle2,
  XCircle,
  Loader2,
  Shield,
  Sparkles,
  UserPlus,
  Eye,
  EyeOff,
  Mail,
  Lock,
  User,
} from "lucide-react";
import { authService } from "@/services/authService";
import {
  getPasswordChecks,
  getPasswordStrength,
  passwordRuleHints,
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

type SignupErrors = {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
};

const Signup = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: ""
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<SignupErrors>({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [touched, setTouched] = useState({
    fullName: false,
    email: false,
    password: false,
    confirmPassword: false,
  });
  const [signupSuccess, setSignupSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  useEffect(() => {
    document.documentElement.classList.remove("custom-cursor-active");
  }, []);

  const validateFullName = (fullName: string): string => {
    const normalized = fullName.trim();

    if (!normalized) {
      return "Full name is required";
    }

    if (!/^[A-Za-z ]+$/.test(normalized)) {
      return "Full name can only contain letters and spaces";
    }

    if (normalized.length < 3) {
      return "Full name must be at least 3 characters";
    }

    return "";
  };

  const getValidationErrors = (data: typeof formData): SignupErrors => {
    const nextErrors: SignupErrors = {
      fullName: validateFullName(data.fullName),
      email: validateEmailField(data.email),
      password: validatePasswordField(data.password),
      confirmPassword: "",
    };

    if (!data.confirmPassword) {
      nextErrors.confirmPassword = "Please confirm your password";
    } else if (data.password !== data.confirmPassword) {
      nextErrors.confirmPassword = "Passwords do not match";
    }

    return nextErrors;
  };

  const validateForm = (): boolean => {
    const newErrors = getValidationErrors(formData);
    setErrors(newErrors);
    return !Object.values(newErrors).some(Boolean);
  };

  const isSignupFormValid = !Object.values(getValidationErrors(formData)).some(Boolean);

  const signupPasswordChecks = getPasswordChecks(formData.password);
  const signupPasswordStrength = getPasswordStrength(formData.password);

  const handleFieldChange = (field: keyof typeof formData, value: string) => {
    const nextData = { ...formData, [field]: value };
    setFormData(nextData);
    setTouched((prev) => ({ ...prev, [field]: true }));
    setErrors(getValidationErrors(nextData));

    if (field === "password") {
      setSignupSuccess(false);
    }
  };

  const handleFieldBlur = (field: keyof typeof formData) => {
    setTouched((prev) => ({ ...prev, [field]: true }));
    setErrors(getValidationErrors(formData));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched({
      fullName: true,
      email: true,
      password: true,
      confirmPassword: true,
    });

    if (!validateForm()) {
      toast.error("Please fix the errors in the form");
      return;
    }

    setLoading(true);
    setSignupSuccess(false);

    try {
      console.log("Submitting signup:", { fullName: formData.fullName, email: formData.email });

      const response = await authService.signup({
        fullName: formData.fullName.trim(),
        email: formData.email.trim(),
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });

      console.log("Signup successful:", response);

      if (response.success) {
        localStorage.setItem("user", JSON.stringify(response.data));
        localStorage.setItem("isAuthenticated", "true");
        localStorage.setItem("hasCustomerRecord", "false");

        setSignupSuccess(true);
        toast.success("Account created successfully! Redirecting to dashboard...");

        setTimeout(() => {
          navigate("/dashboard");
        }, 1100);
      } else {
        setSignupSuccess(false);
        toast.error(response.message || "Signup failed");
      }
    } catch (error: any) {
      setSignupSuccess(false);
      console.error("Signup error:", error);

      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else if (error.code === "ERR_NETWORK") {
        toast.error("Cannot connect to server. Ensure backend is running on port 8080.");
      } else {
        toast.error("Failed to create account. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleContinue = async () => {
    try {
      const response = await authService.getOAuthProviders();
      if (!response.success || !response.data?.googleConfigured) {
        toast.error(
          response.data?.configurationMessage || "Google login is not configured on backend."
        );
        return;
      }

      window.location.href = authService.getGoogleOAuthUrl();
    } catch {
      toast.error("Unable to verify Google OAuth configuration. Please try again.");
    }
  };

  const inputBaseClassName =
    "h-11 rounded-xl border-[var(--glass-border)] bg-transparent text-white placeholder:text-muted-foreground focus-visible:ring-[var(--neon-cyan)] focus-visible:ring-2 focus-visible:ring-offset-0";

  const getInputClassName = (fieldError?: string) =>
    `${inputBaseClassName} ${fieldError ? "border-destructive focus-visible:ring-destructive" : ""}`;

  const primaryButtonClassName =
    "h-11 w-full rounded-xl bg-gradient-to-r from-[var(--neon-blue)] via-[hsl(var(--primary))] to-[var(--neon-purple)] font-semibold text-white shadow-[0_0_24px_oklch(0.65_0.2_260_/_40%)] transition-all duration-300 hover:scale-[1.02] hover:shadow-[0_0_40px_oklch(0.65_0.2_260_/_65%)] active:scale-[0.99]";

  const successButtonClassName =
    "h-11 w-full rounded-xl bg-gradient-to-r from-emerald-500 to-[var(--neon-cyan)] font-semibold text-white shadow-[0_0_32px_rgba(16,185,129,0.45)] transition-all animate-pulse-glow";

  return (

    <div className="relative min-h-screen w-full overflow-hidden bg-[oklch(0.09_0.02_270)] text-white">
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
                    <div className="mt-1 text-sm font-semibold text-white/85">YOUR NAME</div>
                  </div>
                  <div>
                    <div className="uppercase tracking-wide">Expires</div>
                    <div className="mt-1 text-sm font-semibold text-white/85">12/30</div>
                  </div>
                </div>
              </div>
              <div className="absolute -bottom-6 -right-6 h-40 w-64 rounded-2xl bg-[linear-gradient(115deg,#5f74f0_0%,#38b7d8_100%)] opacity-65 blur-sm animate-float-reverse" />
            </div>

            <h2 className="mt-12 text-4xl font-bold leading-tight text-white">
              Join the <span className="bg-gradient-to-r from-[var(--neon-blue)] to-[var(--neon-cyan)] bg-clip-text text-transparent">future</span> of banking.
            </h2>
            <p className="mt-4 text-base text-muted-foreground">
              Open an account in minutes. No paperwork, no hidden fees, no waiting.
            </p>

            <div className="mt-8 space-y-3 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <Shield className="h-4 w-4 text-[var(--neon-cyan)]" />
                <span>FDIC insured</span>
              </div>
              <div className="flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-[var(--neon-purple)]" />
                <span>Free forever plan</span>
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

            <Card className="rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] text-white backdrop-blur-xl shadow-[0_30px_80px_-30px_oklch(0.65_0.2_260_/_40%)]">
              <CardHeader className="p-8 pb-2">
                <CardTitle className="bg-gradient-to-r from-white via-[var(--neon-cyan)] to-[var(--neon-purple)] bg-clip-text text-3xl font-bold text-transparent">
                  Create Your Account
                </CardTitle>
                <CardDescription className="mt-2 text-sm text-muted-foreground">
                  Join Bank Simulation, next-gen digital banking.
                </CardDescription>
              </CardHeader>

              <CardContent className="p-8 pt-4">
                <form onSubmit={handleSubmit} className="mt-8 space-y-5">
                  <div className="space-y-2">
                    <Label htmlFor="fullName" className="text-sm font-medium text-white">
                      Full Name <span className="text-destructive">*</span>
                    </Label>
                    <div className="relative">
                      <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                        id="fullName"
                        type="text"
                        placeholder="Alex Rivera"
                        value={formData.fullName}
                        onChange={(e) => handleFieldChange("fullName", e.target.value)}
                        onBlur={() => handleFieldBlur("fullName")}
                        className={`${getInputClassName(touched.fullName ? errors.fullName : "")} pl-10`}
                        required
                      />
                    </div>
                    {touched.fullName && errors.fullName && (
                      <p className="text-xs text-destructive">{errors.fullName}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-sm font-medium text-white">
                      Email <span className="text-destructive">*</span>
                    </Label>
                    <div className="relative">
                      <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                        id="email"
                        type="email"
                        placeholder="you@example.com"
                        value={formData.email}
                        onChange={(e) => handleFieldChange("email", e.target.value)}
                        onBlur={() => handleFieldBlur("email")}
                        className={`${getInputClassName(touched.email ? errors.email : "")} pl-10`}
                        required
                      />
                    </div>
                    {touched.email && errors.email && (
                      <p className="text-xs text-destructive">{errors.email}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-sm font-medium text-white">
                      Password <span className="text-destructive">*</span>
                    </Label>
                    <div className="relative">
                      <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                        id="password"
                        type={showPassword ? "text" : "password"}
                        placeholder="••••••••"
                        value={formData.password}
                        onChange={(e) => handleFieldChange("password", e.target.value)}
                        onBlur={() => handleFieldBlur("password")}
                        className={`${getInputClassName(touched.password ? errors.password : "")} pl-10 pr-10`}
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
                    {touched.password && errors.password && (
                      <p className="text-xs text-destructive">{errors.password}</p>
                    )}

                    {formData.password.length > 0 && (
                      <div className="rounded-xl border border-[var(--glass-border)] bg-[oklch(0.08_0.02_270_/_45%)] p-4">
                        <div className="mb-2 flex items-center justify-between">
                          <span className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
                            Password Strength
                          </span>
                          <span
                            className={`text-sm font-semibold ${
                              signupPasswordStrength.label === "Strong"
                                ? "text-emerald-300"
                                : signupPasswordStrength.label === "Medium"
                                  ? "text-amber-300"
                                  : "text-rose-300"
                            }`}
                          >
                            {signupPasswordStrength.label}
                          </span>
                        </div>

                        <div className="h-2 w-full overflow-hidden rounded-full bg-white/10">
                          <div
                            className={`h-full rounded-full transition-all duration-300 ${
                              signupPasswordStrength.score === 3
                                ? "w-full bg-emerald-400"
                                : signupPasswordStrength.score === 2
                                  ? "w-2/3 bg-amber-400"
                                  : "w-1/3 bg-rose-400"
                            }`}
                          />
                        </div>

                        <ul className="mt-3 space-y-2">
                          {passwordRuleHints.map((rule) => {
                            const met = signupPasswordChecks[rule.key];
                            return (
                              <li key={rule.key} className="flex items-center gap-2 text-sm">
                                {met ? (
                                  <CheckCircle2 className="h-4 w-4 text-emerald-300" />
                                ) : (
                                  <XCircle className="h-4 w-4 text-rose-300" />
                                )}
                                <span className={met ? "text-emerald-200" : "text-muted-foreground"}>
                                  {rule.label}
                                </span>
                              </li>
                            );
                          })}
                        </ul>
                      </div>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="confirmPassword" className="text-sm font-medium text-white">
                      Confirm Password <span className="text-destructive">*</span>
                    </Label>
                    <div className="relative">
                      <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                        id="confirmPassword"
                        type={showConfirmPassword ? "text" : "password"}
                        placeholder="••••••••"
                        value={formData.confirmPassword}
                        onChange={(e) => handleFieldChange("confirmPassword", e.target.value)}
                        onBlur={() => handleFieldBlur("confirmPassword")}
                        className={`${getInputClassName(touched.confirmPassword ? errors.confirmPassword : "")} pl-10 pr-10`}
                        required
                      />
                      <button
                        type="button"
                        onClick={() => setShowConfirmPassword((prev) => !prev)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-[var(--neon-cyan)]"
                        aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
                      >
                        {showConfirmPassword ? (
                          <EyeOff className="h-4 w-4" />
                        ) : (
                          <Eye className="h-4 w-4" />
                        )}
                      </button>
                    </div>
                    {touched.confirmPassword && errors.confirmPassword && (
                      <p className="text-xs text-destructive">{errors.confirmPassword}</p>
                    )}
                  </div>

                  <Button
                    type="submit"
                    className={signupSuccess ? successButtonClassName : primaryButtonClassName}
                    disabled={loading || !isSignupFormValid}
                  >
                    {loading ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Creating Account...
                      </>
                    ) : signupSuccess ? (
                      <>
                        <CheckCircle2 className="mr-2 h-4 w-4" />
                        Account Created
                      </>
                    ) : (
                      <>
                        <UserPlus className="mr-2 h-4 w-4" />
                        Create Account
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
                  Already have an account?{" "}
                  <Link
                    to="/login"
                    className="font-medium text-[var(--neon-cyan)] transition-colors hover:text-[var(--neon-purple)]"
                  >
                    Login
                  </Link>
                </p>
              </CardContent>
            </Card>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Signup;
