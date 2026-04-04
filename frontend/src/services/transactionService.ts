import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface Transaction {
  timestamp: string | number | Date;
  transactionId?: string;
  senderAccountNumber: string;
  receiverAccountNumber: string;
  amount: number;
  transactionType: string;
  description?: string;
  pin: string;
  createdDate?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const transactionService = {
  getTransactionsByAccount: async (accountNumber: string): Promise<Transaction[]> => {
    try {
      const response = await axios.get<ApiResponse<Transaction[]>>(
        `${API_BASE_URL}/transaction/getTransactionsByAccountNumber/${accountNumber}`
      );
      return response.data.data;
    } catch (error) {
      console.error('Error fetching transactions:', error);
      throw error;
    }
  },

  createTransaction: async (transaction: Omit<Transaction, 'transactionId'>): Promise<string> => {
    try {
      const response = await axios.post<ApiResponse<string>>(
        `${API_BASE_URL}/transaction/createTransaction`,
        transaction
      );
      return response.data.data;
    } catch (error) {
      console.error('Error creating transaction:', error);
      throw error;
    }
  },

  downloadTransactions: async (accountNumber: string): Promise<Blob> => {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/transaction/download/${accountNumber}`,
        { responseType: 'blob' }
      );
      return response.data;
    } catch (error) {
      console.error('Error downloading transactions:', error);
      throw error;
    }
  },

  getAllTransactions: async (): Promise<Transaction[]> => {
    try {
      console.log(' Fetching all transactions');
      const response = await axios.get<ApiResponse<Transaction[]>>(`${API_BASE_URL}/transaction/all`);
      console.log(' Transactions fetched:', response.data.data.length);
      return response.data.data;
    } catch (error: any) {
      console.error(' Error fetching all transactions:', error);
      return [];
    }
  },

  deleteTransaction: async (transactionId: string): Promise<void> => {
    try {
      const response = await axios.delete<ApiResponse<null>>(
        `${API_BASE_URL}/transaction/${transactionId}`
      );
      if (!response.data.success) {
        throw new Error(response.data.message);
      }
    } catch (error: any) {
      console.error('Error deleting transaction:', error);
      throw error;
    }
  },
};

export default transactionService;
