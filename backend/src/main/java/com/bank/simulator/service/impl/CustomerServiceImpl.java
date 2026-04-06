package com.bank.simulator.service.impl;

import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional
    public String createCustomer(Map<String, Object> payload) {
        // Extract fields from payload
        String name = getStringField(payload, "name");
        String phoneNumber = getStringField(payload, "phoneNumber");
        String email = getStringField(payload, "email");
        String address = getStringField(payload, "address");
        String customerPin = getStringField(payload, "customerPin");
        String aadharNumber = getStringField(payload, "aadharNumber");
        String dobStr = getStringField(payload, "dob");
        String status = payload.containsKey("status") ? getStringField(payload, "status") : "Inactive";

        // Validations
        if (name == null || name.isBlank()) throw new BusinessException("Name is required");
        if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) throw new BusinessException("Phone number must be exactly 10 digits");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) throw new BusinessException("Invalid email format");
        if (address == null || address.isBlank()) throw new BusinessException("Address is required");
        if (customerPin == null || !customerPin.matches("\\d{6}")) throw new BusinessException("Customer PIN must be exactly 6 digits");
        if (aadharNumber == null || !aadharNumber.matches("\\d{12}")) throw new BusinessException("Aadhar number must be exactly 12 digits");
        if (dobStr == null || dobStr.isBlank()) throw new BusinessException("Date of birth is required");

        // Uniqueness checks
        if (customerRepository.existsByAadharNumber(aadharNumber)) {
            throw new BusinessException("A customer with this Aadhar number already exists");
        }
        if (customerRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException("A customer with this phone number already exists");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, DOB_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException("Invalid date of birth format. Use yyyy-MM-dd");
        }

        CustomerEntity customer = CustomerEntity.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .address(address)
                .customerPin(customerPin)
                .aadharNumber(aadharNumber)
                .dob(dob)
                .status(status)
                .build();

        CustomerEntity saved = customerRepository.save(customer);
        log.info("Customer onboarded: aadhar={}", aadharNumber);
        return "Customer onboarded successfully. ID: " + saved.getId();
    }

    @Override
    public CustomerEntity getCustomerByAadhar(String aadharNumber) {
        return customerRepository.findByAadharNumber(aadharNumber)
                .orElseThrow(() -> new BusinessException(
                    "Customer not found with Aadhar: " + aadharNumber, HttpStatus.NOT_FOUND));
    }

    @Override
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional
    public void updateCustomerByAadhar(String aadharNumber, Map<String, Object> payload) {
        CustomerEntity customer = customerRepository.findByAadharNumber(aadharNumber)
                .orElseThrow(() -> new BusinessException(
                    "Customer not found with Aadhar: " + aadharNumber, HttpStatus.NOT_FOUND));

        if (payload.containsKey("name") && !getStringField(payload, "name").isBlank()) {
            customer.setName(getStringField(payload, "name"));
        }
        if (payload.containsKey("phoneNumber")) {
            String phone = getStringField(payload, "phoneNumber");
            if (!phone.matches("\\d{10}")) throw new BusinessException("Phone number must be exactly 10 digits");
            // Check uniqueness if changed
            if (!phone.equals(customer.getPhoneNumber()) && customerRepository.existsByPhoneNumber(phone)) {
                throw new BusinessException("A customer with this phone number already exists");
            }
            customer.setPhoneNumber(phone);
        }
        if (payload.containsKey("email")) {
            customer.setEmail(getStringField(payload, "email"));
        }
        if (payload.containsKey("address")) {
            customer.setAddress(getStringField(payload, "address"));
        }
        if (payload.containsKey("customerPin")) {
            String pin = getStringField(payload, "customerPin");
            if (!pin.matches("\\d{6}")) throw new BusinessException("Customer PIN must be exactly 6 digits");
            customer.setCustomerPin(pin);
        }
        if (payload.containsKey("aadharNumber")) {
            String newAadhar = getStringField(payload, "aadharNumber");
            if (!newAadhar.equals(aadharNumber) && customerRepository.existsByAadharNumber(newAadhar)) {
                throw new BusinessException("A customer with this Aadhar number already exists");
            }
            customer.setAadharNumber(newAadhar);
        }
        if (payload.containsKey("dob")) {
            try {
                customer.setDob(LocalDate.parse(getStringField(payload, "dob"), DOB_FORMATTER));
            } catch (Exception e) {
                throw new BusinessException("Invalid date of birth format. Use yyyy-MM-dd");
            }
        }
        if (payload.containsKey("status")) {
            customer.setStatus(getStringField(payload, "status"));
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

    private String getStringField(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
