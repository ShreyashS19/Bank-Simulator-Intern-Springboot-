import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import DashboardLayout from '@/components/DashboardLayout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Loader2, TrendingUp, DollarSign, Percent, FileText, AlertCircle, Sparkles, Activity } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, RadialBarChart, RadialBar, Legend, PieChart, Pie, Cell, AreaChart, Area } from 'recharts';
import { loanService, LoanResponse } from '@/services/loanService';
import { customerService } from '@/services/customerService';
import { accountService } from '@/services/accountService';
import { tokenUtils } from '@/services/authService';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'];

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

      const customer = await customerService.getCustomerByAadhar(user.email);
      const allAccounts = await accountService.getAllAccounts();
      const userAccount = allAccounts.find(acc => acc.aadharNumber === customer.aadharNumber);
      
      if (!userAccount) {
        setError('No account found for this user');
        return;
      }
      
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
        <Alert variant="destructive" className="glass-card">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </DashboardLayout>
    );
  }

  const latestLoan = loans.length > 0 ? loans[0] : null;
  const activeLoans = loans.filter(loan => loan.status === 'APPROVED').length;

  const maxLoanEligibility = latestLoan 
    ? Math.round((latestLoan.eligibilityScore / 100) * 10000000) 
    : 0;

  const creditScoreData = latestLoan ? [{
    name: 'Credit Score',
    value: latestLoan.factorScores ? 
      Math.round((latestLoan.eligibilityScore / 100) * 600 + 300) : 
      300,
    fill: getCreditScoreColor(latestLoan.eligibilityScore)
  }] : [];

  const factorScoresData = latestLoan?.factorScores ? [
    { name: 'Income', score: latestLoan.factorScores.incomeScore, max: 120 },
    { name: 'Emp', score: latestLoan.factorScores.employmentScore, max: 80 },
    { name: 'DTI', score: latestLoan.factorScores.dtiScore, max: 100 },
    { name: 'History', score: latestLoan.factorScores.repaymentHistoryScore, max: 100 },
    { name: 'Collateral', score: latestLoan.factorScores.collateralScore, max: 70 },
  ] : [];

  // Mock distribution data based on actual loans if exist
  const statusCounts = loans.reduce((acc, loan) => {
    acc[loan.status] = (acc[loan.status] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  const pieData = [
    { name: 'Approved', value: statusCounts['APPROVED'] || 0 },
    { name: 'Pending', value: statusCounts['UNDER_REVIEW'] || statusCounts['PENDING'] || 0 },
    { name: 'Rejected', value: statusCounts['REJECTED'] || 0 },
  ].filter(d => d.value > 0);
  
  if (pieData.length === 0) pieData.push({ name: 'No Data', value: 1 });

  return (
    <DashboardLayout>
      <motion.div
        className="space-y-8 pb-10"
        initial="hidden"
        animate="visible"
        variants={{
          hidden: {},
          visible: { transition: { staggerChildren: 0.1 } },
        }}
      >
        <motion.div
          variants={{ hidden: { opacity: 0, y: -20 }, visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } } }}
          className="flex flex-col md:flex-row justify-between md:items-end p-6 rounded-3xl bg-gradient-to-br from-primary/10 to-transparent border border-primary/20 backdrop-blur-xl mb-4 shadow-lg"
        >
          <div>
            <h1 className="text-4xl font-extrabold tracking-tight text-foreground bg-clip-text text-transparent bg-gradient-to-r from-primary to-accent">
              Loan Hub
            </h1>
            <p className="text-muted-foreground mt-2 text-lg">Manage your lending profile & track applications</p>
          </div>
          {latestLoan && (
            <div className="mt-4 md:mt-0 flex items-center gap-3 bg-secondary/15 px-4 py-2 rounded-full border border-secondary/30">
              <Sparkles className="w-5 h-5 text-secondary animate-pulse" />
              <span className="text-sm font-medium text-foreground">
                AI Suggestion: You have a high chance for a Vehicle Loan
              </span>
            </div>
          )}
        </motion.div>

        {/* Top Metrics Cards */}
        <motion.div
          className="grid gap-6 md:grid-cols-2 lg:grid-cols-4"
          variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.1 } } }}
        >
          <MetricCard
            title="Credit Score"
            value={creditScoreData[0]?.value || 'N/A'}
            subtitle="Equifax Data"
            icon={<Activity className="h-5 w-5 text-blue-500" />}
            trend={latestLoan ? "+12 pts" : ""}
          />
          <MetricCard
            title="Max Eligibility"
            value={`₹${(maxLoanEligibility / 100000).toFixed(1)}L`}
            subtitle="Pre-approved"
            icon={<DollarSign className="h-5 w-5 text-emerald-500" />}
            trend={latestLoan ? "Unlocked" : ""}
          />
          <MetricCard
            title="DTI Ratio"
            value={latestLoan ? `${(latestLoan.dtiRatio * 100).toFixed(1)}%` : 'N/A'}
            subtitle="Debt to Income"
            icon={<Percent className="h-5 w-5 text-amber-500" />}
            trend={latestLoan && latestLoan.dtiRatio < 0.4 ? "Healthy" : "Needs work"}
          />
          <MetricCard
            title="Active Loans"
            value={activeLoans}
            subtitle="Total Managed"
            icon={<FileText className="h-5 w-5 text-purple-500" />}
          />
        </motion.div>

        {latestLoan && (
          <>
            <motion.div
              className="grid gap-6 lg:grid-cols-3"
              variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: 'easeOut' } } }}
            >
              <div className="lg:col-span-1">
                <CreditScoreGauge data={creditScoreData} />
              </div>
              <div className="lg:col-span-2">
                <FactorScoresAreaChart data={factorScoresData} />
              </div>
            </motion.div>

            {(latestLoan.status === 'REJECTED' || latestLoan.status === 'UNDER_REVIEW') && 
             latestLoan.improvementTips && latestLoan.improvementTips.length > 0 && (
              <motion.div
                variants={{ hidden: { opacity: 0, scale: 0.95 }, visible: { opacity: 1, scale: 1, transition: { duration: 0.5 } } }}
              >
                <div className="glass-card p-6 rounded-2xl relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-64 h-64 bg-primary/10 rounded-full blur-3xl -mr-20 -mt-20"></div>
                  <div className="flex items-start gap-4 relative z-10">
                    <div className="bg-primary/20 p-3 rounded-xl lg:mt-1 text-primary">
                      <Sparkles className="w-6 h-6" />
                    </div>
                    <div>
                      <h3 className="text-xl font-bold mb-2">AI-Powered Insights to Improve</h3>
                      <ul className="space-y-3 mt-4">
                        {latestLoan.improvementTips.map((tip, index) => (
                          <li key={index} className="flex items-start gap-3">
                            <div className="w-1.5 h-1.5 rounded-full bg-primary mt-2"></div>
                            <span className="text-muted-foreground">{tip}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                </div>
              </motion.div>
            )}
            
            <motion.div
              className="grid gap-6 md:grid-cols-3"
              variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0, transition: { duration: 0.6 } } }}
            >
               <Card className="glass-card col-span-1 md:col-span-1">
                <CardHeader>
                  <CardTitle className="text-lg">Loan Portfolio</CardTitle>
                </CardHeader>
                <CardContent className="flex justify-center items-center">
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        {pieData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip 
                        contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.1)' }}
                      />
                      <Legend verticalAlign="bottom" height={36}/>
                    </PieChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              <div className="col-span-1 md:col-span-2">
                <LoanHistoryTable loans={loans} />
              </div>
            </motion.div>
          </>
        )}
        {!latestLoan && (
           <motion.div variants={{ hidden: { opacity: 0 }, visible: { opacity: 1 } }}>
             <LoanHistoryTable loans={loans} />
           </motion.div>
        )}
      </motion.div>
    </DashboardLayout>
  );
};

function getCreditScoreColor(eligibilityScore: number): string {
  const creditScore = Math.round((eligibilityScore / 100) * 600 + 300);
  if (creditScore >= 700) return '#10b981'; // green
  if (creditScore >= 550) return '#f59e0b'; // yellow
  return '#ef4444'; // red
}

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle: string;
  icon: React.ReactNode;
  trend?: string;
}

const MetricCard = ({ title, value, subtitle, icon, trend }: MetricCardProps) => (
  <motion.div 
    whileHover={{ y: -5, scale: 1.02 }}
    transition={{ type: "spring", stiffness: 300 }}
  >
    <Card className="glass-card border-none overflow-hidden relative group">
      <div className="absolute inset-0 bg-gradient-to-br from-white/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
      <CardContent className="p-6 relative z-10">
        <div className="flex justify-between items-start mb-4">
          <div className="p-2.5 bg-primary/10 rounded-2xl">
            {icon}
          </div>
          {trend && (
             <Badge variant="outline" className="bg-primary/5 text-xs text-primary border-primary/20">
               {trend}
             </Badge>
          )}
        </div>
        <div>
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <h2 className="text-3xl font-bold mt-1 text-foreground">{value}</h2>
          <p className="text-xs text-muted-foreground mt-1">{subtitle}</p>
        </div>
      </CardContent>
    </Card>
  </motion.div>
);

const CreditScoreGauge = ({ data }: { data: any }) => (
  <Card className="glass-card h-full">
    <CardHeader>
      <CardTitle>Credit Health</CardTitle>
    </CardHeader>
    <CardContent className="flex justify-center items-center pb-2">
      <ResponsiveContainer width="100%" height={260}>
        <RadialBarChart
          cx="50%"
          cy="50%"
          innerRadius="70%"
          outerRadius="100%"
          barSize={20}
          data={data}
          startAngle={180}
          endAngle={0}
        >
          <RadialBar
            background={{ fill: 'hsl(var(--muted))' }}
            dataKey="value"
            cornerRadius={10}
          />
          <text
            x="50%"
            y="50%"
            textAnchor="middle"
            dominantBaseline="middle"
            className="fill-foreground text-4xl font-extrabold"
          >
            {data[0]?.value || 0}
          </text>
          <text
            x="50%"
            y="65%"
            textAnchor="middle"
            dominantBaseline="middle"
            className="fill-muted-foreground text-sm font-medium"
            style={{ fill: 'hsl(var(--muted-foreground))' }}
          >
            Score Range: 300-900
          </text>
        </RadialBarChart>
      </ResponsiveContainer>
    </CardContent>
  </Card>
);

const FactorScoresAreaChart = ({ data }: { data: any }) => (
  <Card className="glass-card h-full">
    <CardHeader>
      <CardTitle>Approval Factors (AI Analyzed)</CardTitle>
    </CardHeader>
    <CardContent>
      <ResponsiveContainer width="100%" height={260}>
        <AreaChart data={data} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="colorScore" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.3}/>
              <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0}/>
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.2} />
          <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }} dy={10} />
          <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }} />
          <Tooltip 
             contentStyle={{ backgroundColor: 'hsl(var(--card))', borderRadius: '12px', border: '1px solid hsl(var(--border))', boxShadow: '0 8px 30px rgba(0,0,0,0.12)' }}
          />
          <Area type="monotone" dataKey="score" stroke="hsl(var(--primary))" strokeWidth={3} fillOpacity={1} fill="url(#colorScore)" />
        </AreaChart>
      </ResponsiveContainer>
    </CardContent>
  </Card>
);

const LoanHistoryTable = ({ loans }: { loans: LoanResponse[] }) => {
  const getStatusBadge = (status: string) => {
    const variants: Record<string, string> = {
      APPROVED: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
      REJECTED: 'bg-red-500/15 text-red-600 dark:text-red-400 border-red-500/20',
      UNDER_REVIEW: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/20',
      PENDING: 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border-blue-500/20',
    };
    return (
      <Badge variant="outline" className={`rounded-full px-3 py-1 ${variants[status] || ''}`}>
        {status.replace('_', ' ')}
      </Badge>
    );
  };

  return (
    <Card className="glass-card h-full">
      <CardHeader>
        <CardTitle>Recent Applications</CardTitle>
      </CardHeader>
      <CardContent>
        {loans.length === 0 ? (
          <div className="text-center py-12">
            <div className="bg-primary/10 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <FileText className="w-8 h-8 text-primary" />
            </div>
            <h3 className="text-lg font-medium text-foreground">No applications found</h3>
            <p className="text-sm text-muted-foreground mt-1">Ready to explore loan options?</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow className="border-border/40 hover:bg-transparent">
                  <TableHead className="font-medium text-muted-foreground">Amount</TableHead>
                  <TableHead className="font-medium text-muted-foreground">Purpose</TableHead>
                  <TableHead className="font-medium text-muted-foreground">Status</TableHead>
                  <TableHead className="font-medium text-muted-foreground">EMI</TableHead>
                  <TableHead className="font-medium text-muted-foreground">Date</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loans.slice(0, 5).map((loan) => (
                  <TableRow key={loan.loanId} className="border-border/40 transition-colors hover:bg-muted/30">
                    <TableCell className="font-semibold text-foreground">₹{loan.loanAmount.toLocaleString()}</TableCell>
                    <TableCell className="text-muted-foreground capitalize">{loan.loanPurpose.toLowerCase()}</TableCell>
                    <TableCell>{getStatusBadge(loan.status)}</TableCell>
                    <TableCell className="font-medium">₹{loan.emi.toLocaleString()}</TableCell>
                    <TableCell className="text-muted-foreground text-sm">{new Date(loan.applicationDate).toLocaleDateString()}</TableCell>
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
