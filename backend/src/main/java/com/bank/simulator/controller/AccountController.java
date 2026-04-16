package com.bank.simulator.controller;

import com.bank.simulator.dto.AccountResponse;
import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.dto.CreateAccountRequest;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.UpdateAccountRequest;
import com.bank.simulator.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<String>> createAccount(@Valid @RequestBody CreateAccountRequest payload) {
        String result = accountService.createAccount(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", result));
    }

    /**
     * GET /api/account/all
     * Get all accounts.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AccountResponse> accounts = accountService.getAllAccounts(page, size);
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    /**
     * GET /api/account/number/{accountNumber}
     * Get an account by account number.
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", account));
    }

    /**
     * PUT /api/account/number/{accountNumber}
     * Update account details.
     */
    @PutMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<Void>> updateAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody UpdateAccountRequest payload) {
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
     * POST /api/account/forgot-pin
     * Request an OTP for PIN reset (JWT protected).
     */
    @PostMapping("/forgot-pin")
    public ResponseEntity<ApiResponse<Void>> forgotPin(@Valid @RequestBody com.bank.simulator.dto.ForgotPinRequest request) {
        String jwtEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.generateAndSendPinOtp(request.getEmail(), jwtEmail);
        return ResponseEntity.ok(ApiResponse.success("If the account exists, a PIN reset OTP has been sent."));
    }

    /**
     * POST /api/account/reset-pin
     * Submit OTP and new PIN to reset account PIN (JWT protected).
     */
    @PostMapping("/reset-pin")
    public ResponseEntity<ApiResponse<Void>> resetPin(@Valid @RequestBody com.bank.simulator.dto.ResetPinRequest request) {
        String jwtEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.resetPin(request.getEmail(), request.getOtp(), request.getNewPin(), jwtEmail);
        return ResponseEntity.ok(ApiResponse.success("Account PIN reset successfully."));
    }
}
