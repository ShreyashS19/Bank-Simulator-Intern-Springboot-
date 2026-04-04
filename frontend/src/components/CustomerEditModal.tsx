import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Customer } from "@/services/customerService";
import { Loader2 } from "lucide-react";

interface CustomerEditModalProps {
  customer: Customer | null;
  open: boolean;
  isLoading: boolean;
  onClose: () => void;
  onSave: (customer: Customer) => void;
  onChange: (customer: Customer) => void;
}

export const CustomerEditModal = ({
  customer,
  open,
  isLoading,
  onClose,
  onSave,
  onChange
}: CustomerEditModalProps) => {
  if (!customer) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(customer); 
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit Customer</DialogTitle>
          <DialogDescription>Update customer information</DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Aadhar Number (Read-only) */}
            <div className="space-y-2">
              <Label htmlFor="edit-aadhar">Aadhar Number</Label>
              <Input
                id="edit-aadhar"
                value={customer.aadharNumber}
                disabled
                className="bg-gray-100 font-mono"
              />
            </div>

            {/* Name */}
            <div className="space-y-2">
              <Label htmlFor="edit-name">Full Name</Label>
              <Input
                id="edit-name"
                value={customer.name}
                onChange={(e) => onChange({ ...customer, name: e.target.value })}
              />
            </div>


            {/* Email */}
            <div className="space-y-2">
              <Label htmlFor="edit-email">Email</Label>
              <Input
                id="edit-email"
                type="email"
                value={customer.email}
                onChange={(e) => onChange({ ...customer, email: e.target.value })}
              />
            </div>

            {/* Address */}
            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="edit-address">Address</Label>
              <Input
                id="edit-address"
                value={customer.address}
                onChange={(e) => onChange({ ...customer, address: e.target.value })}
              />
            </div>

            {/* Date of Birth */}
            <div className="space-y-2">
              <Label htmlFor="edit-dob">Date of Birth</Label>
              <Input
                id="edit-dob"
                type="date"
                value={customer.dob}
                onChange={(e) => onChange({ ...customer, dob: e.target.value })}
              />
            </div>

            {/* Customer PIN */}
            <div className="space-y-2">
              <Label htmlFor="edit-pin">Customer PIN</Label>
              <Input
                id="edit-pin"
                type="password"
                value={customer.customerPin || ''}
                onChange={(e) => {
                  const value = e.target.value.replace(/\D/g, '');
                  if (value.length <= 6) {
                    onChange({ ...customer, customerPin: value });
                  }
                }}
                placeholder="6-digit PIN"
                maxLength={6}
              />
            </div>

            {/* Status */}
            <div className="space-y-2">
              <Label htmlFor="edit-status">Status</Label>
              <Select
                value={customer.status}
                onValueChange={(value) => onChange({ ...customer, status: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Active">Active</SelectItem>
                  {/* <SelectItem value="Inactive">Inactive</SelectItem> */}
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  Updating...
                </>
              ) : (
                'Save Changes'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
