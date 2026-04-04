import { useState, useEffect } from "react";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, CreditCard, ArrowLeftRight, TrendingUp, Loader2 } from "lucide-react";
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { customerService } from "@/services/customerService";
import { accountService } from "@/services/accountService";
import { transactionService } from "@/services/transactionService";

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalCustomers: 0,
    activeAccounts: 0,
    totalTransactions: 0,
    totalVolume: 0,
    totalVolumeRaw: 0
  });
  const [weeklyData, setWeeklyData] = useState<any[]>([]);
  const [balanceDistribution, setBalanceDistribution] = useState<any[]>([]);
  const [monthlyTrend, setMonthlyTrend] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setIsLoading(true);
    try {
      console.log(' Loading dashboard data...');
      
      const customers = await customerService.getAllCustomers();
      const accounts = await accountService.getAllAccounts();
      const transactions = await transactionService.getAllTransactions();
      
      console.log(' Data loaded:');
      console.log('- Customers:', customers.length);
      console.log('- Accounts:', accounts.length);
      console.log('- Transactions:', transactions.length);
      
      const activeAccounts = accounts.filter(acc => 
        acc.status && acc.status.toUpperCase() === 'ACTIVE'
      );
      
      const totalVolume = transactions.reduce((sum, txn) => 
        sum + Number(txn.amount || 0), 0
      );
      
      console.log(' Total Transaction Volume:', totalVolume);

      const now = new Date();
      const dailyData = [];

      for (let i = 6; i >= 0; i--) {
        const date = new Date(now);
        date.setDate(date.getDate() - i);
        date.setHours(0, 0, 0, 0);
        
        const nextDay = new Date(date);
        nextDay.setDate(nextDay.getDate() + 1);
        
        const count = transactions.filter(txn => {
          if (txn.createdDate) {
            const txnDate = new Date(txn.createdDate);
            return txnDate >= date && txnDate < nextDay;
          }
          return false;
        }).length;
        
        const dayLabel = date.toLocaleDateString('en-US', { weekday: 'short' });
        const dateLabel = `${date.getDate()}/${date.getMonth() + 1}`;
        
        dailyData.push({
          day: dayLabel,
          date: dateLabel,
          transactions: count
        });
      }

      console.log(' Daily transaction data (last 7 days):', dailyData);

      const balanceData = [
        { name: 'Savings', value: 47 },
        { name: 'Current', value: 33 },
        { name: 'Fixed', value: 13 },
        { name: 'Other', value: 7 }
      ];

      const currentMonth = now.getMonth();
      const currentYear = now.getFullYear();
      
      const weekBuckets = [
        { label: 'Week 1', startDate: 1, endDate: 7, amount: 0 },
        { label: 'Week 2', startDate: 8, endDate: 14, amount: 0 },
        { label: 'Week 3', startDate: 15, endDate: 21, amount: 0 },
        { label: 'Week 4', startDate: 22, endDate: 31, amount: 0 }
      ];

      transactions.forEach(txn => {
        if (txn.createdDate) {
          const txnDate = new Date(txn.createdDate);
          
          if (txnDate.getMonth() === currentMonth && txnDate.getFullYear() === currentYear) {
            const dayOfMonth = txnDate.getDate();
            
            for (const bucket of weekBuckets) {
              if (dayOfMonth >= bucket.startDate && dayOfMonth <= bucket.endDate) {
                bucket.amount += Number(txn.amount || 0);
                console.log(`Transaction on Oct ${dayOfMonth} -> ${bucket.label}: ₹${txn.amount}`);
                break;
              }
            }
          }
        }
      });

      const trendData = weekBuckets.map(bucket => ({
        date: bucket.label,
        amount: bucket.amount
      }));

      console.log(' Monthly trend data (Calendar weeks):', trendData);

      setStats({
        totalCustomers: customers.length,
        activeAccounts: activeAccounts.length,
        totalTransactions: transactions.length,
        totalVolume: totalVolume / 1000000,
        totalVolumeRaw: totalVolume
      });
      
      setWeeklyData(dailyData);
      setBalanceDistribution(balanceData);
      setMonthlyTrend(trendData);

    } catch (error) {
      console.error(' Error loading dashboard data:', error);
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

  const COLORS = ["hsl(var(--primary))", "hsl(var(--secondary))", "hsl(var(--accent))", "hsl(var(--muted))"];

  const formatVolume = (raw: number) => {
    if (raw === 0) return '₹0';
    if (raw >= 10000000) return `₹${(raw / 10000000).toFixed(1)}Cr`;
    if (raw >= 100000) return `₹${(raw / 100000).toFixed(1)}L`;
    if (raw >= 1000) return `₹${(raw / 1000).toFixed(1)}K`;
    return `₹${raw.toFixed(0)}`;
  };

  return (
    <DashboardLayout>
      <div className="space-y-8">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Dashboard Overview</h1>
          <p className="text-muted-foreground mt-1">Welcome back! Here's what's happening today.</p>
        </div>

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Customers</CardTitle>
              <Users className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalCustomers.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">
                <span className="text-secondary">+12%</span> from last month
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Accounts</CardTitle>
              <CreditCard className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.activeAccounts.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">
                <span className="text-secondary">+8%</span> from last month
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Transactions</CardTitle>
              <ArrowLeftRight className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.totalTransactions.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">
                <span className="text-secondary">+23%</span> from last month
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Volume</CardTitle>
              <TrendingUp className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatVolume(stats.totalVolumeRaw)}</div>
              <p className="text-xs text-muted-foreground mt-1">
                <span className="text-secondary">+15%</span> from last month
              </p>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Transactions Overview</CardTitle>
              <p className="text-sm text-muted-foreground">Last 7 days activity</p>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={weeklyData}>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                  <XAxis 
                    dataKey="date" 
                    tick={{ fontSize: 12 }}
                    label={{ value: 'Date (DD/MM)', position: 'insideBottom', offset: -5, fontSize: 12 }}
                  />
                  <YAxis 
                    allowDecimals={false}
                    domain={[0, 'auto']}
                    tickCount={6}
                  />
                  <Tooltip 
                    content={({ active, payload }) => {
                      if (active && payload && payload.length) {
                        return (
                          <div className="bg-card p-3 border rounded-lg shadow-lg">
                            <p className="text-sm font-semibold text-foreground">
                              {payload[0].payload.day}, {payload[0].payload.date}
                            </p>
                            <p className="text-sm text-primary font-medium">
                              Transactions: {payload[0].value}
                            </p>
                          </div>
                        );
                      }
                      return null;
                    }}
                  />
                  <Bar 
                    dataKey="transactions" 
                    fill="hsl(var(--primary))" 
                    radius={[8, 8, 0, 0]} 
                  />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Account Balance Distribution</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={balanceDistribution}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={100}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {balanceDistribution.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Transaction Trend (October 2025)</CardTitle>
            <p className="text-sm text-muted-foreground">Weekly transaction volume by calendar week</p>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={monthlyTrend}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
                <XAxis 
                  dataKey="date" 
                  tick={{ fontSize: 12 }}
                />
                <YAxis 
                  tickFormatter={(value) => `₹${(value / 1000).toFixed(0)}K`}
                />
                <Tooltip 
                  formatter={(value) => [`₹${Number(value).toLocaleString()}`, 'Amount']}
                  labelFormatter={(label) => `${label} (Oct 2025)`}
                />
                <Line 
                  type="monotone" 
                  dataKey="amount" 
                  stroke="hsl(var(--secondary))" 
                  strokeWidth={3}
                  dot={{ fill: 'hsl(var(--secondary))', r: 5 }}
                  activeDot={{ r: 8 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default Dashboard;
