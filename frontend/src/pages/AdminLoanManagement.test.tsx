import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import AdminLoanManagement from './AdminLoanManagement';
import { loanService } from '@/services/loanService';

// Mock the services
vi.mock('@/services/loanService', () => ({
  loanService: {
    getAllLoans: vi.fn(),
    getLoanStatistics: vi.fn(),
    updateLoanStatus: vi.fn(),
  },
}));

// Mock framer-motion to avoid animation issues in tests
vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
  },
}));

// Mock DashboardLayout
vi.mock('@/components/DashboardLayout', () => ({
  default: ({ children }: any) => <div data-testid="dashboard-layout">{children}</div>,
}));

// Mock Recharts components
vi.mock('recharts', () => ({
  PieChart: ({ children }: any) => <div data-testid="pie-chart">{children}</div>,
  Pie: () => <div data-testid="pie" />,
  Cell: () => <div data-testid="cell" />,
  LineChart: ({ children }: any) => <div data-testid="line-chart">{children}</div>,
  Line: () => <div data-testid="line" />,
  XAxis: () => <div data-testid="x-axis" />,
  YAxis: () => <div data-testid="y-axis" />,
  CartesianGrid: () => <div data-testid="cartesian-grid" />,
  Tooltip: () => <div data-testid="tooltip" />,
  Legend: () => <div data-testid="legend" />,
  ResponsiveContainer: ({ children }: any) => <div data-testid="responsive-container">{children}</div>,
}));

// Mock sonner toast
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockLoans = [
  {
    loanId: 'LOAN-12345678',
    accountNumber: 'ACC001',
    loanAmount: 500000,
    loanPurpose: 'HOME',
    loanTenure: 240,
    eligibilityScore: 85.5,
    dtiRatio: 0.25,
    status: 'APPROVED',
    interestRate: 8.5,
    emi: 4500,
    rejectionReason: '',
    improvementTips: [],
    applicationDate: '2024-01-15T10:00:00',
    lastUpdated: '2024-01-15T10:00:00',
    factorScores: {
      incomeScore: 120,
      employmentScore: 80,
      dtiScore: 100,
      repaymentHistoryScore: 100,
      ageScore: 60,
      existingLoansScore: 60,
      collateralScore: 70,
      bankingRelationshipScore: 50,
      residenceScore: 40,
      loanPurposeScore: 40,
      guarantorScore: 30,
    },
  },
  {
    loanId: 'LOAN-87654321',
    accountNumber: 'ACC002',
    loanAmount: 200000,
    loanPurpose: 'EDUCATION',
    loanTenure: 120,
    eligibilityScore: 62.3,
    dtiRatio: 0.45,
    status: 'REJECTED',
    interestRate: 0,
    emi: 0,
    rejectionReason: 'Low eligibility score',
    improvementTips: ['Improve income', 'Reduce DTI'],
    applicationDate: '2024-01-16T11:00:00',
    lastUpdated: '2024-01-16T11:00:00',
    factorScores: {
      incomeScore: 60,
      employmentScore: 50,
      dtiScore: 40,
      repaymentHistoryScore: 100,
      ageScore: 50,
      existingLoansScore: 30,
      collateralScore: 0,
      bankingRelationshipScore: 25,
      residenceScore: 20,
      loanPurposeScore: 40,
      guarantorScore: 0,
    },
  },
];

const mockStatistics = {
  totalApplications: 100,
  approvedCount: 45,
  rejectedCount: 30,
  underReviewCount: 20,
  pendingCount: 5,
  totalApprovedAmount: 50000000,
  averageEligibilityScore: 72.5,
};

describe('AdminLoanManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render with loan data', async () => {
    vi.mocked(loanService.getAllLoans).mockResolvedValue(mockLoans);
    vi.mocked(loanService.getLoanStatistics).mockResolvedValue(mockStatistics);

    render(
      <BrowserRouter>
        <AdminLoanManagement />
      </BrowserRouter>
    );

    // Wait for loading to complete
    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check if title is rendered
    expect(screen.getByText('Loan Management')).toBeInTheDocument();
    expect(screen.getByText('Manage and review all loan applications')).toBeInTheDocument();

    // Check if loan data is displayed
    expect(screen.getByText('LOAN-12345678')).toBeInTheDocument();
    expect(screen.getByText('LOAN-87654321')).toBeInTheDocument();
  });

  it('should display statistics cards with correct values', async () => {
    vi.mocked(loanService.getAllLoans).mockResolvedValue(mockLoans);
    vi.mocked(loanService.getLoanStatistics).mockResolvedValue(mockStatistics);

    render(
      <BrowserRouter>
        <AdminLoanManagement />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check statistics cards
    expect(screen.getByText('Total Applications')).toBeInTheDocument();
    expect(screen.getByText('100')).toBeInTheDocument();
    
    expect(screen.getByText('Approved')).toBeInTheDocument();
    expect(screen.getByText('45')).toBeInTheDocument();
    
    expect(screen.getByText('Rejected')).toBeInTheDocument();
    expect(screen.getByText('30')).toBeInTheDocument();
    
    expect(screen.getByText('Under Review')).toBeInTheDocument();
    expect(screen.getByText('20')).toBeInTheDocument();
    
    expect(screen.getByText('Total Approved Amount')).toBeInTheDocument();
    expect(screen.getByText('₹500.0L')).toBeInTheDocument();
    
    expect(screen.getByText('Avg Eligibility Score')).toBeInTheDocument();
    expect(screen.getByText('72.50')).toBeInTheDocument();
  });

  it('should render charts correctly', async () => {
    vi.mocked(loanService.getAllLoans).mockResolvedValue(mockLoans);
    vi.mocked(loanService.getLoanStatistics).mockResolvedValue(mockStatistics);

    render(
      <BrowserRouter>
        <AdminLoanManagement />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    // Check if charts are rendered
    expect(screen.getByText('Loan Purpose Distribution')).toBeInTheDocument();
    expect(screen.getByText('Loan Status by Month')).toBeInTheDocument();
    
    // Check if chart components are present
    const pieCharts = screen.getAllByTestId('pie-chart');
    expect(pieCharts.length).toBeGreaterThan(0);
    
    const lineCharts = screen.getAllByTestId('line-chart');
    expect(lineCharts.length).toBeGreaterThan(0);
  });

  it('should display error message when data loading fails', async () => {
    const errorMessage = 'Failed to load loan data';
    vi.mocked(loanService.getAllLoans).mockRejectedValue(new Error(errorMessage));
    vi.mocked(loanService.getLoanStatistics).mockRejectedValue(new Error(errorMessage));

    render(
      <BrowserRouter>
        <AdminLoanManagement />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Error')).toBeInTheDocument();
    });
  });

  it('should display loading state initially', () => {
    vi.mocked(loanService.getAllLoans).mockImplementation(() => new Promise(() => {}));
    vi.mocked(loanService.getLoanStatistics).mockImplementation(() => new Promise(() => {}));

    render(
      <BrowserRouter>
        <AdminLoanManagement />
      </BrowserRouter>
    );

    // Check for loading spinner (Loader2 component)
    const loader = screen.getByRole('generic', { hidden: true });
    expect(loader).toBeInTheDocument();
  });

  it('should display "No loan applications found" when loans array is empty', async () => {
    vi.mocked(loanService.getAllLoans).mockResolvedValue([]);
    vi.mocked(loanService.getLoanStatistics).mockResolvedValue({
      totalApplications: 0,
      approvedCount: 0,
      rejectedCount: 0,
      underReviewCount: 0,
      pendingCount: 0,
      totalApprovedAmount: 0,
      averageEligibilityScore: 0,
    });

    render(
      <BrowserRouter>
        <AdminLoanManagement />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('No loan applications found')).toBeInTheDocument();
    });
  });
});
