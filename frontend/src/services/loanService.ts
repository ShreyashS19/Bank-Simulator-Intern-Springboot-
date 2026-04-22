import axios from '../utils/axiosConfig';

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface LoanApplicationRequest {
  loanAmount: number;
  loanPurpose: string;
  loanTenure: number;
  monthlyIncome: number;
  employmentType: string;
  existingEmi: number;
  creditScore: number;
  age: number;
  existingLoans: number;
  hasCollateral: boolean;
  residenceYears: number;
  hasGuarantor: boolean;
  repaymentHistory: string;
}

export interface FactorScores {
  incomeScore: number;
  employmentScore: number;
  dtiScore: number;
  repaymentHistoryScore: number;
  ageScore: number;
  existingLoansScore: number;
  collateralScore: number;
  bankingRelationshipScore: number;
  residenceScore: number;
  loanPurposeScore: number;
  guarantorScore: number;
}

export interface LoanResponse {
  loanId: string;
  accountNumber: string;
  loanAmount: number;
  loanPurpose: string;
  loanTenure: number;
  eligibilityScore: number;
  dtiRatio: number;
  status: string;
  referenceNumber?: string;
  eligibilityStatus?: 'ELIGIBLE' | 'NOT_ELIGIBLE';
  interestRate: number;
  emi: number;
  rejectionReason: string;
  improvementTips: string[];
  applicationDate: string;
  lastUpdated: string;
  factorScores: FactorScores;
}

export interface LoanEligibilityResultDto {
  referenceNumber: string;
  eligibilityStatus: 'ELIGIBLE' | 'NOT_ELIGIBLE';
  customerName: string;
  customerEmail: string;
  loanAmount: number;
  loanPurpose: string;
  loanTenure: number;
  eligibilityScore: number;
  eligibilityMessage: string;
  requiredDocuments: string[];
  specialNotes: string[];
  improvementTips?: string[];
  generatedAt: string;
  pdfDownloadPath: string;
}

export interface LoanStatistics {
  totalApplications: number;
  approvedCount: number;
  rejectedCount: number;
  underReviewCount: number;
  pendingCount: number;
  totalApprovedAmount: number;
  averageEligibilityScore: number;
}

export interface UpdateLoanStatusRequest {
  status: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const loanService = {
  applyForLoan: async (request: LoanApplicationRequest): Promise<LoanEligibilityResultDto> => {
    try {
      const response = await axios.post<ApiResponse<LoanEligibilityResultDto>>(
        `${API_BASE_URL}/loan/apply`,
        request,
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        }
      );
      return response.data.data;
    } catch (error: any) {
      console.error('Error applying for loan:', error.response?.data || error);
      throw error;
    }
  },

  getLoansByAccount: async (accountNumber: string): Promise<LoanResponse[]> => {
    try {
      const response = await axios.get<ApiResponse<LoanResponse[]>>(
        `${API_BASE_URL}/loan/account/${accountNumber}`
      );
      return response.data.data;
    } catch (error: any) {
      console.error('Error fetching loans:', error);
      throw error;
    }
  },

  getLoanById: async (loanId: string): Promise<LoanResponse> => {
    try {
      const response = await axios.get<ApiResponse<LoanResponse>>(
        `${API_BASE_URL}/loan/${loanId}`
      );
      return response.data.data;
    } catch (error: any) {
      console.error('Error fetching loan:', error);
      throw error;
    }
  },

  getAllLoans: async (): Promise<LoanResponse[]> => {
    try {
      const response = await axios.get<ApiResponse<LoanResponse[]>>(
        `${API_BASE_URL}/loan/all`
      );
      return response.data.data;
    } catch (error: any) {
      console.error('Error fetching all loans:', error);
      throw error;
    }
  },

  updateLoanStatus: async (loanId: string, request: UpdateLoanStatusRequest): Promise<void> => {
    try {
      await axios.put<ApiResponse<void>>(
        `${API_BASE_URL}/loan/${loanId}/status`,
        request,
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        }
      );
    } catch (error: any) {
      console.error('Error updating loan status:', error);
      throw error;
    }
  },

  getLoanStatistics: async (): Promise<LoanStatistics> => {
    try {
      const response = await axios.get<ApiResponse<LoanStatistics>>(
        `${API_BASE_URL}/loan/statistics`
      );
      return response.data.data;
    } catch (error: any) {
      console.error('Error fetching loan statistics:', error);
      throw error;
    }
  }
};
