package com.bank.simulator.service;

import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.LoanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LoanService
 * Tests verify specific examples and edge cases for loan service methods
 */
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private CreditScoringService creditScoringService;
    
    @Mock
    private NotificationService notificationService;

    @Mock
    private LoanPdfService loanPdfService;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(
                loanRepository,
                creditScoringService,
                notificationService,
                loanPdfService,
                accountRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("determineStatus returns APPROVED when eligibility score >= 75.0 and DTI < 0.40")
    void determineStatus_ApprovedCase() {
        // Arrange
        double eligibilityScore = 80.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.35);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("determineStatus returns UNDER_REVIEW when eligibility score is between 65.0 and 75.0")
    void determineStatus_UnderReviewByScore() {
        // Arrange
        double eligibilityScore = 70.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.30);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("determineStatus returns UNDER_REVIEW when DTI is between 0.40 and 0.50")
    void determineStatus_UnderReviewByDti() {
        // Arrange
        double eligibilityScore = 80.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.45);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("determineStatus returns REJECTED when eligibility score < 65.0")
    void determineStatus_RejectedByScore() {
        // Arrange
        double eligibilityScore = 60.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.30);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("determineStatus returns REJECTED when DTI > 0.50")
    void determineStatus_RejectedByDti() {
        // Arrange
        double eligibilityScore = 80.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.55);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("determineStatus handles boundary case: score = 75.0, DTI = 0.39")
    void determineStatus_BoundaryApproved() {
        // Arrange
        double eligibilityScore = 75.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.39);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("determineStatus handles boundary case: score = 75.0, DTI = 0.40")
    void determineStatus_BoundaryUnderReview() {
        // Arrange
        double eligibilityScore = 75.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.40);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("determineStatus handles boundary case: score = 65.0, DTI = 0.30")
    void determineStatus_BoundaryUnderReviewScore() {
        // Arrange
        double eligibilityScore = 65.0;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.30);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("determineStatus handles boundary case: score = 64.99, DTI = 0.30")
    void determineStatus_BoundaryRejected() {
        // Arrange
        double eligibilityScore = 64.99;
        BigDecimal dtiRatio = BigDecimal.valueOf(0.30);
        
        // Act
        String status = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert
        assertThat(status).isEqualTo("REJECTED");
    }
}
