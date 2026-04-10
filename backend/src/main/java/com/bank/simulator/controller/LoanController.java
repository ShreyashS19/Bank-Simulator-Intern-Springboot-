package com.bank.simulator.controller;

import com.bank.simulator.dto.*;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for loan operations.
 * Provides endpoints for loan application, retrieval, and management.
 */
@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;
    private final CustomerRepository customerRepository;

    /**
     * POST /api/loan/apply
     * Apply for a loan (authenticated users only).
     * Extracts account number from JWT authentication principal.
     */
    //  @Transactional(readOnly = true)
    @Transactional
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request) {
        
        // Extract authenticated user's email from JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Loan application request from user: {}", email);
        
        // Get customer by email to retrieve account number
        // CustomerEntity customer = customerRepository.findByEmail(email)
        //         .orElseThrow(() -> new BusinessException("Customer not found for email: " + email));
        CustomerEntity customer = customerRepository.findByEmailWithAccounts(email)
        .orElseThrow(() -> new BusinessException("Customer not found for email: " + email));
        // Get the first account (assuming one account per customer for loan applications)
        if (customer.getAccounts().isEmpty()) {
            throw new BusinessException("No account found for customer: " + email);
        }
        
        String accountNumber = customer.getAccounts().get(0).getAccountNumber();
        log.info("Processing loan application for account: {}", accountNumber);
        
        // Process loan application
        LoanResponse response = loanService.applyForLoan(request, accountNumber);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan application submitted successfully", response));
    }

    /**
     * GET /api/loan/account/{accountNumber}
     * Get all loans for a specific account (authenticated users only).
     * Verifies that authenticated user can only access their own account.
     */
    @Transactional(readOnly = true)
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoansByAccount(
            @PathVariable String accountNumber) {
        
        // Extract authenticated user's email from JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        
        // Verify user can only access their own account (unless admin)
        if (!userRole.equals("ROLE_ADMIN")) {
            CustomerEntity customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Customer not found for email: " + email));
            
            boolean ownsAccount = customer.getAccounts().stream()
                    .anyMatch(account -> account.getAccountNumber().equals(accountNumber));
            
            if (!ownsAccount) {
                throw new BusinessException("Access denied: You can only view your own loans");
            }
        }
        
        List<LoanResponse> loans = loanService.getLoansByAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Loans retrieved successfully", loans));
    }

    /**
     * GET /api/loan/{loanId}
     * Get loan details by loan ID (authenticated users only).
     * Verifies that authenticated user can only access loans for their account (or is admin).
     */
    @Transactional(readOnly = true)
    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable String loanId) {
        
        // Extract authenticated user's email from JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        
        // Get loan details
        LoanResponse loan = loanService.getLoanById(loanId);
        
        // Verify user can only access their own loans (unless admin)
        if (!userRole.equals("ROLE_ADMIN")) {
            CustomerEntity customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Customer not found for email: " + email));
            
            boolean ownsAccount = customer.getAccounts().stream()
                    .anyMatch(account -> account.getAccountNumber().equals(loan.getAccountNumber()));
            
            if (!ownsAccount) {
                throw new BusinessException("Access denied: You can only view your own loans");
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success("Loan retrieved successfully", loan));
    }

    /**
     * GET /api/loan/all
     * Get all loans in the system (admin only).
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getAllLoans() {
        log.info("Admin requesting all loans");
        List<LoanResponse> loans = loanService.getAllLoans();
        return ResponseEntity.ok(ApiResponse.success("All loans retrieved successfully", loans));
    }

    /**
     * PUT /api/loan/{loanId}/status
     * Update loan status (admin only).
     */
    @PutMapping("/{loanId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> updateLoanStatus(
            @PathVariable String loanId,
            @Valid @RequestBody UpdateLoanStatusRequest request) {
        
        log.info("Admin updating loan status for loan ID: {} to status: {}", loanId, request.getStatus());
        LoanResponse response = loanService.updateLoanStatus(loanId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Loan status updated successfully", response));
    }

    /**
     * GET /api/loan/statistics
     * Get loan statistics (admin only).
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanStatistics>> getLoanStatistics() {
        log.info("Admin requesting loan statistics");
        LoanStatistics statistics = loanService.getLoanStatistics();
        return ResponseEntity.ok(ApiResponse.success("Loan statistics retrieved successfully", statistics));
    }
}
