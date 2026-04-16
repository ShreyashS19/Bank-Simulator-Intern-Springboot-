package com.bank.simulator.service;

import com.bank.simulator.dto.AccountResponse;
import com.bank.simulator.dto.CreateAccountRequest;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.UpdateAccountRequest;

public interface AccountService {
    String createAccount(CreateAccountRequest payload);
    AccountResponse getAccountByNumber(String accountNumber);
    PageResponse<AccountResponse> getAllAccounts(int page, int size);
    void updateAccountByNumber(String accountNumber, UpdateAccountRequest payload);
    void deleteAccountByNumber(String accountNumber);
    void generateAndSendPinOtp(String email, String jwtEmail);
    void resetPin(String email, String otp, String newPin, String jwtEmail);
}
