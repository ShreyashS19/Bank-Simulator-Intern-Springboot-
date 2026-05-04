import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { authService, tokenUtils, User } from "@/services/authService";
import { ACCOUNT_DEACTIVATED_URL_ERROR, OAUTH_FAILED_URL_ERROR } from "@/lib/authMessages";
import LoginTransitionOverlay from "@/components/LoginTransitionOverlay";

const OAuthSuccess = () => {
  const navigate = useNavigate();
  const [transition, setTransition] = useState<{ show: boolean; userName: string; target: string }>({
    show: false,
    userName: "",
    target: "/dashboard",
  });

  useEffect(() => {
    const processOAuthCallback = async () => {
      const hashParams = new URLSearchParams(window.location.hash.replace("#", ""));
      const error = hashParams.get("error");

      if (error) {
        const loginError = error === ACCOUNT_DEACTIVATED_URL_ERROR
          ? ACCOUNT_DEACTIVATED_URL_ERROR
          : OAUTH_FAILED_URL_ERROR;
        navigate(`/login?error=${encodeURIComponent(loginError)}`, { replace: true });
        return;
      }

      const token = hashParams.get("token");
      const encodedUser = hashParams.get("user");

      if (!token || !encodedUser) {
        toast.error("Invalid OAuth response. Please login again.");
        navigate("/login", { replace: true });
        return;
      }

      try {
        const user = JSON.parse(encodedUser) as User;
        tokenUtils.saveSession(token, user);

        try {
          const customerCheck = await authService.checkCustomerExists(user.email);
          const hasCustomerRec = customerCheck.success && customerCheck.data.hasCustomerRecord;
          localStorage.setItem("hasCustomerRecord", String(hasCustomerRec));
        } catch {
          localStorage.setItem("hasCustomerRecord", "false");
        }

        window.history.replaceState({}, document.title, window.location.pathname);
        toast.success("Google login successful");
        const target = user.role === "ADMIN" ? "/admin" : "/dashboard";
        setTransition({
          show: true,
          userName: user.fullName || user.email,
          target,
        });
        return;
      } catch {
        toast.error("Unable to process Google login response.");
        navigate("/login", { replace: true });
      }
    };

    processOAuthCallback();
  }, [navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
      {transition.show && (
        <LoginTransitionOverlay
          userName={transition.userName}
          onComplete={() => navigate(transition.target, { replace: true })}
        />
      )}
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Completing Google Sign-In</CardTitle>
          <CardDescription>We are setting up your secure session.</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center gap-3 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" />
          Please wait...
        </CardContent>
      </Card>
    </div>
  );
};

export default OAuthSuccess;
