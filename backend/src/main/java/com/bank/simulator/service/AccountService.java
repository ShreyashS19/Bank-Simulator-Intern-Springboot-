package com.bank.simulator.service;

import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public String createAccount(Map<String, Object> payload) {
        String accountNumber = getStr(payload, "accountNumber");
        String aadharNumber = getStr(payload, "aadharNumber");
        String ifscCode = getStr(payload, "ifscCode");
        String bankName = getStr(payload, "bankName");
        String nameOnAccount = getStr(payload, "nameOnAccount");
        String status = payload.containsKey("status") ? getStr(payload, "status") : "ACTIVE";

        BigDecimal amount;
        try {
            amount = new BigDecimal(payload.getOrDefault("amount", "600.00").toString());
        } catch (Exception e) {
            amount = BigDecimal.valueOf(600.00);
        }

        // Validations
        if (accountNumber == null || accountNumber.isBlank()) throw new BusinessException("Account number is required");
        if (aadharNumber == null || !aadharNumber.matches("\\d{12}")) throw new BusinessException("Valid 12-digit Aadhar number is required");
        if (ifscCode == null || !ifscCode.matches("[A-Z]{4}0[A-Z0-9]{6}")) throw new BusinessException("Invalid IFSC code format (e.g., SBIN0001234)");
        if (bankName == null || bankName.isBlank()) throw new BusinessException("Bank name is required");
        if (nameOnAccount == null || nameOnAccount.isBlank()) throw new BusinessException("Name on account is required");

        // Check duplicate account number
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new BusinessException("Account with this account number already exists");
        }

        // Find and auto-link customer by Aadhar
        CustomerEntity customer = customerRepository.findByAadharNumber(aadharNumber)
                .orElseThrow(() -> new BusinessException(
                    "No customer found with Aadhar number: " + aadharNumber + ". Please complete customer onboarding first."));

        // Auto-link phone number from customer
        String phoneNumberLinked = customer.getPhoneNumber();

        AccountEntity account = AccountEntity.builder()
                .customer(customer)
                .accountNumber(accountNumber)
                .aadharNumber(aadharNumber)
                .ifscCode(ifscCode)
                .phoneNumberLinked(phoneNumberLinked)
                .amount(amount)
                .bankName(bankName)
                .nameOnAccount(nameOnAccount)
                .status(status)
                .build();

        AccountEntity saved = accountRepository.save(account);
        log.info("Account created: accountNumber={}, customerId={}", accountNumber, customer.getId());
        return "Account created successfully. Account Number: " + saved.getAccountNumber();
    }

    public AccountEntity getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(
                    "Account not found with number: " + accountNumber, HttpStatus.NOT_FOUND));
    }

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAllByOrderByCreatedDesc();
    }

    @Transactional
    public void updateAccountByNumber(String accountNumber, Map<String, Object> payload) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(
                    "Account not found with number: " + accountNumber, HttpStatus.NOT_FOUND));

        if (payload.containsKey("accountNumber")) {
            String newAccountNumber = getStr(payload, "accountNumber");
            if (!newAccountNumber.equals(accountNumber) && accountRepository.existsByAccountNumber(newAccountNumber)) {
                throw new BusinessException("Account number already exists");
            }
            account.setAccountNumber(newAccountNumber);
        }

        // Re-link customer if aadhar changes
        if (payload.containsKey("aadharNumber")) {
            String newAadhar = getStr(payload, "aadharNumber");
            if (!newAadhar.equals(account.getAadharNumber())) {
                CustomerEntity customer = customerRepository.findByAadharNumber(newAadhar)
                        .orElseThrow(() -> new BusinessException("No customer found for Aadhar: " + newAadhar));
                account.setCustomer(customer);
                account.setAadharNumber(newAadhar);
                account.setPhoneNumberLinked(customer.getPhoneNumber());
            }
        }

        if (payload.containsKey("ifscCode")) account.setIfscCode(getStr(payload, "ifscCode"));
        if (payload.containsKey("bankName")) account.setBankName(getStr(payload, "bankName"));
        if (payload.containsKey("nameOnAccount")) account.setNameOnAccount(getStr(payload, "nameOnAccount"));
        if (payload.containsKey("status")) account.setStatus(getStr(payload, "status"));
        if (payload.containsKey("amount")) {
            try {
                account.setAmount(new BigDecimal(payload.get("amount").toString()));
            } catch (Exception e) {
                throw new BusinessException("Invalid amount value");
            }
        }

        accountRepository.save(account);
        log.info("Account updated: accountNumber={}", accountNumber);
    }

    @Transactional
    public void deleteAccountByNumber(String accountNumber) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(
                    "Account not found with number: " + accountNumber, HttpStatus.NOT_FOUND));
        accountRepository.delete(account);
        log.info("Account deleted: accountNumber={}", accountNumber);
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
