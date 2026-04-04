import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Account } from "@/services/accountService"; 
import { Loader2 } from "lucide-react";

interface AccountEditModalProps {
  account: Account | null;
  open: boolean;
  isLoading: boolean;
  onClose: () => void;
  onSave: (account: Account) => void;
  onChange: (account: Account) => void;
}

export const AccountEditModal = ({ 
  account, 
  open, 
  isLoading, 
  onClose, 
  onSave, 
  onChange 
}: AccountEditModalProps) => {
  if (!account) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(account);
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit Account</DialogTitle>
          <DialogDescription>Update account information</DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Account Number (Read-only) */}
            <div className="space-y-2">
              <Label htmlFor="edit-accountNumber">Account Number</Label>
              <Input
                id="edit-accountNumber"
                value={account.accountNumber}
                disabled
                className="bg-gray-100 font-mono"
              />
            </div>

            {/* Name on Account */}
            <div className="space-y-2">
              <Label htmlFor="edit-nameOnAccount">Name on Account</Label>
              <Input
                id="edit-nameOnAccount"
                value={account.nameOnAccount}
                onChange={(e) => onChange({ ...account, nameOnAccount: e.target.value })}
              />
            </div>

            {/* Bank Name */}
            <div className="space-y-2">
              <Label htmlFor="edit-bankName">Bank Name</Label>
              <Input
                id="edit-bankName"
                value={account.bankName}
                onChange={(e) => onChange({ ...account, bankName: e.target.value })}
              />
            </div>

            {/* IFSC Code */}
            <div className="space-y-2">
              <Label htmlFor="edit-ifscCode">IFSC Code</Label>
              <Input
                id="edit-ifscCode"
                value={account.ifscCode}
                onChange={(e) => onChange({ ...account, ifscCode: e.target.value.toUpperCase() })}
                maxLength={11}
                className="font-mono"
              />
            </div>

            {/* Aadhar Number (Read-only) */}
            <div className="space-y-2">
              <Label htmlFor="edit-aadhar">Aadhar Number</Label>
              <Input
                id="edit-aadhar"
                value={account.aadharNumber}
                disabled
                className="bg-gray-100 font-mono"
              />
            </div>

            {/* Balance */}
            <div className="space-y-2">
              <Label htmlFor="edit-amount">Balance</Label>
              <Input
                id="edit-amount"
                type="number"
                step="0.01"
                value={account.amount}
                onChange={(e) => onChange({ ...account, amount: parseFloat(e.target.value) || 0 })}
              />
            </div>

            {/* Status */}
            <div className="space-y-2">
              <Label htmlFor="edit-status">Status</Label>
              <Select
                value={account.status}
                onValueChange={(value) => onChange({ ...account, status: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ACTIVE">Active</SelectItem>
                  {/* <SelectItem value="INACTIVE">Inactive</SelectItem> */}
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
