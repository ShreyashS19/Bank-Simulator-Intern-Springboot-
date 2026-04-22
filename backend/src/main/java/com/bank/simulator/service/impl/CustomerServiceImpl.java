package com.bank.simulator.service.impl;

import com.bank.simulator.dto.CreateCustomerRequest;
import com.bank.simulator.dto.CustomerResponse;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.UpdateCustomerRequest;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createCustomer(CreateCustomerRequest payload) {
        String name = safeTrim(payload.getName());
        String phoneNumber = safeTrim(payload.getPhoneNumber());
        String email = safeTrim(payload.getEmail());
        String address = safeTrim(payload.getAddress());
        String customerPin = safeTrim(payload.getCustomerPin());
        String aadharNumber = safeTrim(payload.getAadharNumber());
        LocalDate dob = payload.getDob();
        String status = safeTrim(payload.getStatus());

        if (status == null || status.isBlank()) {
            status = "INACTIVE";
        }
        status = normalizeCustomerStatus(status);

        // Validations
        if (name == null || name.isBlank()) throw new BusinessException("Name is required");
        if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) throw new BusinessException("Phone number must be exactly 10 digits");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) throw new BusinessException("Invalid email format");
        if (address == null || address.isBlank()) throw new BusinessException("Address is required");
        if (customerPin == null || !customerPin.matches("\\d{6}")) throw new BusinessException("Customer PIN must be exactly 6 digits");
        if (aadharNumber == null || !aadharNumber.matches("\\d{12}")) throw new BusinessException("Aadhar number must be exactly 12 digits");
        if (dob == null) throw new BusinessException("Date of birth is required");

        // Uniqueness checks
        if (customerRepository.existsByAadharNumber(aadharNumber)) {
            throw new BusinessException("A customer with this Aadhar number already exists");
        }
        if (customerRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException("A customer with this phone number already exists");
        }

        CustomerEntity customer = CustomerEntity.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .address(address)
                .customerPin(passwordEncoder.encode(customerPin))
                .aadharNumber(aadharNumber)
                .dob(dob)
                .status(status)
                .build();

        CustomerEntity saved = customerRepository.save(customer);
        log.info("Customer onboarded: aadhar={}", aadharNumber);
        return "Customer onboarded successfully. ID: " + saved.getId();
    }

    @Override
    public CustomerResponse getCustomerByAadhar(String aadharNumber) {
        CustomerEntity customer = customerRepository.findByAadharNumber(aadharNumber)
                .orElseThrow(() -> new BusinessException(
                    "Customer not found with Aadhar: " + aadharNumber, HttpStatus.NOT_FOUND));

        return toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByEmail(String email) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                    "Customer not found with Email: " + email, HttpStatus.NOT_FOUND));

        return toResponse(customer);
    }

    @Override
    public PageResponse<CustomerResponse> getAllCustomers(int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = size > 0 ? size : 20;

        Page<CustomerEntity> customerPage = customerRepository.findAll(PageRequest.of(normalizedPage, normalizedSize));

        List<CustomerResponse> content = customerPage
                .getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<CustomerResponse>builder()
                .content(content)
                .page(customerPage.getNumber())
                .size(customerPage.getSize())
                .totalElements(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void updateCustomerByAadhar(String aadharNumber, UpdateCustomerRequest payload) {
        CustomerEntity customer = customerRepository.findByAadharNumber(aadharNumber)
                .orElseThrow(() -> new BusinessException(
                    "Customer not found with Aadhar: " + aadharNumber, HttpStatus.NOT_FOUND));

        String name = safeTrim(payload.getName());
        if (name != null && !name.isBlank()) {
            customer.setName(name);
        }

        String phone = safeTrim(payload.getPhoneNumber());
        if (phone != null) {
            if (!phone.matches("\\d{10}")) throw new BusinessException("Phone number must be exactly 10 digits");
            // Check uniqueness if changed
            if (!phone.equals(customer.getPhoneNumber()) && customerRepository.existsByPhoneNumber(phone)) {
                throw new BusinessException("A customer with this phone number already exists");
            }
            customer.setPhoneNumber(phone);
        }

        if (payload.getEmail() != null) {
            customer.setEmail(safeTrim(payload.getEmail()));
        }

        if (payload.getAddress() != null) {
            customer.setAddress(safeTrim(payload.getAddress()));
        }

        String pin = safeTrim(payload.getCustomerPin());
        if (pin != null) {
            if (!pin.matches("\\d{6}")) throw new BusinessException("Customer PIN must be exactly 6 digits");
            customer.setCustomerPin(passwordEncoder.encode(pin));
        }

        String newAadhar = safeTrim(payload.getAadharNumber());
        if (newAadhar != null) {
            if (!newAadhar.matches("\\d{12}")) {
                throw new BusinessException("Aadhar number must be exactly 12 digits");
            }
            if (!newAadhar.equals(aadharNumber) && customerRepository.existsByAadharNumber(newAadhar)) {
                throw new BusinessException("A customer with this Aadhar number already exists");
            }
            customer.setAadharNumber(newAadhar);
        }

        if (payload.getDob() != null) {
            customer.setDob(payload.getDob());
        }

        if (payload.getStatus() != null) {
            customer.setStatus(normalizeCustomerStatus(safeTrim(payload.getStatus())));
        }

        customerRepository.save(customer);
        log.info("Customer updated: aadhar={}", aadharNumber);
    }

    @Override
    @Transactional
    public void deleteCustomerByAadhar(String aadharNumber) {
        CustomerEntity customer = customerRepository.findByAadharNumber(aadharNumber)
                .orElseThrow(() -> new BusinessException(
                    "Customer not found with Aadhar: " + aadharNumber, HttpStatus.NOT_FOUND));
        customerRepository.delete(customer);
        log.info("Customer deleted: aadhar={}", aadharNumber);
    }

    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private String normalizeCustomerStatus(String status) {
        if (status == null || status.isBlank()) {
            return "INACTIVE";
        }

        String normalized = status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new BusinessException("Status must be either ACTIVE or INACTIVE");
        }

        return normalized;
    }

    private CustomerResponse toResponse(CustomerEntity customer) {
        return CustomerResponse.builder()
                .customerId(String.valueOf(customer.getId()))
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .aadharNumber(customer.getAadharNumber())
                .dob(customer.getDob())
                .status(customer.getStatus())
                .build();
    }
}
