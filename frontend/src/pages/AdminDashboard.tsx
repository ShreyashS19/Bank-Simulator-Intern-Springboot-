import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import DashboardLayout from '../components/DashboardLayout';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../components/ui/table';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../components/ui/alert-dialog';
import { toast } from 'sonner';
import {
  Users,
  CreditCard,
  ArrowLeftRight,
  Search,
  Trash2,
  Shield,
  UserX,
  UserCheck,
  Filter,
} from 'lucide-react';
import { customerService, Customer } from '../services/customerService';
import { accountService, Account } from '../services/accountService';
import { transactionService, Transaction } from '../services/transactionService';
import { authService, User } from '../services/authService';

type TabType = 'users' | 'customers' | 'accounts' | 'transactions';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<TabType>('users');
  const [loading, setLoading] = useState(false);

  const [users, setUsers] = useState<User[]>([]);
  const [userSearch, setUserSearch] = useState('');

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [customerSearch, setCustomerSearch] = useState('');

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [accountSearch, setAccountSearch] = useState('');

  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [transactionSearch, setTransactionSearch] = useState('');
  const [transactionTypeFilter, setTransactionTypeFilter] = useState<string>('all');

  const [deleteDialog, setDeleteDialog] = useState<{
    open: boolean;
    type: 'user' | 'customer' | 'account' | 'transaction' | null;
    id: string;
    name: string;
  }>({
    open: false,
    type: null,
    id: '',
    name: '',
  });

  const [stats, setStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    totalCustomers: 0,
    activeCustomers: 0,
    totalAccounts: 0,
    activeAccounts: 0,
    totalTransactions: 0,
    totalVolume: 0,
  });

  useEffect(() => {
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    if (!isAdmin) {
      toast.error('Unauthorized access');
      navigate('/dashboard');
      return;
    }

    loadAllData();
  }, [navigate]);

  const loadAllData = async () => {
    setLoading(true);
    try {
      const [usersData, customersData, accountsData, transactionsData] = await Promise.all([
        authService.getAllUsers(),
        customerService.getAllCustomers(),
        accountService.getAllAccounts(),
        transactionService.getAllTransactions(),
      ]);

      setUsers(usersData.data || []);
      setCustomers(customersData);
      setAccounts(accountsData);
      setTransactions(transactionsData);

      const activeUsers = (usersData.data || []).filter((u: User) => u.active).length;
      const activeCustomers = customersData.filter(
        (c) => c.status.toLowerCase() === 'active'
      ).length;
      const activeAccounts = accountsData.filter(
        (a) => a.status.toUpperCase() === 'ACTIVE'
      ).length;
      const totalVolume = transactionsData.reduce(
        (sum, t) => sum + Number(t.amount || 0),
        0
      );

      setStats({
        totalUsers: (usersData.data || []).length,
        activeUsers,
        totalCustomers: customersData.length,
        activeCustomers,
        totalAccounts: accountsData.length,
        activeAccounts,
        totalTransactions: transactionsData.length,
        totalVolume,
      });

      toast.success('Admin data loaded successfully');
    } catch (error: any) {
      toast.error('Failed to load admin data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const toggleUserStatus = async (user: User) => {
    try {
      const newStatus = !user.active;
      await authService.updateUserStatus(user.email, newStatus);
      toast.success(`User ${newStatus ? 'activated' : 'deactivated'} successfully`);
      loadAllData();
    } catch (error: any) {
      toast.error('Failed to update user status');
    }
  };

  const toggleCustomerStatus = async (customer: Customer) => {
    try {
      const newStatus = customer.status.toLowerCase() === 'active' ? 'Inactive' : 'Active';
      await customerService.updateCustomerByAadhar(customer.aadharNumber, {
        ...customer,
        status: newStatus,
      });
      toast.success(`Customer ${newStatus === 'Active' ? 'activated' : 'deactivated'}`);
      loadAllData();
    } catch (error: any) {
      toast.error('Failed to update customer status');
    }
  };

  const deleteCustomer = async () => {
    try {
      await customerService.deleteCustomer(deleteDialog.id);
      toast.success('Customer deleted successfully');
      setDeleteDialog({ open: false, type: null, id: '', name: '' });
      loadAllData();
    } catch (error: any) {
      toast.error('Failed to delete customer');
    }
  };

  const toggleAccountStatus = async (account: Account) => {
    try {
      const newStatus = account.status.toUpperCase() === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await accountService.updateAccount(account.accountNumber, {
        ...account,
        status: newStatus,
      });
      toast.success(`Account ${newStatus === 'ACTIVE' ? 'activated' : 'deactivated'}`);
      loadAllData();
    } catch (error: any) {
      toast.error('Failed to update account status');
    }
  };

  const deleteAccount = async () => {
    try {
      await accountService.deleteAccount(deleteDialog.id);
      toast.success('Account deleted successfully');
      setDeleteDialog({ open: false, type: null, id: '', name: '' });
      loadAllData();
    } catch (error: any) {
      toast.error('Failed to delete account');
    }
  };

  const deleteTransaction = async () => {
    try {
      await transactionService.deleteTransaction(deleteDialog.id);
      toast.success('Transaction deleted successfully');
      setDeleteDialog({ open: false, type: null, id: '', name: '' });
      loadAllData();
    } catch (error: any) {
      toast.error('Failed to delete transaction. Please try again.');
    }
  };

  const filteredUsers = users.filter(
    (u) =>
      u.fullName.toLowerCase().includes(userSearch.toLowerCase()) ||
      u.email.toLowerCase().includes(userSearch.toLowerCase())
  );

  const filteredCustomers = customers.filter(
    (c) =>
      c.name.toLowerCase().includes(customerSearch.toLowerCase()) ||
      c.email.toLowerCase().includes(customerSearch.toLowerCase()) ||
      c.aadharNumber.includes(customerSearch)
  );

  const filteredAccounts = accounts.filter(
    (a) =>
      a.accountNumber.includes(accountSearch) ||
      a.nameOnAccount.toLowerCase().includes(accountSearch.toLowerCase())
  );

  const filteredTransactions = transactions.filter((t) => {
    const matchesSearch =
      t.senderAccountNumber.includes(transactionSearch) ||
      t.receiverAccountNumber.includes(transactionSearch) ||
      (t.transactionId && t.transactionId.includes(transactionSearch));
    const matchesType =
      transactionTypeFilter === 'all' || t.transactionType === transactionTypeFilter;
    return matchesSearch && matchesType;
  });

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(amount);
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold flex items-center gap-2">
              <Shield className="h-8 w-8 text-primary" />
              Admin Dashboard
            </h1>
            <p className="text-muted-foreground">Full system access and management</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Total Users</CardTitle>
                <Shield className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stats.totalUsers}</div>
                <p className="text-xs text-muted-foreground">
                  {stats.activeUsers} active
                </p>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Total Customers</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stats.totalCustomers}</div>
                <p className="text-xs text-muted-foreground">
                  {stats.activeCustomers} active
                </p>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Total Accounts</CardTitle>
                <CreditCard className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stats.totalAccounts}</div>
                <p className="text-xs text-muted-foreground">
                  {stats.activeAccounts} active
                </p>
              </CardContent>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Transaction Volume</CardTitle>
                <ArrowLeftRight className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{formatCurrency(stats.totalVolume)}</div>
                <p className="text-xs text-muted-foreground">{stats.totalTransactions} total</p>
              </CardContent>
            </Card>
          </motion.div>
        </div>

        <Card>
          <CardHeader>
            <div className="flex gap-4 border-b">
              <button
                onClick={() => setActiveTab('users')}
                className={`pb-2 px-4 font-medium transition-colors ${
                  activeTab === 'users'
                    ? 'border-b-2 border-primary text-primary'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <Shield className="h-4 w-4 inline mr-2" />
                Users
              </button>
              <button
                onClick={() => setActiveTab('customers')}
                className={`pb-2 px-4 font-medium transition-colors ${
                  activeTab === 'customers'
                    ? 'border-b-2 border-primary text-primary'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <Users className="h-4 w-4 inline mr-2" />
                Customers
              </button>
              <button
                onClick={() => setActiveTab('accounts')}
                className={`pb-2 px-4 font-medium transition-colors ${
                  activeTab === 'accounts'
                    ? 'border-b-2 border-primary text-primary'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <CreditCard className="h-4 w-4 inline mr-2" />
                Accounts
              </button>
              <button
                onClick={() => setActiveTab('transactions')}
                className={`pb-2 px-4 font-medium transition-colors ${
                  activeTab === 'transactions'
                    ? 'border-b-2 border-primary text-primary'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <ArrowLeftRight className="h-4 w-4 inline mr-2" />
                Transactions
              </button>
            </div>
          </CardHeader>

          <CardContent>
            {activeTab === 'users' && (
              <div className="space-y-4">
                <div className="flex gap-4">
                  <div className="relative flex-1">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search by name or email..."
                      value={userSearch}
                      onChange={(e) => setUserSearch(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                </div>

                <div className="rounded-md border overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>User ID</TableHead>
                        <TableHead>Name</TableHead>
                        <TableHead>Email</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Created Date</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredUsers.map((user) => (
                        <TableRow key={user.id}>
                          <TableCell className="font-mono text-sm">{user.id}</TableCell>
                          <TableCell className="font-medium">{user.fullName}</TableCell>
                          <TableCell>{user.email}</TableCell>
                          <TableCell>
                            <span
                              className={`inline-block px-2 py-1 rounded-full text-xs ${
                                user.active
                                  ? 'bg-green-100 text-green-700'
                                  : 'bg-red-100 text-red-700'
                              }`}
                            >
                              {user.active ? 'Active' : 'Inactive'}
                            </span>
                          </TableCell>
                          <TableCell className="text-sm text-muted-foreground">
                            {new Date(user.createdAt || '').toLocaleDateString()}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => toggleUserStatus(user)}
                              title={user.active ? 'Deactivate' : 'Activate'}
                            >
                              {user.active ? (
                                <UserX className="h-4 w-4 text-orange-600" />
                              ) : (
                                <UserCheck className="h-4 w-4 text-green-600" />
                              )}
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>
            )}

            {activeTab === 'customers' && (
              <div className="space-y-4">
                <div className="flex gap-4">
                  <div className="relative flex-1">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search by name, email, or Aadhar..."
                      value={customerSearch}
                      onChange={(e) => setCustomerSearch(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                </div>

                <div className="rounded-md border overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Customer ID</TableHead>
                        <TableHead>Name</TableHead>
                        <TableHead>Email</TableHead>
                        <TableHead>Phone</TableHead>
                        <TableHead>Aadhar</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredCustomers.map((customer) => (
                        <TableRow key={customer.aadharNumber}>
                          <TableCell className="font-mono text-sm">
                            {customer.customerId || 'N/A'}
                          </TableCell>
                          <TableCell className="font-medium">{customer.name}</TableCell>
                          <TableCell>{customer.email}</TableCell>
                          <TableCell className="font-mono">{customer.phoneNumber}</TableCell>
                          <TableCell className="font-mono">{customer.aadharNumber}</TableCell>
                          <TableCell>
                            <span
                              className={`inline-block px-2 py-1 rounded-full text-xs ${
                                customer.status.toLowerCase() === 'active'
                                  ? 'bg-green-100 text-green-700'
                                  : 'bg-red-100 text-red-700'
                              }`}
                            >
                              {customer.status}
                            </span>
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() =>
                                  setDeleteDialog({
                                    open: true,
                                    type: 'customer',
                                    id: customer.aadharNumber,
                                    name: customer.name,
                                  })
                                }
                                title="Delete customer"
                              >
                                <Trash2 className="h-4 w-4 text-destructive" />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>
            )}

            {activeTab === 'accounts' && (
              <div className="space-y-4">
                <div className="flex gap-4">
                  <div className="relative flex-1">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search by account number or name..."
                      value={accountSearch}
                      onChange={(e) => setAccountSearch(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                </div>

                <div className="rounded-md border overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Account Number</TableHead>
                        <TableHead>Name</TableHead>
                        <TableHead>Bank</TableHead>
                        <TableHead>IFSC</TableHead>
                        <TableHead>Balance</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredAccounts.map((account) => (
                        <TableRow key={account.accountNumber}>
                          <TableCell className="font-mono text-sm">
                            {account.accountNumber}
                          </TableCell>
                          <TableCell className="font-medium">{account.nameOnAccount}</TableCell>
                          <TableCell>{account.bankName}</TableCell>
                          <TableCell className="font-mono text-sm">{account.ifscCode}</TableCell>
                          <TableCell className="font-semibold">
                            {formatCurrency(account.amount)}
                          </TableCell>
                          <TableCell>
                            <span
                              className={`inline-block px-2 py-1 rounded-full text-xs ${
                                account.status.toUpperCase() === 'ACTIVE'
                                  ? 'bg-green-100 text-green-700'
                                  : 'bg-red-100 text-red-700'
                              }`}
                            >
                              {account.status}
                            </span>
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => toggleAccountStatus(account)}
                                title={
                                  account.status.toUpperCase() === 'ACTIVE'
                                    ? 'Deactivate'
                                    : 'Activate'
                                }
                              >
                                {account.status.toUpperCase() === 'ACTIVE' ? (
                                  <UserX className="h-4 w-4 text-orange-600" />
                                ) : (
                                  <UserCheck className="h-4 w-4 text-green-600" />
                                )}
                              </Button>
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() =>
                                  setDeleteDialog({
                                    open: true,
                                    type: 'account',
                                    id: account.accountNumber,
                                    name: account.nameOnAccount,
                                  })
                                }
                                title="Delete account"
                              >
                                <Trash2 className="h-4 w-4 text-destructive" />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>
            )}

            {activeTab === 'transactions' && (
              <div className="space-y-4">
                <div className="flex gap-4">
                  <div className="relative flex-1">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search by transaction ID or account number..."
                      value={transactionSearch}
                      onChange={(e) => setTransactionSearch(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                  <Select
                    value={transactionTypeFilter}
                    onValueChange={setTransactionTypeFilter}
                  >
                    <SelectTrigger className="w-[180px]">
                      <Filter className="h-4 w-4 mr-2" />
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Types</SelectItem>
                      <SelectItem value="ONLINE">Online</SelectItem>
                      <SelectItem value="UPI">UPI</SelectItem>
                      <SelectItem value="NEFT">NEFT</SelectItem>
                      <SelectItem value="RTGS">RTGS</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="rounded-md border overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Transaction ID</TableHead>
                        <TableHead>Sender</TableHead>
                        <TableHead>Receiver</TableHead>
                        <TableHead>Amount</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Date</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredTransactions.map((txn, index) => (
                        <TableRow key={txn.transactionId || index}>
                          <TableCell className="font-mono text-sm">
                            {txn.transactionId}
                          </TableCell>
                          <TableCell className="font-mono text-sm">
                            {txn.senderAccountNumber}
                          </TableCell>
                          <TableCell className="font-mono text-sm">
                            {txn.receiverAccountNumber}
                          </TableCell>
                          <TableCell className="font-semibold">
                            {formatCurrency(Number(txn.amount))}
                          </TableCell>
                          <TableCell>
                            <span className="inline-block px-2 py-1 rounded bg-blue-100 text-blue-700 text-xs">
                              {txn.transactionType}
                            </span>
                          </TableCell>
                          <TableCell className="text-sm text-muted-foreground">
                            {new Date(txn.createdDate || txn.timestamp).toLocaleString()}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() =>
                                setDeleteDialog({
                                  open: true,
                                  type: 'transaction',
                                  id: txn.transactionId || '',
                                  name: `Transaction ${txn.transactionId}`,
                                })
                              }
                              title="Delete transaction"
                            >
                              <Trash2 className="h-4 w-4 text-destructive" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <AlertDialog
        open={deleteDialog.open}
        onOpenChange={(open) =>
          !open && setDeleteDialog({ open: false, type: null, id: '', name: '' })
        }
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete{' '}
              <span className="font-semibold">{deleteDialog.name}</span>. This action
              cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (deleteDialog.type === 'customer') {
                  deleteCustomer();
                } else if (deleteDialog.type === 'account') {
                  deleteAccount();
                } else if (deleteDialog.type === 'transaction') {
                  deleteTransaction();
                }
              }}
              className="bg-destructive hover:bg-destructive/90"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </DashboardLayout>
  );
};

export default AdminDashboard;
