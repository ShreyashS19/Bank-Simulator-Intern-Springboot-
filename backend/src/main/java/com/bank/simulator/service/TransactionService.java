package com.bank.simulator.service;

import com.bank.simulator.dto.CreateTransactionRequest;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.TransactionResponse;
import com.bank.simulator.entity.TransactionEntity;

import java.util.List;

public interface TransactionService {
    String createTransaction(CreateTransactionRequest payload);
    List<TransactionResponse> getTransactionsByAccount(String accountNumber);
    PageResponse<TransactionResponse> getAllTransactions(int page, int size);
    List<TransactionEntity> getTransactionsByAccountForExport(String accountNumber);
    List<TransactionEntity> getAllTransactionsForExport();
    void deleteTransaction(String transactionId);
}