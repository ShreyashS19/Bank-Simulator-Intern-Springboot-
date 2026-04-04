import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface Customer {
  customerId?: string;
  name: string;
  phoneNumber: string;
  email: string;
  address: string;
  aadharNumber: string;
  dob: string;
  status: string;
  customerPin?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const customerService = {
  getAllCustomers: async (): Promise<Customer[]> => {
    try {
      const response = await axios.get<ApiResponse<Customer[]>>(`${API_BASE_URL}/customer/all`);
      return response.data.data;
    } catch (error: any) {
      console.error(' Error fetching customers:', error);
      throw error;
    }
  },

  getCustomerByAadhar: async (aadharNumber: string): Promise<Customer> => {
    try {
      const response = await axios.get<ApiResponse<Customer>>(
        `${API_BASE_URL}/customer/aadhar/${aadharNumber}`
      );
      return response.data.data;
    } catch (error: any) {
      console.error(' Error fetching customer:', error);
      throw error;
    }
  },

  createCustomer: async (customer: Omit<Customer, 'customerId'>): Promise<string> => {
    try {
      const response = await axios.post<ApiResponse<string>>(
        `${API_BASE_URL}/customer/onboard`,
        customer,
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        }
      );
      return response.data.data;
    } catch (error: any) {
      console.error(' Error creating customer:', error.response?.data || error);
      throw error;
    }
  },

  updateCustomerByAadhar: async (aadharNumber: string, customer: Customer): Promise<void> => {
    try {
      console.log('==========  UPDATING CUSTOMER ==========');
      console.log('Aadhar:', aadharNumber);
      
      await axios.put<ApiResponse<void>>(
        `${API_BASE_URL}/customer/aadhar/${aadharNumber}`,
        {
          name: customer.name,
          phoneNumber: customer.phoneNumber,
          email: customer.email,
          address: customer.address,
          customerPin: customer.customerPin,
          aadharNumber: customer.aadharNumber,
          dob: customer.dob,
          status: customer.status
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        }
      );
      
      console.log(' Customer updated successfully');
    } catch (error: any) {
      console.error(' Update failed:', error.response?.data || error);
      throw error;
    }
  },

  deleteCustomer: async (aadharNumber: string): Promise<void> => {
    try {
      await axios.delete(`${API_BASE_URL}/customer/aadhar/${aadharNumber}`);
    } catch (error: any) {
      console.error(' Error deleting customer:', error);
      throw error;
    }
  }
};
