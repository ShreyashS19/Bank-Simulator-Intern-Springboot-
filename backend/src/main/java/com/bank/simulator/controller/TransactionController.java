package com.bank.simulator.controller;

import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.entity.TransactionEntity;
import com.bank.simulator.service.ExcelGeneratorService;
import com.bank.simulator.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final ExcelGeneratorService excelGeneratorService;

    /**
     * POST /api/transaction/createTransaction
     * Create a new money transfer transaction.
     * Validates PIN, active status, balance, and self-transfer.
     */
    @PostMapping("/createTransaction")
    public ResponseEntity<ApiResponse<String>> createTransaction(@RequestBody Map<String, Object> payload) {
        String transactionId = transactionService.createTransaction(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction completed successfully", transactionId));
    }

    /**
     * GET /api/transaction/getTransactionsByAccountNumber/{accountNumber}
     * Get all transactions for a specific account number (both sent and received).
     */
    @GetMapping("/getTransactionsByAccountNumber/{accountNumber}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTransactionsByAccount(
            @PathVariable String accountNumber) {
        List<TransactionEntity> transactions = transactionService.getTransactionsByAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully",
                transactions.stream().map(this::toTransactionMap).toList()));
    }

    /**
     * GET /api/transaction/all
     * Get all transactions (admin use).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTransactions() {
        List<TransactionEntity> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success("All transactions retrieved",
                transactions.stream().map(this::toTransactionMap).toList()));
    }

    /**
     * GET /api/transaction/download/{accountNumber}
     * Download transactions for a specific account as Excel file.
     */
    @GetMapping("/download/{accountNumber}")
    public ResponseEntity<byte[]> downloadTransactionsByAccount(@PathVariable String accountNumber) {
        List<TransactionEntity> transactions = transactionService.getTransactionsByAccount(accountNumber);
        String title = "Transaction History — Account: " + accountNumber;
        byte[] excelBytes = excelGeneratorService.generateTransactionExcel(transactions, title);

        String filename = "transactions_" + accountNumber + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * GET /api/transaction/download/all
     * Download ALL transactions across all accounts as Excel file.
     */
    @GetMapping("/download/all")
    public ResponseEntity<byte[]> downloadAllTransactions() {
        List<TransactionEntity> transactions = transactionService.getAllTransactions();
        String title = "All Transactions — Bank Simulator Report";
        byte[] excelBytes = excelGeneratorService.generateTransactionExcel(transactions, title);

        String filename = "all_transactions_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * DELETE /api/transaction/{transactionId}
     * Delete a transaction by its TXN_YYYYMMDDNNN ID.
     */
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable String transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }

    /**
     * Maps TransactionEntity to a Map matching the frontend Transaction interface.
     */
    private Map<String, Object> toTransactionMap(TransactionEntity t) {
        return Map.ofEntries(
            Map.entry("transactionId", t.getTransactionId() != null ? t.getTransactionId() : ""),
            Map.entry("senderAccountNumber", t.getSenderAccountNumber()),
            Map.entry("receiverAccountNumber", t.getReceiverAccountNumber()),
            Map.entry("amount", t.getAmount()),
            Map.entry("transactionType", t.getTransactionType() != null ? t.getTransactionType() : "ONLINE"),
            Map.entry("description", t.getDescription() != null ? t.getDescription() : ""),
            Map.entry("createdDate", t.getCreatedDate() != null ? t.getCreatedDate().toString() : ""),
            Map.entry("timestamp", t.getCreatedDate() != null ? t.getCreatedDate().toString() : ""),
            Map.entry("pin", "")
        );
    }
}
