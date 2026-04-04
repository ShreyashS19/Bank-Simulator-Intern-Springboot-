import { useState } from "react";
import { motion } from "framer-motion";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { Search, ArrowLeftRight, CheckCircle, TrendingUp, Download, Loader2 } from "lucide-react";
import { transactionService, Transaction } from "@/services/transactionService";

const Transactions = () => {
  const [searchedTransactions, setSearchedTransactions] = useState<Transaction[]>([]);
  const [searchAccountNumber, setSearchAccountNumber] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [formData, setFormData] = useState({
    senderAccountNumber: "",
    receiverAccountNumber: "",
    amount: "",
    transactionType: "ONLINE",
    description: "",
    pin: ""
  });

  const handleSearchByAccount = async () => {
    if (!searchAccountNumber.trim()) {
      toast.error("Please enter an account number");
      return;
    }

    if (!/^\d+$/.test(searchAccountNumber)) {
      toast.error("Account number must contain only digits");
      return;
    }

    setIsLoading(true);
    setHasSearched(true);
    try {
      const data = await transactionService.getTransactionsByAccount(searchAccountNumber);
      setSearchedTransactions(data);
      if (data.length === 0) {
        toast.info(`No transactions found for account ${searchAccountNumber}`);
      } else {
        toast.success(`Found ${data.length} transaction(s)`);
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Failed to fetch transactions. Please try again.";
      toast.error(errorMessage);
      setSearchedTransactions([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownloadExcel = async () => {
    if (!searchAccountNumber.trim()) {
      toast.error("Please enter an account number first");
      return;
    }

    if (searchedTransactions.length === 0) {
      toast.error("No transactions to download");
      return;
    }

    try {
      const blob = await transactionService.downloadTransactions(searchAccountNumber);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `transactions_${searchAccountNumber}_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success("Excel file downloaded successfully!");
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Failed to download Excel file";
      toast.error(errorMessage);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCreating(true);

    try {
      const transactionData = {
        senderAccountNumber: formData.senderAccountNumber.trim(),
        receiverAccountNumber: formData.receiverAccountNumber.trim(),
        amount: parseFloat(formData.amount),
        transactionType: "ONLINE", 
        description: formData.description.trim() || undefined,
        pin: formData.pin.trim(),
        timestamp: new Date().toISOString()
      };

      const transactionId = await transactionService.createTransaction(transactionData);
      toast.success(`Transaction completed successfully!`);
      handleReset();

      if (searchAccountNumber === formData.senderAccountNumber || searchAccountNumber === formData.receiverAccountNumber) {
        handleSearchByAccount();
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || "Transaction failed. Please try again.";
      toast.error(errorMessage);
    } finally {
      setIsCreating(false);
    }
  };

  const handleReset = () => {
    setFormData({
      senderAccountNumber: "",
      receiverAccountNumber: "",
      amount: "",
      transactionType: "ONLINE",
      description: "",
      pin: ""
    });
  };

  const totalTransactions = searchedTransactions.length;
  const totalVolume = searchedTransactions.reduce((sum, t) => sum + Number(t.amount), 0);

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Transaction Management</h1>
          <p className="text-muted-foreground mt-1">Create and track online transactions</p>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Transactions</CardTitle>
              <ArrowLeftRight className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{totalTransactions}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Successful</CardTitle>
              <CheckCircle className="h-4 w-4 text-secondary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{totalTransactions}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Volume</CardTitle>
              <TrendingUp className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₹{totalVolume.toLocaleString()}</div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ArrowLeftRight className="h-5 w-5" />
              Create New Online Transaction
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="senderAccountNumber">
                    Sender Account Number <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="senderAccountNumber"
                    value={formData.senderAccountNumber}
                    onChange={(e) => setFormData({ ...formData, senderAccountNumber: e.target.value })}
                    placeholder="Enter sender account"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="receiverAccountNumber">
                    Receiver Account Number <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="receiverAccountNumber"
                    value={formData.receiverAccountNumber}
                    onChange={(e) => setFormData({ ...formData, receiverAccountNumber: e.target.value })}
                    placeholder="Enter receiver account"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="amount">
                    Amount <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="amount"
                    type="number"
                    step="1.00"
                    min="1.00"
                    value={formData.amount}
                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                    placeholder="Enter amount"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="transactionType">Transaction Type</Label>
                  <Input
                    id="transactionType"
                    value="ONLINE"
                    disabled
                    className="bg-gray-100"
                  />
                  <p className="text-xs text-muted-foreground">All transactions are online</p>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="description">Description (Optional)</Label>
                  <Input
                    id="description"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="Enter description"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="pin">
                    Sender PIN <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="pin"
                    type="password"
                    maxLength={6}
                    value={formData.pin}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      if (value.length <= 6) {
                        setFormData({ ...formData, pin: value });
                      }
                    }}
                    placeholder="6-digit PIN"
                    required
                  />
                </div>
              </div>
              <div className="flex gap-3">
                <Button type="submit" disabled={isCreating}>
                  {isCreating ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Processing...
                    </>
                  ) : (
                    "Create Transaction"
                  )}
                </Button>
                <Button type="button" variant="outline" onClick={handleReset}>Reset</Button>
              </div>
            </form>
            
            <div className="mt-4">
              <Button
                type="button"
                onClick={() => {
                  const email = 'bank.simulator.issue@gmail.com';
                  const subject = 'Issue Report - Bank Simulator - Transaction Dashboard';
                  const body = `Dear Admin,%0D%0A%0D%0AI am facing an issue with the Transaction Management dashboard. Please look into this.%0D%0A%0D%0ADescription of issue:%0D%0A%0D%0A`;
                  
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
     
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Search className="h-5 w-5" />
              Search Transactions by Account Number
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="flex-1">
                <Input
                  placeholder="Enter account number..."
                  value={searchAccountNumber}
                  onChange={(e) => setSearchAccountNumber(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearchByAccount()}
                  disabled={isLoading}
                />
              </div>
              <Button 
                onClick={handleSearchByAccount} 
                disabled={isLoading}
                className="sm:w-auto"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Searching...
                  </>
                ) : (
                  <>
                    <Search className="mr-2 h-4 w-4" />
                    Search
                  </>
                )}
              </Button>
              <Button 
                onClick={handleDownloadExcel}
                variant="secondary"
                disabled={searchedTransactions.length === 0}
                className="sm:w-auto"
              >
                <Download className="mr-2 h-4 w-4" />
                Download Excel
              </Button>
            </div>
            
            {hasSearched && (
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="mt-6"
              >
                {isLoading ? (
                  <div className="flex items-center justify-center py-12">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                  </div>
                ) : searchedTransactions.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground">
                    No transactions found for account number: {searchAccountNumber}
                  </div>
                ) : (
                  <div className="overflow-x-auto rounded-md border">
                    <table className="w-full">
                      <thead className="bg-muted/50">
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">Transaction ID</th>
                          <th className="text-left py-3 px-4 font-medium">Sender Account</th>
                          <th className="text-left py-3 px-4 font-medium">Receiver Account</th>
                          <th className="text-left py-3 px-4 font-medium">Amount</th>
                          <th className="text-left py-3 px-4 font-medium">Type</th>
                          <th className="text-left py-3 px-4 font-medium">Description</th>
                          <th className="text-left py-3 px-4 font-medium">Date/Time</th>
                        </tr>
                      </thead>
                      <tbody>
                        {searchedTransactions.map((transaction) => (
                          <motion.tr 
                            key={transaction.transactionId} 
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="border-b hover:bg-muted/50 transition-colors"
                          >
                            <td className="py-3 px-4 font-mono text-sm">
                              {transaction.transactionId || 'N/A'}
                            </td>
                            <td className="py-3 px-4 font-mono text-sm">
                              {transaction.senderAccountNumber}
                            </td>
                            <td className="py-3 px-4 font-mono text-sm">
                              {transaction.receiverAccountNumber}
                            </td>
                            <td className="py-3 px-4 font-semibold">
                              ₹{Number(transaction.amount).toLocaleString()}
                            </td>
                            <td className="py-3 px-4">
                              <span className="px-2 py-1 rounded-full text-xs bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400">
                                {transaction.transactionType}
                              </span>
                            </td>
                            <td className="py-3 px-4 text-sm">
                              {transaction.description || '-'}
                            </td>
                            <td className="py-3 px-4 text-sm text-muted-foreground">
                              {transaction.createdDate 
                                ? new Date(transaction.createdDate).toLocaleString()
                                : 'N/A'}
                            </td>
                          </motion.tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </motion.div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default Transactions;
