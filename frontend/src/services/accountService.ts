import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface Account {
  accountId?: string;
  customerId?: string;
  accountNumber: string;
  aadharNumber: string;
  ifscCode: string;
  phoneNumberLinked: string;
  amount: number;
  bankName: string;
  nameOnAccount: string;
  status: string;
  created?: string;
  modified?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const accountService = {
  getAllAccounts: async (): Promise<Account[]> => {
    try {
      console.log(' Fetching all accounts');
      const response = await axios.get<ApiResponse<Account[]>>(`${API_BASE_URL}/account/all`);
      console.log(' Accounts fetched:', response.data.data.length);
      return response.data.data;
    } catch (error: any) {
      console.error(' Error fetching accounts:', error);
      throw error;
    }
  },

  getAccountByNumber: async (accountNumber: string): Promise<Account> => {
    try {
      console.log(' Fetching account by number:', accountNumber);
      const response = await axios.get<ApiResponse<Account>>(
        `${API_BASE_URL}/account/number/${accountNumber}`
      );
      console.log(' Account found:', response.data.data);
      return response.data.data;
    } catch (error: any) {
      console.error(' Error fetching account:', error);
      throw error;
    }
  },

  createAccount: async (account: Omit<Account, 'accountId' | 'customerId'>): Promise<string> => {
    try {
      console.log('==========  CREATING ACCOUNT ==========');
      console.log('API URL:', `${API_BASE_URL}/account/add`);
      console.log('Payload:', JSON.stringify(account, null, 2));
      
      const response = await axios.post<ApiResponse<string>>(`${API_BASE_URL}/account/add`, account,
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        }
      );
      
      console.log(' Account created! ', response.data.data);
      return response.data.data;
    } catch (error: any) {
      console.error(' Error creating account:', error.response?.data || error);
      throw error;
    }
  },

  updateAccount: async (accountNumber: string, account: Partial<Account>): Promise<void> => {
    try {
      console.log('==========  UPDATING ACCOUNT ==========');
      console.log('Account Number:', accountNumber);
      console.log('API URL:', `${API_BASE_URL}/account/number/${accountNumber}`);
      
      await axios.put(
        `${API_BASE_URL}/account/number/${accountNumber}`,
        account,
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log(' Account updated successfully');
    } catch (error: any) {
      console.error(' Error updating account:', error);
      throw error;
    }
  },

  deleteAccount: async (accountNumber: string): Promise<void> => {
    try {
      console.log(' Deleting account by number:', accountNumber);
      await axios.delete(`${API_BASE_URL}/account/number/${accountNumber}`);
      console.log(' Account deleted successfully');
    } catch (error: any) {
      console.error(' Error deleting account:', error);
      throw error;
    }
  }
};
