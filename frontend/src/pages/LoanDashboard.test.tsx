import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LoanDashboard from './LoanDashboard';
import { loanService, LoanResponse } from '@/services/loanService';
import { customerService } from '@/services/customerService';
import { tokenUtils } from '@/services/authService';

// Mock the services
vi.mock('@/services/loanService');
vi.mock('@/services/customerService');
vi.mock('@/services/authService');

// Mock DashboardLayout
vi.mock('@/components/DashboardLayout', () => ({
  default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock recharts to avoid rendering issues in tests
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  BarChart: ({ children }: any) => <div data-testid="bar-chart">{children}</div>,
  Bar: () => <div />,
  XAxis: () => <div />,
  YAxis: () => <div />,
  CartesianGrid: () => <div />,
  Tooltip: () => <div />,
  RadialBarChart: ({ children }: any) => <div data-testid="radial-chart">{children}</div>,
  RadialBar: () => <div />,
  Legend: () => <div />,
}));

const mockLoanData: LoanResponse[] = [
  {
    loanId: 'LOAN-12345678',
    accountNumber: 'ACC123',
    loanAmount: 500000,
    loanPurpose: 'HOME',
    loanTenure: 240,
    eligibilityScore: 85.5,
    dtiRatio: 0.35,
    status: 'APPROVED',
    interestRate: 8.5,
    emi: 4500,
    rejectionReason: '',
    improvementTips: [],
    applicationDate: '2024-01-15T10:00:00',
    lastUpdated: '2024-01-15T10:00:00',
    factorScores: {
      incomeScore: 100,
      employmentScore: 70,
      dtiScore: 80,
      repaymentHistoryScore: 100,
      ageScore: 60,
      existingLoansScore: 50,
      collateralScore: 70,
      bankingRelationshipScore: 40,
      residenceScore: 30,
      loanPurposeScore: 40,
      guarantorScore: 30,
    },
  },
  {
    loanId: 'LOAN-87654321',
    accountNumber: 'ACC123',
    loanAmount: 200000,
    loanPurpose: 'EDUCATION',
    loanTenure: 120,
    eligibilityScore: 65.0,
    dtiRatio: 0.45,
    status: 'UNDER_REVIEW',
    interestRate: 10.0,
    emi: 2200,
    rejectionReason: '',
    improvementTips: [
      'Increase your monthly income to improve eligibility.',
      'Reduce existing EMI obligations to lower your debt-to-income ratio.',
    ],
    applicationDate: '2024-01-10T10:00:00',
    lastUpdated: '2024-01-10T10:00:00',
    factorScores: {
      incomeScore: 60,
      employmentScore: 70,
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

const mockRejectedLoan: LoanResponse = {
  loanId: 'LOAN-99999999',
  accountNumber: 'ACC123',
  loanAmount: 1000000,
  loanPurpose: 'PERSONAL',
  loanTenure: 60,
  eligibilityScore: 45.0,
  dtiRatio: 0.55,
  status: 'REJECTED',
  interestRate: 0,
  emi: 0,
  rejectionReason: 'Low eligibility score and high DTI ratio',
  improvementTips: [
    'Increase your monthly income to improve eligibility.',
    'Reduce existing EMI obligations to lower your debt-to-income ratio.',
    'Providing collateral can significantly improve your loan eligibility.',
  ],
  applicationDate: '2024-01-05T10:00:00',
  lastUpdated: '2024-01-05T10:00:00',
  factorScores: {
    incomeScore: 40,
    employmentScore: 50,
    dtiScore: 0,
    repaymentHistoryScore: 100,
    ageScore: 30,
    existingLoansScore: 0,
    collateralScore: 0,
    bankingRelationshipScore: 10,
    residenceScore: 10,
    loanPurposeScore: 15,
    guarantorScore: 0,
  },
};

describe('LoanDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock tokenUtils
    vi.mocked(tokenUtils.getUser).mockReturnValue({
      id: '1',
      fullName: 'Test User',
      email: 'test@example.com',
      role: 'USER',
      active: true,
    });

    // Mock customerService
    vi.mocked(customerService.getCustomerByAadhar).mockResolvedValue({
      customerId: 'CUST123',
      name: 'Test User',
      phoneNumber: '1234567890',
      email: 'test@example.com',
      address: 'Test Address',
      aadharNumber: 'ACC123',
      dob: '1990-01-01',
      status: 'ACTIVE',
    });
  });

  it('should render loading state initially', () => {
    vi.mocked(loanService.getLoansByAccount).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    expect(screen.getByRole('status', { hidden: true })).toBeInTheDocument();
  });

  it('should render loan dashboard with loan data', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue(mockLoanData);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Loan Dashboard')).toBeInTheDocument();
    });

    // Check top metrics cards
    expect(screen.getByText('Credit Score')).toBeInTheDocument();
    expect(screen.getByText('Max Loan Eligibility')).toBeInTheDocument();
    expect(screen.getByText('DTI Ratio')).toBeInTheDocument();
    expect(screen.getByText('Active Loans')).toBeInTheDocument();

    // Check loan history table
    expect(screen.getByText('Loan History')).toBeInTheDocument();
    expect(screen.getByText('LOAN-12345678')).toBeInTheDocument();
    expect(screen.getByText('LOAN-87654321')).toBeInTheDocument();
  });

  it('should render charts correctly', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue(mockLoanData);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Credit Score')).toBeInTheDocument();
    });

    // Check for chart components
    expect(screen.getByTestId('radial-chart')).toBeInTheDocument();
    expect(screen.getByTestId('bar-chart')).toBeInTheDocument();
  });

  it('should show improvement tips only for rejected or under review loans', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue([mockLoanData[1]]);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Improvement Tips')).toBeInTheDocument();
    });

    expect(screen.getByText('Increase your monthly income to improve eligibility.')).toBeInTheDocument();
    expect(screen.getByText('Reduce existing EMI obligations to lower your debt-to-income ratio.')).toBeInTheDocument();
  });

  it('should not show improvement tips for approved loans', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue([mockLoanData[0]]);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Loan Dashboard')).toBeInTheDocument();
    });

    expect(screen.queryByText('Improvement Tips')).not.toBeInTheDocument();
  });

  it('should display all loans in history table', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue(mockLoanData);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Loan History')).toBeInTheDocument();
    });

    // Check table headers
    expect(screen.getByText('Loan ID')).toBeInTheDocument();
    expect(screen.getByText('Amount')).toBeInTheDocument();
    expect(screen.getByText('Purpose')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Interest Rate')).toBeInTheDocument();
    expect(screen.getByText('EMI')).toBeInTheDocument();
    expect(screen.getByText('Application Date')).toBeInTheDocument();

    // Check loan data
    expect(screen.getByText('HOME')).toBeInTheDocument();
    expect(screen.getByText('EDUCATION')).toBeInTheDocument();
    expect(screen.getByText('8.5%')).toBeInTheDocument();
    expect(screen.getByText('10%')).toBeInTheDocument();
  });

  it('should show improvement tips for rejected loans', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue([mockRejectedLoan]);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Improvement Tips')).toBeInTheDocument();
    });

    expect(screen.getByText('Providing collateral can significantly improve your loan eligibility.')).toBeInTheDocument();
  });

  it('should handle error state', async () => {
    vi.mocked(loanService.getLoansByAccount).mockRejectedValue({
      response: { data: { message: 'Failed to fetch loans' } },
    });

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Error')).toBeInTheDocument();
    });

    expect(screen.getByText('Failed to fetch loans')).toBeInTheDocument();
  });

  it('should handle no loans scenario', async () => {
    vi.mocked(loanService.getLoansByAccount).mockResolvedValue([]);

    render(
      <BrowserRouter>
        <LoanDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Loan Dashboard')).toBeInTheDocument();
    });

    expect(screen.getByText('No loan applications found')).toBeInTheDocument();
  });
});
