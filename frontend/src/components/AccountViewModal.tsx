import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Account } from "@/services/accountService";

interface AccountViewModalProps {
  account: Account | null;
  open: boolean;
  onClose: () => void;
  onEdit: (account: Account) => void;
}

export const AccountViewModal = ({ account, open, onClose, onEdit }: AccountViewModalProps) => {
  if (!account) return null;

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Account Details</DialogTitle>
          <DialogDescription>View complete account information</DialogDescription>
        </DialogHeader>
        
        <div className="grid grid-cols-2 gap-4">
          {account.accountId && (
            <div className="space-y-2">
              <Label className="text-muted-foreground">Account ID</Label>
              <p className="font-medium">{account.accountId}</p>
            </div>
          )}
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Account Number</Label>
            <p className="font-medium font-mono">{account.accountNumber}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Name on Account</Label>
            <p className="font-medium">{account.nameOnAccount}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Bank Name</Label>
            <p className="font-medium">{account.bankName}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">IFSC Code</Label>
            <p className="font-medium font-mono">{account.ifscCode}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Balance</Label>
            <p className="font-medium text-lg">{formatCurrency(account.amount)}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Aadhar Number</Label>
            <p className="font-medium font-mono">{account.aadharNumber}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Phone Number</Label>
            <p className="font-medium">{account.phoneNumberLinked}</p>
          </div>
          
          <div className="space-y-2">
            <Label className="text-muted-foreground">Status</Label>
            <span className={`inline-block px-2 py-1 rounded-full text-xs ${
              account.status.toLowerCase() === 'active' 
                ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' 
                : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
            }`}>
              {account.status}
            </span>
          </div>
          
          {account.created && (
            <div className="space-y-2">
              <Label className="text-muted-foreground">Created</Label>
              <p className="font-medium">{new Date(account.created).toLocaleDateString()}</p>
            </div>
          )}
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Close</Button>
          <Button onClick={() => { onClose(); onEdit(account); }}>Edit Account</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
