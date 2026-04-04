import { useState } from "react";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";
import { Search, CreditCard, Loader2, Eye, Edit, Trash2 } from "lucide-react";
import { accountService, Account } from "@/services/accountService";
import { AccountViewModal } from "@/components/AccountViewModal";
import { AccountEditModal } from "@/components/AccountEditModal";

const Accounts = () => {
  const [accountSearch, setAccountSearch] = useState("");
  const [searchedAccount, setSearchedAccount] = useState<Account | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [searchNotFound, setSearchNotFound] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);
  const [deletingAccount, setDeletingAccount] = useState<Account | null>(null);
  const [viewingAccount, setViewingAccount] = useState<Account | null>(null);
  const [formData, setFormData] = useState({
    accountNumber: "",
    aadharNumber: "",
    ifscCode: "",
    phoneNumberLinked: "",
    amount: "0.00",
    bankName: "",
    nameOnAccount: "",
    status: "ACTIVE"
  });

  const handleSearch = async () => {
    if (!accountSearch.trim()) {
      toast.error("Please enter an account number");
      return;
    }

    setIsSearching(true);
    setSearchNotFound(false);
    setSearchedAccount(null);
    
    try {
      const account = await accountService.getAccountByNumber(accountSearch.trim());
      if (account) {
        setSearchedAccount(account);
        toast.success("Account found!");
      }
    } catch (error: any) {
      if (error.response?.status === 404) {
        setSearchNotFound(true);
        toast.error("Account not found");
      } else {
        toast.error("Failed to search account");
        console.error(error);
      }
    } finally {
      setIsSearching(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    try {
      const accountData = {
        accountNumber: formData.accountNumber.trim(),
        aadharNumber: formData.aadharNumber.trim(),
        ifscCode: formData.ifscCode.trim().toUpperCase(),
        phoneNumberLinked: formData.phoneNumberLinked.trim(),
        amount: parseFloat(formData.amount),
        bankName: formData.bankName.trim(),
        nameOnAccount: formData.nameOnAccount.trim(),
        status: formData.status
      };

      const accountId = await accountService.createAccount(accountData);
      
      console.log(' Account created successfully! ID:', accountId);
      toast.success(`Account created successfully!`);
      
      handleReset();
      setSearchedAccount(null);
      setAccountSearch("");
      setSearchNotFound(false);
    } catch (error: any) {
      console.error(' Account creation failed:', error);
      
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else if (error.response?.status === 400) {
        toast.error('Validation failed. Please check all required fields.');
      } else {
        toast.error('Failed to create account. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = async (account: Account) => {
    if (!account.accountNumber) {
      toast.error("Account number is missing. Cannot update account.");
      return;
    }
    
    setIsLoading(true);
    
    try {
      await accountService.updateAccount(account.accountNumber, account);
      
      toast.success("Account updated successfully!");
      setEditingAccount(null);
      
      if (searchedAccount?.accountNumber === account.accountNumber) {
        try {
          const updatedAccount = await accountService.getAccountByNumber(account.accountNumber);
          setSearchedAccount(updatedAccount);
        } catch (error) {
          console.warn('Could not refresh account data');
        }
      }
    } catch (error: any) {
      console.error(' Update failed:', error);
      
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('Failed to update account');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingAccount) return;
    setIsLoading(true);
    try {
      await accountService.deleteAccount(deletingAccount.accountNumber);
      toast.success("Account deleted successfully!");
      setDeletingAccount(null);
      
      if (searchedAccount?.accountNumber === deletingAccount.accountNumber) {
        setSearchedAccount(null);
        setAccountSearch("");
      }
    } catch (error: any) {
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error("Failed to delete account");
      }
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setFormData({
      accountNumber: "",
      aadharNumber: "",
      ifscCode: "",
      phoneNumberLinked: "",
      amount: "0.00",
      bankName: "",
      nameOnAccount: "",
      status: "ACTIVE"
    });
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Account Management</h1>
          <p className="text-muted-foreground mt-1">Create and manage bank accounts</p>
        </div>

        {/* Create Account Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              Create New Account
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="accountNumber">Account Number <span className="text-red-500">*</span></Label>
                  <Input
                    id="accountNumber"
                    value={formData.accountNumber}
                    onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
                    placeholder="10-25 digit account number"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="nameOnAccount">Name on Account <span className="text-red-500">*</span></Label>
                  <Input
                    id="nameOnAccount"
                    value={formData.nameOnAccount}
                    onChange={(e) => setFormData({ ...formData, nameOnAccount: e.target.value })}
                    placeholder="Account holder name"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="aadharNumber">Aadhar Number <span className="text-red-500">*</span></Label>
                  <Input
                    id="aadharNumber"
                    value={formData.aadharNumber}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      if (value.length <= 12) {
                        setFormData({ ...formData, aadharNumber: value });
                      }
                    }}
                    placeholder="12-digit Aadhar number"
                    required
                    maxLength={12}
                  />
                </div>
             
                <div className="space-y-2">
                  <Label htmlFor="bankName">Bank Name <span className="text-red-500">*</span></Label>
                  <Input
                    id="bankName"
                    value={formData.bankName}
                    onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
                    placeholder="e.g., State Bank of India"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="ifscCode">IFSC Code <span className="text-red-500">*</span></Label>
                  <Input
                    id="ifscCode"
                    value={formData.ifscCode}
                    onChange={(e) => setFormData({ ...formData, ifscCode: e.target.value.toUpperCase() })}
                    placeholder="e.g., SBIN0001234"
                    required
                    maxLength={11}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="amount">Initial Balance <span className="text-red-500">*</span></Label>
                  <Input
                    id="amount"
                    type="number"
                    step="0.01"
                    min="0"
                    value={formData.amount}
                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                    placeholder="Minimum 0.00"
                    required
                  />
                </div>
                 
                 
                <div className="space-y-2">
                  <Label htmlFor="status">Status</Label>
                  <Select value={formData.status} onValueChange={(value) => setFormData({ ...formData, status: value })}>
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
              
              
              <div className="flex gap-3">
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Creating...
                    </>
                  ) : (
                    "Create Account"
                  )}
                </Button>
                <Button type="button" variant="outline" onClick={handleReset}>
                  Reset
                </Button>
              </div>
            </form>
          <div className="mt-4">
  <Button
    type="button"
    onClick={() => {
      const email = 'bank.simulator.issue@gmail.com';
      const subject = 'Issue Report - Bank Simulator - Account Dashboard';
      const body = `Dear Admin,%0D%0A%0D%0AI am facing an issue with the Account Management dashboard. Please look into this.%0D%0A%0D%0ADescription of issue:%0D%0A%0D%0A`;
      
      const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${email}&su=${encodeURIComponent(subject)}&body=${body}`;
      window.open(gmailUrl, '_blank');
    }}
    className="bg-red-600 hover:bg-red-700 text-white font-medium"
  >
    Report Issue
  </Button>
</div>

          </CardContent>
        </Card>
 
        {/* Search Account */}
        <Card>
          <CardHeader>
            <CardTitle>Search Account</CardTitle>
            <p className="text-sm text-muted-foreground">Enter Account Number to find account details</p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-3">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Enter Account Number"
                  value={accountSearch}
                  onChange={(e) => setAccountSearch(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  className="pl-9"
                />
              </div>
              <Button onClick={handleSearch} disabled={isSearching}>
                {isSearching ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                    Searching...
                  </>
                ) : (
                  <>
                    <Search className="h-4 w-4 mr-2" />
                    Search
                  </>
                )}
              </Button>
            </div>

            {searchNotFound && (
              <Alert>
                <AlertDescription>
                  No account found with this account number.
                </AlertDescription>
              </Alert>
            )}

            {searchedAccount && (
              <Card className="border-2">
                <CardHeader>
                  <CardTitle className="text-lg">Account Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <Label className="text-muted-foreground">Account Number</Label>
                      <p className="font-medium">{searchedAccount.accountNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Name on Account</Label>
                      <p className="font-medium">{searchedAccount.nameOnAccount}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Bank Name</Label>
                      <p className="font-medium">{searchedAccount.bankName}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">IFSC Code</Label>
                      <p className="font-medium">{searchedAccount.ifscCode}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Balance</Label>
                      <p className="font-medium">₹{searchedAccount.amount.toLocaleString()}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Status</Label>
                      <p className="font-medium capitalize">{searchedAccount.status}</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => setViewingAccount(searchedAccount)}>
                      <Eye className="h-4 w-4 mr-2" />
                      View Details
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => setEditingAccount(searchedAccount)}>
                      <Edit className="h-4 w-4 mr-2" />
                      Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => setDeletingAccount(searchedAccount)}>
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )}
          </CardContent>
        </Card>
      </div>

      <AccountViewModal 
        account={viewingAccount}
        open={!!viewingAccount}
        onClose={() => setViewingAccount(null)}
        onEdit={(account) => setEditingAccount(account)}
      />

      <AccountEditModal 
        account={editingAccount}
        open={!!editingAccount}
        isLoading={isLoading}
        onClose={() => setEditingAccount(null)}
        onSave={handleEdit}
        onChange={(account) => setEditingAccount(account)}
      />

      <AlertDialog open={!!deletingAccount} onOpenChange={() => setDeletingAccount(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the account "{deletingAccount?.accountNumber}". This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  Deleting...
                </>
              ) : (
                "Delete"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </DashboardLayout>
  );
};

export default Accounts;
