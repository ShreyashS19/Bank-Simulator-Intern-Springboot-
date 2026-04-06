package com.bank.simulator.service;

import com.bank.simulator.entity.AccountEntity;

import java.util.List;
import java.util.Map;

public interface AccountService {
    String createAccount(Map<String, Object> payload);
    AccountEntity getAccountByNumber(String accountNumber);
    List<AccountEntity> getAllAccounts();
    void updateAccountByNumber(String accountNumber, Map<String, Object> payload);
    void deleteAccountByNumber(String accountNumber);
}
