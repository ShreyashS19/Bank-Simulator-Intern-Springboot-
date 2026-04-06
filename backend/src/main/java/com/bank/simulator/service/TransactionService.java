package com.bank.simulator.service;

import com.bank.simulator.entity.TransactionEntity;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    String createTransaction(Map<String, Object> payload);
    List<TransactionEntity> getTransactionsByAccount(String accountNumber);
    List<TransactionEntity> getAllTransactions();
    void deleteTransaction(String transactionId);
}