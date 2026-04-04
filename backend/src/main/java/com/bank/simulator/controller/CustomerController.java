package com.bank.simulator.controller;

import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * POST /api/customer/onboard
     * Onboard a new customer.
     */
    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<String>> onboardCustomer(@RequestBody Map<String, Object> payload) {
        String result = customerService.createCustomer(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer onboarded successfully", result));
    }

    /**
     * GET /api/customer/all
     * Get all customers.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CustomerEntity>>> getAllCustomers() {
        List<CustomerEntity> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    /**
     * GET /api/customer/aadhar/{aadharNumber}
     * Get customer by Aadhar number.
     */
    @GetMapping("/aadhar/{aadharNumber}")
    public ResponseEntity<ApiResponse<CustomerEntity>> getCustomerByAadhar(@PathVariable String aadharNumber) {
        CustomerEntity customer = customerService.getCustomerByAadhar(aadharNumber);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
    }

    /**
     * PUT /api/customer/aadhar/{aadharNumber}
     * Update customer details by Aadhar number.
     */
    @PutMapping("/aadhar/{aadharNumber}")
    public ResponseEntity<ApiResponse<Void>> updateCustomer(
            @PathVariable String aadharNumber,
            @RequestBody Map<String, Object> payload) {
        customerService.updateCustomerByAadhar(aadharNumber, payload);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully"));
    }

    /**
     * DELETE /api/customer/aadhar/{aadharNumber}
     * Delete customer by Aadhar number (cascades to accounts and transactions).
     */
    @DeleteMapping("/aadhar/{aadharNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable String aadharNumber) {
        customerService.deleteCustomerByAadhar(aadharNumber);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully"));
    }
}
