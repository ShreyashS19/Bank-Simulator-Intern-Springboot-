package com.bank.simulator.service;

import com.bank.simulator.entity.CustomerEntity;

import java.util.List;
import java.util.Map;

public interface CustomerService {
    String createCustomer(Map<String, Object> payload);

    CustomerEntity getCustomerByAadhar(String aadharNumber);

    List<CustomerEntity> getAllCustomers();

    void updateCustomerByAadhar(String aadharNumber, Map<String, Object> payload);

    void deleteCustomerByAadhar(String aadharNumber);
}
