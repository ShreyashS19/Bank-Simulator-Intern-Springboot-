import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import DashboardLayout from '@/components/DashboardLayout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Loader2, FileText, CheckCircle, XCircle, Clock, DollarSign, TrendingUp, AlertCircle } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend, LineChart, Line, XAxis, YAxis, CartesianGrid } from 'recharts';
import { loanService, LoanResponse, LoanStatistics } from '@/services/loanService';
import { toast } from 'sonner';

const AdminLoanManagement = () => {
  const [loans, setLoans] = useState<LoanResponse[]>([]);
  const [statistics, setStatistics] = useState<LoanStatistics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [updatingLoanId, setUpdatingLoanId] = useState<string | null>(null);

  useEffect(() => {
    loadLoanData();
  }, []);

  const loadLoanData = async () => {
    setIsLoading(true);
    setError('');
    
    try {
      const [loansData, statsData] = await Promise.all([
        loanService.getAllLoans(),
        loanService.getLoanStatistics()
      ]);
      
      setLoans(loansData);
      setStatistics(statsData);
    } catch (err: any) {
      console.error('Error loading loan data:', err);
      setError(err.response?.data?.message || 'Failed to load loan data');
      toast.error('Failed to load loan data');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusUpdate = async (loanId: string, newStatus: string) => {
    setUpdatingLoanId(loanId);
    
    try {
      await loanService.updateLoanStatus(loanId, { status: newStatus });
      toast.success('Loan status updated successfully');
      
      // Refresh data after successful update
      await loadLoanData();
    } catch (err: any) {
      console.error('Error updating loan status:', err);
      toast.error(err.response?.data?.message || 'Failed to update loan status');
    } finally {
      setUpdatingLoanId(null);
    }
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center min-h-[600px]">
          <Loader2 className="h-12 w-12 animate-spin text-primary" />
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </DashboardLayout>
    );
  }

  // Prepare loan purpose distribution data for pie chart
  const loanPurposeData = loans.reduce((acc, loan) => {
    const purpose = loan.loanPurpose;
    const existing = acc.find(item => item.name === purpose);
    if (existing) {
      existing.value += 1;
    } else {
      acc.push({ name: purpose, value: 1 });
    }
    return acc;
  }, [] as Array<{ name: string; value: number }>);

  // Prepare loan status by month data for line chart
  const loanStatusByMonth = loans.reduce((acc, loan) => {
    const date = new Date(loan.applicationDate);
    const monthYear = `${date.getMonth() + 1}/${date.getFullYear()}`;
    
    const existing = acc.find(item => item.month === monthYear);
    if (existing) {
      existing[loan.status] = (existing[loan.status] || 0) + 1;
      existing.total += 1;
    } else {
      acc.push({
        month: monthYear,
        [loan.status]: 1,
        total: 1
      });
    }
    return acc;
  }, [] as Array<{ month: string; [key: string]: any }>);

  // Sort by month
  loanStatusByMonth.sort((a, b) => {
    const [monthA, yearA] = a.month.split('/').map(Number);
    const [monthB, yearB] = b.month.split('/').map(Number);
    return yearA !== yearB ? yearA - yearB : monthA - monthB;
  });

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  return (
    <DashboardLayout>
      <motion.div
        className="space-y-8"
        initial="hidden"
        animate="visible"
        variants={{
          hidden: {},
          visible: { transition: { staggerChildren: 0.08 } },
        }}
      >
        <motion.div
          variants={{ hidden: { opacity: 0, y: 24 }, visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } } }}
        >
          <h1 className="text-3xl font-bold text-foreground">Loan Management</h1>
          <p className="text-muted-foreground mt-1">Manage and review all loan applications</p>
        </motion.div>

        {/* Statistics Cards */}
        {statistics && (
          <motion.div
            className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6"
            variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.1 } } }}
          >
            <StatCard
              title="Total Applications"
              value={statistics.totalApplications}
              icon={<FileText className="h-4 w-4 text-primary" />}
            />
            <StatCard
              title="Approved"
              value={statistics.approvedCount}
              icon={<CheckCircle className="h-4 w-4 text-green-500" />}
            />
            <StatCard
              title="Rejected"
              value={statistics.rejectedCount}
              icon={<XCircle className="h-4 w-4 text-red-500" />}
            />
            <StatCard
              title="Under Review"
              value={statistics.underReviewCount}
              icon={<Clock className="h-4 w-4 text-yellow-500" />}
            />
            <StatCard
              title="Total Approved Amount"
              value={`₹${(statistics.totalApprovedAmount / 100000).toFixed(1)}L`}
              icon={<DollarSign className="h-4 w-4 text-primary" />}
            />
            <StatCard
              title="Avg Eligibility Score"
              value={statistics.averageEligibilityScore.toFixed(2)}
              icon={<TrendingUp className="h-4 w-4 text-primary" />}
            />
          </motion.div>
        )}

        {/* Loan Management Table */}
        <motion.div
          variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.55, ease: 'easeOut' } } }}
        >
          <LoanManagementTable 
            loans={loans} 
            onStatusUpdate={handleStatusUpdate}
            updatingLoanId={updatingLoanId}
          />
        </motion.div>

        {/* Analytics Charts */}
        <motion.div
          className="grid gap-6 md:grid-cols-2"
          variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.55, ease: 'easeOut', delay: 0.1 } } }}
        >
          <LoanPurposeChart data={loanPurposeData} colors={COLORS} />
          <LoanStatusByMonthChart data={loanStatusByMonth} />
        </motion.div>
      </motion.div>
    </DashboardLayout>
  );
};

// Stat Card Component
interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
}

const StatCard = ({ title, value, icon }: StatCardProps) => (
  <motion.div variants={{ hidden: { opacity: 0, y: 20, scale: 0.97 }, visible: { opacity: 1, y: 0, scale: 1, transition: { duration: 0.45, ease: 'easeOut' } } }}>
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
        {icon}
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
      </CardContent>
    </Card>
  </motion.div>
);

// Loan Management Table Component
interface LoanManagementTableProps {
  loans: LoanResponse[];
  onStatusUpdate: (loanId: string, newStatus: string) => void;
  updatingLoanId: string | null;
}

const LoanManagementTable = ({ loans, onStatusUpdate, updatingLoanId }: LoanManagementTableProps) => {
  const getStatusBadge = (status: string) => {
    const badgeClasses: Record<string, string> = {
      APPROVED: 'bg-emerald-100 text-emerald-800 border-emerald-200',
      REJECTED: 'bg-red-100 text-red-800 border-red-200',
      UNDER_REVIEW: 'bg-amber-100 text-amber-800 border-amber-200',
      PENDING: 'bg-slate-100 text-slate-800 border-slate-200',
      PENDING_BANK_REVIEW: 'bg-blue-100 text-blue-800 border-blue-200',
    };
    return (
      <Badge variant="outline" className={badgeClasses[status] || 'bg-slate-100 text-slate-800 border-slate-200'}>
        {status.replace('_', ' ')}
      </Badge>
    );
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>All Loan Applications</CardTitle>
        <p className="text-sm text-muted-foreground">Review and manage loan applications</p>
      </CardHeader>
      <CardContent>
        {loans.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">No loan applications found</p>
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Loan ID</TableHead>
                  <TableHead>Reference No.</TableHead>
                  <TableHead>Account Number</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Eligibility Score</TableHead>
                  <TableHead>Interest Rate</TableHead>
                  <TableHead>Application Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loans.map((loan) => (
                  <TableRow key={loan.loanId}>
                    <TableCell className="font-medium">{loan.loanId}</TableCell>
                    <TableCell className="font-mono text-xs">{loan.referenceNumber || '—'}</TableCell>
                    <TableCell className="font-mono text-sm">{loan.accountNumber}</TableCell>
                    <TableCell>₹{loan.loanAmount.toLocaleString()}</TableCell>
                    <TableCell>{getStatusBadge(loan.status)}</TableCell>
                    <TableCell>{loan.eligibilityScore.toFixed(2)}%</TableCell>
                    <TableCell>{loan.interestRate}%</TableCell>
                    <TableCell>{new Date(loan.applicationDate).toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Select
                        value={loan.status}
                        onValueChange={(value) => onStatusUpdate(loan.loanId, value)}
                        disabled={updatingLoanId === loan.loanId}
                      >
                        <SelectTrigger className="w-[150px]">
                          {updatingLoanId === loan.loanId ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            <SelectValue />
                          )}
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="PENDING_BANK_REVIEW">Pending Bank Review</SelectItem>
                          <SelectItem value="PENDING">Pending</SelectItem>
                          <SelectItem value="APPROVED">Approved</SelectItem>
                          <SelectItem value="REJECTED">Rejected</SelectItem>
                          <SelectItem value="UNDER_REVIEW">Under Review</SelectItem>
                        </SelectContent>
                      </Select>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

// Loan Purpose Distribution Pie Chart Component
interface LoanPurposeChartProps {
  data: Array<{ name: string; value: number }>;
  colors: string[];
}

const LoanPurposeChart = ({ data, colors }: LoanPurposeChartProps) => (
  <Card>
    <CardHeader>
      <CardTitle>Loan Purpose Distribution</CardTitle>
      <p className="text-sm text-muted-foreground">Distribution of loans by purpose</p>
    </CardHeader>
    <CardContent>
      {data.length === 0 ? (
        <p className="text-center text-muted-foreground py-8">No data available</p>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
              outerRadius={80}
              fill="#8884d8"
              dataKey="value"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      )}
    </CardContent>
  </Card>
);

// Loan Status by Month Line Chart Component
interface LoanStatusByMonthChartProps {
  data: Array<{ month: string; [key: string]: any }>;
}

const LoanStatusByMonthChart = ({ data }: LoanStatusByMonthChartProps) => (
  <Card>
    <CardHeader>
      <CardTitle>Loan Status by Month</CardTitle>
      <p className="text-sm text-muted-foreground">Monthly trend of loan applications</p>
    </CardHeader>
    <CardContent>
      {data.length === 0 ? (
        <p className="text-center text-muted-foreground py-8">No data available</p>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
            <XAxis dataKey="month" tick={{ fontSize: 11 }} />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="APPROVED" stroke="#22c55e" strokeWidth={2} />
            <Line type="monotone" dataKey="REJECTED" stroke="#ef4444" strokeWidth={2} />
            <Line type="monotone" dataKey="UNDER_REVIEW" stroke="#eab308" strokeWidth={2} />
            <Line type="monotone" dataKey="PENDING_BANK_REVIEW" stroke="#3b82f6" strokeWidth={2} />
            <Line type="monotone" dataKey="PENDING" stroke="#3b82f6" strokeWidth={2} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </CardContent>
  </Card>
);

export default AdminLoanManagement;
