package com.bank.simulator.controller;

import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.dto.CreateTransactionRequest;
import com.bank.simulator.dto.PageResponse;
import com.bank.simulator.dto.TransactionResponse;
import com.bank.simulator.entity.TransactionEntity;
import com.bank.simulator.service.ExcelGeneratorService;
import com.bank.simulator.service.TransactionService;
import jakarta.validation.Valid;
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
        public ResponseEntity<ApiResponse<String>> createTransaction(@Valid @RequestBody CreateTransactionRequest payload) {
        String transactionId = transactionService.createTransaction(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction completed successfully", transactionId));
    }

    /**
     * GET /api/transaction/getTransactionsByAccountNumber/{accountNumber}
     * Get all transactions for a specific account number (both sent and received).
     */
    @GetMapping("/getTransactionsByAccountNumber/{accountNumber}")
        public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByAccount(
            @PathVariable String accountNumber) {
                List<TransactionResponse> transactions = transactionService.getTransactionsByAccount(accountNumber);
                return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
    }

    /**
     * GET /api/transaction/all
     * Get all transactions (admin use).
     */
    @GetMapping("/all")
        public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAllTransactions(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                PageResponse<TransactionResponse> transactions = transactionService.getAllTransactions(page, size);
                return ResponseEntity.ok(ApiResponse.success("All transactions retrieved", transactions));
    }

    /**
     * GET /api/transaction/download/{accountNumber}
     * Download transactions for a specific account as Excel file.
     */
    @GetMapping("/download/{accountNumber}")
    public ResponseEntity<byte[]> downloadTransactionsByAccount(@PathVariable String accountNumber) {
        List<TransactionEntity> transactions = transactionService.getTransactionsByAccountForExport(accountNumber);
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
        List<TransactionEntity> transactions = transactionService.getAllTransactionsForExport();
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
}
