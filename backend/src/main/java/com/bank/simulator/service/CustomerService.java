package com.bank.simulator.service;

import com.bank.simulator.dto.CreateCustomerRequest;
import com.bank.simulator.dto.CustomerResponse;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.UpdateCustomerRequest;

public interface CustomerService {
    String createCustomer(CreateCustomerRequest payload);

    CustomerResponse getCustomerByAadhar(String aadharNumber);

    CustomerResponse getCustomerByEmail(String email);

    PageResponse<CustomerResponse> getAllCustomers(int page, int size);

    void updateCustomerByAadhar(String aadharNumber, UpdateCustomerRequest payload);

    void deleteCustomerByAadhar(String aadharNumber);
}
