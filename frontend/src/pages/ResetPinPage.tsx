import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";
import { KeyRound, Loader2 } from "lucide-react";
import { accountService } from "@/services/accountService";

interface ResetPinState {
  step: 1 | 2;
  otp: string;
  newPin: string;
  isLoading: boolean;
  error: string | null;
  userEmail: string | null;
}

const ResetPinPage = () => {
  const navigate = useNavigate();
  const [state, setState] = useState<ResetPinState>({
    step: 1,
    otp: "",
    newPin: "",
    isLoading: false,
    error: null,
    userEmail: null
  });

  useEffect(() => {
    // Validate session on component mount
    const userEmail = localStorage.getItem('userEmail');
    const token = localStorage.getItem('token');
    
    if (!userEmail || !token) {
      toast.error("User email not found in session");
      navigate('/login');
      return;
    }
    
    setState(prev => ({ ...prev, userEmail }));
  }, [navigate]);

  const handleSendOtp = async () => {
    if (!state.userEmail) {
      setState(prev => ({ ...prev, error: "User email not found in session" }));
      return;
    }

    setState(prev => ({ ...prev, isLoading: true, error: null }));
    
    try {
      await accountService.forgotPin(state.userEmail);
      toast.success("OTP sent to your email!");
      setState(prev => ({ ...prev, step: 2, isLoading: false }));
    } catch (error: any) {
      const errorMessage = error.message || "Failed to send OTP";
      setState(prev => ({ ...prev, error: errorMessage, isLoading: false }));
      toast.error(errorMessage);
    }
  };

  const handleResetPin = async () => {
    if (!state.userEmail || !state.otp || !state.newPin) {
      setState(prev => ({ ...prev, error: "Please fill in all fields" }));
      return;
    }

    // Validate OTP format
    if (!/^\d{6}$/.test(state.otp)) {
      setState(prev => ({ ...prev, error: "Invalid OTP format" }));
      toast.error("Invalid OTP format");
      return;
    }

    // Validate PIN format
    if (!/^\d{6}$/.test(state.newPin)) {
      setState(prev => ({ ...prev, error: "PIN must be exactly 6 digits" }));
      toast.error("PIN must be exactly 6 digits");
      return;
    }

    setState(prev => ({ ...prev, isLoading: true, error: null }));
    
    try {
      await accountService.resetPin(state.userEmail, state.otp, state.newPin);
      toast.success("Account PIN reset successfully!");
      navigate('/accounts');
    } catch (error: any) {
      const errorMessage = error.message || "Failed to reset PIN";
      setState(prev => ({ ...prev, error: errorMessage, isLoading: false }));
      toast.error(errorMessage);
    }
  };

  const handleBack = () => {
    setState(prev => ({ 
      ...prev, 
      step: 1, 
      otp: "", 
      newPin: "", 
      error: null 
    }));
  };

  const handleOtpChange = (value: string) => {
    const sanitized = value.replace(/\D/g, '').slice(0, 6);
    setState(prev => ({ ...prev, otp: sanitized, error: null }));
  };

  const handlePinChange = (value: string) => {
    const sanitized = value.replace(/\D/g, '').slice(0, 6);
    setState(prev => ({ ...prev, newPin: sanitized, error: null }));
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Reset PIN</h1>
          <p className="text-muted-foreground mt-1">Reset your account PIN securely</p>
        </div>

        {state.step === 1 ? (
          <Card className="w-full max-w-md mx-auto">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <KeyRound className="h-5 w-5" />
                Reset PIN - Step 1
              </CardTitle>
              <CardDescription>
                We'll send an OTP to your registered email address
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email Address</Label>
                <Input
                  id="email"
                  type="email"
                  value={state.userEmail || ''}
                  disabled
                  className="bg-muted"
                />
              </div>
              {state.error && (
                <Alert variant="destructive">
                  <AlertDescription>{state.error}</AlertDescription>
                </Alert>
              )}
              <Button 
                onClick={handleSendOtp} 
                disabled={state.isLoading || !state.userEmail}
                className="w-full"
              >
                {state.isLoading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                    Sending...
                  </>
                ) : (
                  <>
                    <KeyRound className="h-4 w-4 mr-2" />
                    Send OTP
                  </>
                )}
              </Button>
            </CardContent>
          </Card>
        ) : (
          <Card className="w-full max-w-md mx-auto">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <KeyRound className="h-5 w-5" />
                Reset PIN - Step 2
              </CardTitle>
              <CardDescription>
                Enter the OTP sent to your email and your new 6-digit PIN
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="otp">OTP (6 digits)</Label>
                <Input
                  id="otp"
                  type="text"
                  maxLength={6}
                  value={state.otp}
                  onChange={(e) => handleOtpChange(e.target.value)}
                  placeholder="123456"
                  aria-describedby="otp-error"
                  aria-required="true"
                  aria-invalid={state.error ? 'true' : 'false'}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="newPin">New PIN (6 digits)</Label>
                <Input
                  id="newPin"
                  type="password"
                  maxLength={6}
                  value={state.newPin}
                  onChange={(e) => handlePinChange(e.target.value)}
                  placeholder="••••••"
                  aria-describedby="pin-error"
                  aria-required="true"
                  aria-invalid={state.error ? 'true' : 'false'}
                />
              </div>
              {state.error && (
                <Alert variant="destructive" role="alert">
                  <AlertDescription id="otp-error">{state.error}</AlertDescription>
                </Alert>
              )}
              <div className="flex gap-2">
                <Button 
                  variant="outline" 
                  onClick={handleBack} 
                  className="flex-1"
                  disabled={state.isLoading}
                >
                  Back
                </Button>
                <Button 
                  onClick={handleResetPin} 
                  disabled={state.isLoading || !state.otp || !state.newPin}
                  className="flex-1"
                >
                  {state.isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Processing...
                    </>
                  ) : (
                    "Reset PIN"
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </DashboardLayout>
  );
};

export default ResetPinPage;
