import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Customer } from "@/services/customerService"; 

interface CustomerViewModalProps {
  customer: Customer | null;
  open: boolean;
  onClose: () => void;
  onEdit: (customer: Customer) => void;
}

export const CustomerViewModal = ({ customer, open, onClose, onEdit }: CustomerViewModalProps) => {
  if (!customer) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Customer Details</DialogTitle>
          <DialogDescription>View complete customer information</DialogDescription>
        </DialogHeader>
        
        <div className="grid grid-cols-2 gap-4">
          {customer.customerId && (
            <div className="space-y-2">
              <Label className="text-muted-foreground">Customer ID</Label>
              <p className="font-medium">{customer.customerId}</p>
            </div>
          )}
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Full Name</Label>
            <p className="font-medium">{customer.name}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Email</Label>
            <p className="font-medium">{customer.email}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Phone Number</Label>
            <p className="font-medium">{customer.phoneNumber}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Aadhar Number</Label>
            <p className="font-medium font-mono">{customer.aadharNumber}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Date of Birth</Label>
            <p className="font-medium">{new Date(customer.dob).toLocaleDateString()}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Status</Label>
            <span className={`inline-block px-2 py-1 rounded-full text-xs ${
              customer.status.toLowerCase() === 'active' 
                ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' 
                : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
            }`}>
              {customer.status}
            </span>
          </div>
          
          <div className="space-y-2 col-span-2">
            <Label className="text-muted-foreground">Address</Label>
            <p className="font-medium">{customer.address}</p>
          </div>
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Close</Button>
          <Button onClick={() => { onClose(); onEdit(customer); }}>
            Edit Customer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
