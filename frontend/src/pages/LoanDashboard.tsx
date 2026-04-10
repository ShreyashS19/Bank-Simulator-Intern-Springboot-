import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import DashboardLayout from '@/components/DashboardLayout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Loader2, TrendingUp, DollarSign, Percent, FileText, AlertCircle } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, RadialBarChart, RadialBar, Legend } from 'recharts';
import { loanService, LoanResponse } from '@/services/loanService';
import { customerService } from '@/services/customerService';
import { accountService } from '@/services/accountService';
import { tokenUtils } from '@/services/authService';

const LoanDashboard = () => {
  const [loans, setLoans] = useState<LoanResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadLoanData();
  }, []);

  const loadLoanData = async () => {
    setIsLoading(true);
    setError('');
    
    try {
      const user = tokenUtils.getUser();
      if (!user) {
        setError('User not authenticated');
        return;
      }

      // Get customer by email
      const customer = await customerService.getCustomerByAadhar(user.email);
      
      // Get all accounts and find the one belonging to this customer
      const allAccounts = await accountService.getAllAccounts();
      const userAccount = allAccounts.find(acc => acc.aadharNumber === customer.aadharNumber);
      
      if (!userAccount) {
        setError('No account found for this user');
        return;
      }
      
      // Fetch loans for the account number
      const loanData = await loanService.getLoansByAccount(userAccount.accountNumber);
      setLoans(loanData);
    } catch (err: any) {
      console.error('Error loading loan data:', err);
      setError(err.response?.data?.message || 'Failed to load loan data');
    } finally {
      setIsLoading(false);
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

  // Get the most recent loan for dashboard metrics
  const latestLoan = loans.length > 0 ? loans[0] : null;
  const activeLoans = loans.filter(loan => loan.status === 'APPROVED').length;

  // Calculate max loan eligibility (simplified - based on latest loan's eligibility score)
  const maxLoanEligibility = latestLoan 
    ? Math.round((latestLoan.eligibilityScore / 100) * 10000000) 
    : 0;

  // Prepare credit score gauge data
  const creditScoreData = latestLoan ? [{
    name: 'Credit Score',
    value: latestLoan.factorScores ? 
      Math.round((latestLoan.eligibilityScore / 100) * 600 + 300) : 
      300,
    fill: getCreditScoreColor(latestLoan.eligibilityScore)
  }] : [];

  // Prepare factor scores bar chart data
  const factorScoresData = latestLoan?.factorScores ? [
    { name: 'Income', score: latestLoan.factorScores.incomeScore, max: 120 },
    { name: 'Employment', score: latestLoan.factorScores.employmentScore, max: 80 },
    { name: 'DTI', score: latestLoan.factorScores.dtiScore, max: 100 },
    { name: 'Repayment', score: latestLoan.factorScores.repaymentHistoryScore, max: 100 },
    { name: 'Age', score: latestLoan.factorScores.ageScore, max: 60 },
    { name: 'Existing Loans', score: latestLoan.factorScores.existingLoansScore, max: 60 },
    { name: 'Collateral', score: latestLoan.factorScores.collateralScore, max: 70 },
    { name: 'Banking', score: latestLoan.factorScores.bankingRelationshipScore, max: 50 },
    { name: 'Residence', score: latestLoan.factorScores.residenceScore, max: 40 },
    { name: 'Purpose', score: latestLoan.factorScores.loanPurposeScore, max: 40 },
    { name: 'Guarantor', score: latestLoan.factorScores.guarantorScore, max: 30 },
  ] : [];

  // Prepare factor information grid data
  const factorGridData = latestLoan?.factorScores ? [
    { name: 'Income Score', score: latestLoan.factorScores.incomeScore, max: 120 },
    { name: 'Employment Score', score: latestLoan.factorScores.employmentScore, max: 80 },
    { name: 'DTI Score', score: latestLoan.factorScores.dtiScore, max: 100 },
    { name: 'Repayment History', score: latestLoan.factorScores.repaymentHistoryScore, max: 100 },
    { name: 'Age Score', score: latestLoan.factorScores.ageScore, max: 60 },
    { name: 'Existing Loans', score: latestLoan.factorScores.existingLoansScore, max: 60 },
    { name: 'Collateral Score', score: latestLoan.factorScores.collateralScore, max: 70 },
    { name: 'Banking Relationship', score: latestLoan.factorScores.bankingRelationshipScore, max: 50 },
    { name: 'Residence Score', score: latestLoan.factorScores.residenceScore, max: 40 },
    { name: 'Loan Purpose', score: latestLoan.factorScores.loanPurposeScore, max: 40 },
    { name: 'Guarantor Score', score: latestLoan.factorScores.guarantorScore, max: 30 },
  ] : [];

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
          <h1 className="text-3xl font-bold text-foreground">Loan Dashboard</h1>
          <p className="text-muted-foreground mt-1">View your loan eligibility and application history</p>
        </motion.div>

        {/* Top Metrics Cards */}
        <motion.div
          className="grid gap-6 md:grid-cols-2 lg:grid-cols-4"
          variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.1 } } }}
        >
          <MetricCard
            title="Credit Score"
            value={creditScoreData[0]?.value || 300}
            icon={<TrendingUp className="h-4 w-4 text-primary" />}
          />
          <MetricCard
            title="Max Loan Eligibility"
            value={`₹${(maxLoanEligibility / 100000).toFixed(1)}L`}
            icon={<DollarSign className="h-4 w-4 text-primary" />}
          />
          <MetricCard
            title="DTI Ratio"
            value={latestLoan ? `${(latestLoan.dtiRatio * 100).toFixed(2)}%` : 'N/A'}
            icon={<Percent className="h-4 w-4 text-primary" />}
          />
          <MetricCard
            title="Active Loans"
            value={activeLoans}
            icon={<FileText className="h-4 w-4 text-primary" />}
          />
        </motion.div>

        {latestLoan && (
          <>
            {/* Credit Score Gauge and Factor Scores Bar Chart */}
            <motion.div
              className="grid gap-6 md:grid-cols-2"
              variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.55, ease: 'easeOut' } } }}
            >
              <CreditScoreGauge data={creditScoreData} />
              <FactorScoresBarChart data={factorScoresData} />
            </motion.div>

            {/* Factor Information Grid */}
            <motion.div
              variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.55, ease: 'easeOut', delay: 0.1 } } }}
            >
              <FactorInformationGrid data={factorGridData} />
            </motion.div>

            {/* Improvement Tips */}
            {(latestLoan.status === 'REJECTED' || latestLoan.status === 'UNDER_REVIEW') && 
             latestLoan.improvementTips && latestLoan.improvementTips.length > 0 && (
              <motion.div
                variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.55, ease: 'easeOut', delay: 0.2 } } }}
              >
                <ImprovementTips tips={latestLoan.improvementTips} />
              </motion.div>
            )}
          </>
        )}

        {/* Loan History Table */}
        <motion.div
          variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.55, ease: 'easeOut', delay: 0.3 } } }}
        >
          <LoanHistoryTable loans={loans} />
        </motion.div>
      </motion.div>
    </DashboardLayout>
  );
};

// Helper function to get credit score color
function getCreditScoreColor(eligibilityScore: number): string {
  const creditScore = Math.round((eligibilityScore / 100) * 600 + 300);
  if (creditScore >= 700) return '#22c55e'; // green
  if (creditScore >= 550) return '#eab308'; // yellow
  return '#ef4444'; // red
}

// Helper function to get progress bar color
function getProgressColor(score: number, max: number): string {
  const percentage = (score / max) * 100;
  if (percentage >= 70) return 'bg-green-500';
  if (percentage >= 50) return 'bg-yellow-500';
  return 'bg-red-500';
}

// Metric Card Component
interface MetricCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
}

const MetricCard = ({ title, value, icon }: MetricCardProps) => (
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

// Credit Score Gauge Component
interface CreditScoreGaugeProps {
  data: Array<{ name: string; value: number; fill: string }>;
}

const CreditScoreGauge = ({ data }: CreditScoreGaugeProps) => (
  <Card>
    <CardHeader>
      <CardTitle>Credit Score</CardTitle>
      <p className="text-sm text-muted-foreground">Range: 300-900</p>
    </CardHeader>
    <CardContent>
      <ResponsiveContainer width="100%" height={300}>
        <RadialBarChart
          cx="50%"
          cy="50%"
          innerRadius="60%"
          outerRadius="100%"
          barSize={30}
          data={data}
          startAngle={180}
          endAngle={0}
        >
          <RadialBar
            minAngle={15}
            background
            clockWise
            dataKey="value"
            cornerRadius={10}
          />
          <Legend
            iconSize={10}
            layout="vertical"
            verticalAlign="middle"
            align="right"
            content={({ payload }) => {
              if (payload && payload.length > 0) {
                const value = payload[0].payload?.value || 0;
                return (
                  <div className="text-center">
                    <p className="text-4xl font-bold">{value}</p>
                    <p className="text-sm text-muted-foreground">Credit Score</p>
                  </div>
                );
              }
              return null;
            }}
          />
        </RadialBarChart>
      </ResponsiveContainer>
    </CardContent>
  </Card>
);

// Factor Scores Bar Chart Component
interface FactorScoresBarChartProps {
  data: Array<{ name: string; score: number; max: number }>;
}

const FactorScoresBarChart = ({ data }: FactorScoresBarChartProps) => (
  <Card>
    <CardHeader>
      <CardTitle>Factor Scores</CardTitle>
      <p className="text-sm text-muted-foreground">Individual factor breakdown</p>
    </CardHeader>
    <CardContent>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data} layout="horizontal">
          <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
          <XAxis type="number" domain={[0, 'dataMax']} />
          <YAxis type="category" dataKey="name" width={100} tick={{ fontSize: 11 }} />
          <Tooltip
            content={({ active, payload }) => {
              if (active && payload && payload.length) {
                const data = payload[0].payload;
                return (
                  <div className="bg-card p-3 border rounded-lg shadow-lg">
                    <p className="text-sm font-semibold">{data.name}</p>
                    <p className="text-sm text-primary">
                      Score: {data.score} / {data.max}
                    </p>
                  </div>
                );
              }
              return null;
            }}
          />
          <Bar dataKey="score" fill="hsl(var(--primary))" radius={[0, 4, 4, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </CardContent>
  </Card>
);

// Factor Information Grid Component
interface FactorInformationGridProps {
  data: Array<{ name: string; score: number; max: number }>;
}

const FactorInformationGrid = ({ data }: FactorInformationGridProps) => (
  <Card>
    <CardHeader>
      <CardTitle>Factor Details</CardTitle>
      <p className="text-sm text-muted-foreground">Detailed breakdown of all scoring factors</p>
    </CardHeader>
    <CardContent>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {data.map((factor, index) => {
          const percentage = (factor.score / factor.max) * 100;
          return (
            <div key={index} className="p-4 border rounded-lg space-y-2">
              <div className="flex justify-between items-center">
                <p className="text-sm font-semibold">{factor.name}</p>
                <p className="text-sm text-muted-foreground">
                  {factor.score} / {factor.max}
                </p>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className={`h-2 rounded-full ${getProgressColor(factor.score, factor.max)}`}
                  style={{ width: `${percentage}%` }}
                />
              </div>
              <p className="text-xs text-muted-foreground">{percentage.toFixed(1)}%</p>
            </div>
          );
        })}
      </div>
    </CardContent>
  </Card>
);

// Improvement Tips Component
interface ImprovementTipsProps {
  tips: string[];
}

const ImprovementTips = ({ tips }: ImprovementTipsProps) => (
  <Alert>
    <AlertCircle className="h-4 w-4" />
    <AlertTitle>Improvement Tips</AlertTitle>
    <AlertDescription>
      <ul className="list-disc list-inside space-y-1 mt-2">
        {tips.map((tip, index) => (
          <li key={index}>{tip}</li>
        ))}
      </ul>
    </AlertDescription>
  </Alert>
);

// Loan History Table Component
interface LoanHistoryTableProps {
  loans: LoanResponse[];
}

const LoanHistoryTable = ({ loans }: LoanHistoryTableProps) => {
  const getStatusBadge = (status: string) => {
    const variants: Record<string, 'default' | 'secondary' | 'destructive' | 'outline'> = {
      APPROVED: 'default',
      REJECTED: 'destructive',
      UNDER_REVIEW: 'secondary',
      PENDING: 'outline',
    };
    return (
      <Badge variant={variants[status] || 'outline'}>
        {status.replace('_', ' ')}
      </Badge>
    );
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Loan History</CardTitle>
        <p className="text-sm text-muted-foreground">All your loan applications</p>
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
                  <TableHead>Amount</TableHead>
                  <TableHead>Purpose</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Interest Rate</TableHead>
                  <TableHead>EMI</TableHead>
                  <TableHead>Application Date</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loans.map((loan) => (
                  <TableRow key={loan.loanId}>
                    <TableCell className="font-medium">{loan.loanId}</TableCell>
                    <TableCell>₹{loan.loanAmount.toLocaleString()}</TableCell>
                    <TableCell>{loan.loanPurpose}</TableCell>
                    <TableCell>{getStatusBadge(loan.status)}</TableCell>
                    <TableCell>{loan.interestRate}%</TableCell>
                    <TableCell>₹{loan.emi.toLocaleString()}</TableCell>
                    <TableCell>{new Date(loan.applicationDate).toLocaleDateString()}</TableCell>
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

export default LoanDashboard;
