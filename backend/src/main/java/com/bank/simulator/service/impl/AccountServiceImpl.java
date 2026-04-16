package com.bank.simulator.service.impl;

import com.bank.simulator.dto.AccountResponse;
import com.bank.simulator.dto.CreateAccountRequest;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.UpdateAccountRequest;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.simulator.entity.OtpEntity;
import com.bank.simulator.entity.OtpPurpose;
import com.bank.simulator.repository.OtpRepository;
import com.bank.simulator.service.NotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final OtpRepository otpRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createAccount(CreateAccountRequest payload) {
        String accountNumber = safeTrim(payload.getAccountNumber());
        String aadharNumber = safeTrim(payload.getAadharNumber());
        String ifscCode = safeTrim(payload.getIfscCode());
        String bankName = safeTrim(payload.getBankName());
        String nameOnAccount = safeTrim(payload.getNameOnAccount());
        String status = safeTrim(payload.getStatus());

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        BigDecimal amount = payload.getAmount() != null ? payload.getAmount() : BigDecimal.valueOf(600.00);

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

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        AccountEntity account = accountRepository.findByAccountNumberWithCustomer(accountNumber)
                .orElseThrow(() -> new BusinessException(
                    "Account not found with number: " + accountNumber, HttpStatus.NOT_FOUND));

        return toResponse(account);
    }

    @Override
    public PageResponse<AccountResponse> getAllAccounts(int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = size > 0 ? size : 20;

        Page<AccountEntity> accountPage = accountRepository.findAllWithCustomer(PageRequest.of(normalizedPage, normalizedSize));

        List<AccountResponse> content = accountPage
                .getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<AccountResponse>builder()
                .content(content)
                .page(accountPage.getNumber())
                .size(accountPage.getSize())
                .totalElements(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void updateAccountByNumber(String accountNumber, UpdateAccountRequest payload) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(
                    "Account not found with number: " + accountNumber, HttpStatus.NOT_FOUND));

        String newAccountNumber = safeTrim(payload.getAccountNumber());
        if (newAccountNumber != null && !newAccountNumber.isBlank()) {
            if (!newAccountNumber.equals(accountNumber) && accountRepository.existsByAccountNumber(newAccountNumber)) {
                throw new BusinessException("Account number already exists");
            }
            account.setAccountNumber(newAccountNumber);
        }

        // Re-link customer if aadhar changes
        String newAadhar = safeTrim(payload.getAadharNumber());
        if (newAadhar != null && !newAadhar.isBlank()) {
            if (!newAadhar.equals(account.getAadharNumber())) {
                CustomerEntity customer = customerRepository.findByAadharNumber(newAadhar)
                        .orElseThrow(() -> new BusinessException("No customer found for Aadhar: " + newAadhar));
                account.setCustomer(customer);
                account.setAadharNumber(newAadhar);
                account.setPhoneNumberLinked(customer.getPhoneNumber());
            }
        }

        String ifscCode = safeTrim(payload.getIfscCode());
        if (ifscCode != null && !ifscCode.isBlank()) {
            account.setIfscCode(ifscCode);
        }

        String bankName = safeTrim(payload.getBankName());
        if (bankName != null && !bankName.isBlank()) {
            account.setBankName(bankName);
        }

        String nameOnAccount = safeTrim(payload.getNameOnAccount());
        if (nameOnAccount != null && !nameOnAccount.isBlank()) {
            account.setNameOnAccount(nameOnAccount);
        }

        String status = safeTrim(payload.getStatus());
        if (status != null && !status.isBlank()) {
            account.setStatus(status);
        }

        if (payload.getAmount() != null) {
            account.setAmount(payload.getAmount());
        }

        accountRepository.save(account);
        log.info("Account updated: accountNumber={}", accountNumber);
    }

    @Override
    @Transactional
    public void deleteAccountByNumber(String accountNumber) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(
                    "Account not found with number: " + accountNumber, HttpStatus.NOT_FOUND));
        accountRepository.delete(account);
        log.info("Account deleted: accountNumber={}", accountNumber);
    }

    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private AccountResponse toResponse(AccountEntity account) {
        return AccountResponse.builder()
                .accountId(String.valueOf(account.getId()))
                .customerId(account.getCustomer() != null ? String.valueOf(account.getCustomer().getId()) : null)
                .accountNumber(account.getAccountNumber())
                .aadharNumber(account.getAadharNumber())
                .ifscCode(account.getIfscCode())
                .phoneNumberLinked(account.getPhoneNumberLinked())
                .amount(account.getAmount())
                .bankName(account.getBankName())
                .nameOnAccount(account.getNameOnAccount())
                .status(account.getStatus())
                .created(account.getCreated())
                .modified(account.getModified())
                .build();
    }
    // ======================== PIN RESET ========================

    @Override
    @Transactional
    public void generateAndSendPinOtp(String email, String jwtEmail) {
        // Enforce JWT ownership
        if (!email.equalsIgnoreCase(jwtEmail)) {
            throw new BusinessException("Unauthorized to request PIN reset for this email", HttpStatus.FORBIDDEN);
        }

        // Layer 1 Rate Limiting: Max 3 requests per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = otpRepository.countByEmailAndPurposeAndCreatedAtAfter(
                email, OtpPurpose.PIN_RESET, oneHourAgo);
        
        if (recentRequests >= 3) {
            log.warn("Rate limit exceeded for PIN reset OTP: {} requests in last hour for email: {}", recentRequests, email);
            throw new BusinessException("Too many OTP requests. Please try again after 1 hour.", HttpStatus.TOO_MANY_REQUESTS);
        }

        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Customer not found", HttpStatus.NOT_FOUND));

        // Invalidate old OTPs
        otpRepository.invalidateExistingOtps(email, OtpPurpose.PIN_RESET);

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

        OtpEntity otpEntity = OtpEntity.builder()
                .email(email)
                .otp(otp)
                .expiryTime(expiryTime)
                .isUsed(false)
                .purpose(OtpPurpose.PIN_RESET)
                .attemptCount(0)
                .build();
        otpRepository.save(otpEntity);

        notificationService.sendPinResetOtpEmail(email, customer.getName(), otp, expiryTime);
        log.info("PIN reset OTP generated for customer email: {}", email);
    }

    @Override
    @Transactional
    public void resetPin(String email, String otp, String newPin, String jwtEmail) {
        if (!email.equalsIgnoreCase(jwtEmail)) {
            throw new BusinessException("Unauthorized to reset PIN for this email", HttpStatus.FORBIDDEN);
        }

        OtpEntity otpEntity = otpRepository.findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
                email, OtpPurpose.PIN_RESET)
                .orElseThrow(() -> new BusinessException("Invalid or expired OTP", HttpStatus.BAD_REQUEST));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        if (otpEntity.getAttemptCount() >= 5) {
            throw new BusinessException("Maximum attempts reached. Please request a new OTP.", HttpStatus.TOO_MANY_REQUESTS);
        }

        if (!otpEntity.getOtp().equals(otp)) {
            otpEntity.setAttemptCount(otpEntity.getAttemptCount() + 1);
            otpRepository.save(otpEntity);
            throw new BusinessException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        if (newPin == null || !newPin.matches("\\d{6}")) {
            throw new BusinessException("PIN must be exactly 6 digits", HttpStatus.BAD_REQUEST);
        }

        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Customer not found", HttpStatus.NOT_FOUND));

        customer.setCustomerPin(passwordEncoder.encode(newPin));
        customerRepository.save(customer);

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        notificationService.sendPinResetSuccessEmail(email, customer.getName(), LocalDateTime.now());
        log.info("PIN successfully reset for customer email: {}", email);
    }
}
