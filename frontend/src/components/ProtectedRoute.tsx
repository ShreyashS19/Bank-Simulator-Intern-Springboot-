import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireNoCustomer?: boolean;
}

export const ProtectedRoute = ({ children, requireNoCustomer = false }: ProtectedRouteProps) => {
  const navigate = useNavigate();
  
  useEffect(() => {
    const hasCustomerRecord = localStorage.getItem("hasCustomerRecord") === "true";
    
    if (requireNoCustomer && hasCustomerRecord) {
      toast.error("You already have a customer profile");
      navigate("/dashboard");
    }
  }, [navigate, requireNoCustomer]);
  
  return <>{children}</>;
};
