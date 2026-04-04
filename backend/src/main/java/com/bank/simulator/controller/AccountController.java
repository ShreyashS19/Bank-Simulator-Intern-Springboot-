package com.bank.simulator.controller;

import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    /**
     * POST /api/account/add
     * Create a new bank account (auto-links customer by Aadhar).
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> createAccount(@RequestBody Map<String, Object> payload) {
        String result = accountService.createAccount(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", result));
    }

    /**
     * GET /api/account/all
     * Get all accounts.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllAccounts() {
        List<AccountEntity> accounts = accountService.getAllAccounts();

        // Map to response format matching frontend expectations
        List<Map<String, Object>> accountList = accounts.stream()
                .map(this::toAccountMap)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accountList));
    }

    /**
     * GET /api/account/number/{accountNumber}
     * Get an account by account number.
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAccountByNumber(@PathVariable String accountNumber) {
        AccountEntity account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", toAccountMap(account)));
    }

    /**
     * PUT /api/account/number/{accountNumber}
     * Update account details.
     */
    @PutMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<Void>> updateAccount(
            @PathVariable String accountNumber,
            @RequestBody Map<String, Object> payload) {
        accountService.updateAccountByNumber(accountNumber, payload);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully"));
    }

    /**
     * DELETE /api/account/number/{accountNumber}
     * Delete account by account number.
     */
    @DeleteMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    /**
     * Maps AccountEntity to a Map matching frontend interface expectations.
     * Exposes: accountId, customerId, accountNumber, aadharNumber, ifscCode,
     * phoneNumberLinked, amount, bankName, nameOnAccount, status, created, modified
     */
    private Map<String, Object> toAccountMap(AccountEntity a) {
        return Map.ofEntries(
            Map.entry("accountId", String.valueOf(a.getId())),
            Map.entry("customerId", a.getCustomer() != null ? String.valueOf(a.getCustomer().getId()) : ""),
            Map.entry("accountNumber", a.getAccountNumber()),
            Map.entry("aadharNumber", a.getAadharNumber()),
            Map.entry("ifscCode", a.getIfscCode()),
            Map.entry("phoneNumberLinked", a.getPhoneNumberLinked()),
            Map.entry("amount", a.getAmount()),
            Map.entry("bankName", a.getBankName()),
            Map.entry("nameOnAccount", a.getNameOnAccount()),
            Map.entry("status", a.getStatus()),
            Map.entry("created", a.getCreated() != null ? a.getCreated().toString() : ""),
            Map.entry("modified", a.getModified() != null ? a.getModified().toString() : "")
        );
    }
}
