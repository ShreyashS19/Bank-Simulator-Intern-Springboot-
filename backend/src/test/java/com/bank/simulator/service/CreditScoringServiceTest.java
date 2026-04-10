package com.bank.simulator.service;

import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreditScoringService
 */
@ExtendWith(MockitoExtension.class)
class CreditScoringServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CreditScoringService creditScoringService;

    /**
     * Test for Requirement 5.1: DTI Ratio Calculation with Division by Zero
     * 
     * When monthlyIncome is zero, the method should return 1.0 to indicate
     * maximum debt burden (100% DTI ratio)
     */
    @Test
    void calculateDtiRatio_whenMonthlyIncomeIsZero_shouldReturnOne() {
        // Arrange
        BigDecimal existingEmi = BigDecimal.valueOf(5000);
        BigDecimal monthlyIncome = BigDecimal.ZERO;

        // Act
        BigDecimal result = creditScoringService.calculateDtiRatio(existingEmi, monthlyIncome);

        // Assert
        assertThat(result)
                .as("DTI ratio should be 1.0 when monthly income is zero")
                .isEqualTo(BigDecimal.ONE);
    }

    /**
     * Test for Requirement 5.1: DTI Ratio Calculation with Normal Values
     * 
     * Verifies that DTI ratio is calculated correctly and rounded to 4 decimal places
     */
    @Test
    void calculateDtiRatio_withNormalValues_shouldCalculateCorrectly() {
        // Arrange
        BigDecimal existingEmi = BigDecimal.valueOf(10000);
        BigDecimal monthlyIncome = BigDecimal.valueOf(50000);

        // Act
        BigDecimal result = creditScoringService.calculateDtiRatio(existingEmi, monthlyIncome);

        // Assert
        assertThat(result)
                .as("DTI ratio should be 0.2000 (10000/50000)")
                .isEqualTo(new BigDecimal("0.2000"));
    }

    /**
     * Test for Requirement 5.1: DTI Ratio Calculation with Rounding
     * 
     * Verifies that DTI ratio is rounded to 4 decimal places
     */
    @Test
    void calculateDtiRatio_shouldRoundToFourDecimalPlaces() {
        // Arrange
        BigDecimal existingEmi = BigDecimal.valueOf(10000);
        BigDecimal monthlyIncome = BigDecimal.valueOf(30000);

        // Act
        BigDecimal result = creditScoringService.calculateDtiRatio(existingEmi, monthlyIncome);

        // Assert
        assertThat(result)
                .as("DTI ratio should be rounded to 4 decimal places")
                .isEqualTo(new BigDecimal("0.3333"));
        assertThat(result.scale())
                .as("DTI ratio should have scale of 4")
                .isEqualTo(4);
    }

    /**
     * Test for Requirement 10.1-10.6: Banking Relationship Score with Account Not Found
     * 
     * When account is not found, the method should return 10 points (minimum score)
     * and handle the error gracefully without throwing an exception
     */
    @Test
    void calculateBankingRelationshipScore_whenAccountNotFound_shouldReturnMinimumScore() {
        // Arrange
        String accountNumber = "ACC-12345678";
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        // Act
        double result = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(result)
                .as("Banking relationship score should be 10.0 when account not found")
                .isEqualTo(10.0);
    }

    /**
     * Test for Requirement 10.1-10.6: Banking Relationship Score with Null Creation Date
     * 
     * When account creation date is null, the method should return 10 points (minimum score)
     * and handle the error gracefully
     */
    @Test
    void calculateBankingRelationshipScore_whenCreationDateIsNull_shouldReturnMinimumScore() {
        // Arrange
        String accountNumber = "ACC-12345678";
        AccountEntity account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        account.setCreated(null);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double result = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(result)
                .as("Banking relationship score should be 10.0 when creation date is null")
                .isEqualTo(10.0);
    }

    /**
     * Test for Requirement 30.1-30.4: Transaction Pattern Score with Retrieval Failure
     * 
     * When transaction retrieval fails, the method should return 0.0
     * and handle the error gracefully without throwing an exception
     */
    @Test
    void calculateTransactionPatternScore_whenTransactionRetrievalFails_shouldReturnZero() {
        // Arrange
        String accountNumber = "ACC-12345678";
        when(transactionRepository.findByAccountNumberAndCreatedDateAfter(anyString(), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        double result = creditScoringService.calculateTransactionPatternScore(accountNumber);

        // Assert
        assertThat(result)
                .as("Transaction pattern score should be 0.0 when retrieval fails")
                .isEqualTo(0.0);
    }

    /**
     * Test for Requirement 30.1-30.4: Transaction Pattern Score with No Transactions
     * 
     * When no transactions are found, the method should return 0.0
     */
    @Test
    void calculateTransactionPatternScore_whenNoTransactions_shouldReturnZero() {
        // Arrange
        String accountNumber = "ACC-12345678";
        when(transactionRepository.findByAccountNumberAndCreatedDateAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        double result = creditScoringService.calculateTransactionPatternScore(accountNumber);

        // Assert
        assertThat(result)
                .as("Transaction pattern score should be 0.0 when no transactions found")
                .isEqualTo(0.0);
    }

    // Boundary Value Tests for All Factors

    /**
     * Test for Requirement 3.1-3.6: Income Score Boundary Values
     * 
     * Tests exact boundary values for income brackets
     */
    @Test
    void calculateIncomeScore_atBoundaryValues_shouldReturnCorrectScores() {
        // Test lower boundaries
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(100000)))
                .as("Income of 100000 should return 120 points")
                .isEqualTo(120.0);
        
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(75000)))
                .as("Income of 75000 should return 100 points")
                .isEqualTo(100.0);
        
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(50000)))
                .as("Income of 50000 should return 80 points")
                .isEqualTo(80.0);
        
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(30000)))
                .as("Income of 30000 should return 60 points")
                .isEqualTo(60.0);
        
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(20000)))
                .as("Income of 20000 should return 40 points")
                .isEqualTo(40.0);
        
        // Test upper boundaries (just below next bracket)
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(99999)))
                .as("Income of 99999 should return 100 points")
                .isEqualTo(100.0);
        
        assertThat(creditScoringService.calculateIncomeScore(BigDecimal.valueOf(19999)))
                .as("Income of 19999 should return 20 points")
                .isEqualTo(20.0);
    }

    /**
     * Test for Requirement 5.2-5.6: DTI Score Boundary Values
     * 
     * Tests exact boundary values for DTI ratio brackets
     */
    @Test
    void calculateDtiScore_atBoundaryValues_shouldReturnCorrectScores() {
        // Test lower boundaries
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.19)))
                .as("DTI of 0.19 should return 100 points")
                .isEqualTo(100.0);
        
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.20)))
                .as("DTI of 0.20 should return 80 points")
                .isEqualTo(80.0);
        
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.30)))
                .as("DTI of 0.30 should return 60 points")
                .isEqualTo(60.0);
        
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.40)))
                .as("DTI of 0.40 should return 40 points")
                .isEqualTo(40.0);
        
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.50)))
                .as("DTI of 0.50 should return 0 points")
                .isEqualTo(0.0);
        
        // Test upper boundaries (just below next bracket)
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.29)))
                .as("DTI of 0.29 should return 80 points")
                .isEqualTo(80.0);
        
        assertThat(creditScoringService.calculateDtiScore(BigDecimal.valueOf(0.49)))
                .as("DTI of 0.49 should return 40 points")
                .isEqualTo(40.0);
    }

    /**
     * Test for Requirement 7.1-7.3: Age Score Boundary Values
     * 
     * Tests exact boundary values for age brackets
     */
    @Test
    void calculateAgeScore_atBoundaryValues_shouldReturnCorrectScores() {
        // Test lower boundaries
        assertThat(creditScoringService.calculateAgeScore(18))
                .as("Age 18 should return 30 points")
                .isEqualTo(30.0);
        
        assertThat(creditScoringService.calculateAgeScore(25))
                .as("Age 25 should return 50 points")
                .isEqualTo(50.0);
        
        assertThat(creditScoringService.calculateAgeScore(30))
                .as("Age 30 should return 60 points")
                .isEqualTo(60.0);
        
        assertThat(creditScoringService.calculateAgeScore(51))
                .as("Age 51 should return 50 points")
                .isEqualTo(50.0);
        
        assertThat(creditScoringService.calculateAgeScore(61))
                .as("Age 61 should return 30 points")
                .isEqualTo(30.0);
        
        // Test upper boundaries
        assertThat(creditScoringService.calculateAgeScore(24))
                .as("Age 24 should return 30 points")
                .isEqualTo(30.0);
        
        assertThat(creditScoringService.calculateAgeScore(29))
                .as("Age 29 should return 50 points")
                .isEqualTo(50.0);
        
        assertThat(creditScoringService.calculateAgeScore(50))
                .as("Age 50 should return 60 points")
                .isEqualTo(60.0);
        
        assertThat(creditScoringService.calculateAgeScore(60))
                .as("Age 60 should return 50 points")
                .isEqualTo(50.0);
        
        assertThat(creditScoringService.calculateAgeScore(70))
                .as("Age 70 should return 30 points")
                .isEqualTo(30.0);
    }

    /**
     * Test for Requirement 8.1-8.4: Existing Loans Score Boundary Values
     * 
     * Tests exact boundary values for existing loans count
     */
    @Test
    void calculateExistingLoansScore_atBoundaryValues_shouldReturnCorrectScores() {
        assertThat(creditScoringService.calculateExistingLoansScore(0))
                .as("0 existing loans should return 60 points")
                .isEqualTo(60.0);
        
        assertThat(creditScoringService.calculateExistingLoansScore(1))
                .as("1 existing loan should return 50 points")
                .isEqualTo(50.0);
        
        assertThat(creditScoringService.calculateExistingLoansScore(2))
                .as("2 existing loans should return 30 points")
                .isEqualTo(30.0);
        
        assertThat(creditScoringService.calculateExistingLoansScore(3))
                .as("3 existing loans should return 0 points")
                .isEqualTo(0.0);
        
        assertThat(creditScoringService.calculateExistingLoansScore(10))
                .as("10 existing loans should return 0 points")
                .isEqualTo(0.0);
    }

    /**
     * Test for Requirement 11.1-11.4: Residence Score Boundary Values
     * 
     * Tests exact boundary values for residence years
     */
    @Test
    void calculateResidenceScore_atBoundaryValues_shouldReturnCorrectScores() {
        // Test lower boundaries
        assertThat(creditScoringService.calculateResidenceScore(0))
                .as("0 residence years should return 10 points")
                .isEqualTo(10.0);
        
        assertThat(creditScoringService.calculateResidenceScore(1))
                .as("1 residence year should return 20 points")
                .isEqualTo(20.0);
        
        assertThat(creditScoringService.calculateResidenceScore(3))
                .as("3 residence years should return 30 points")
                .isEqualTo(30.0);
        
        assertThat(creditScoringService.calculateResidenceScore(5))
                .as("5 residence years should return 40 points")
                .isEqualTo(40.0);
        
        // Test upper boundaries
        assertThat(creditScoringService.calculateResidenceScore(2))
                .as("2 residence years should return 20 points")
                .isEqualTo(20.0);
        
        assertThat(creditScoringService.calculateResidenceScore(4))
                .as("4 residence years should return 30 points")
                .isEqualTo(30.0);
        
        assertThat(creditScoringService.calculateResidenceScore(10))
                .as("10 residence years should return 40 points")
                .isEqualTo(40.0);
    }

    /**
     * Test for Requirement 10.3-10.6: Banking Relationship Score Boundary Values
     * 
     * Tests exact boundary values for banking relationship duration
     */
    @Test
    void calculateBankingRelationshipScore_atBoundaryValues_shouldReturnCorrectScores() {
        String accountNumber = "ACC-12345678";
        AccountEntity account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));
        
        // Test 5 months (< 6 months) - should return 10 points
        account.setCreated(LocalDateTime.now().minusMonths(5));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("5 months relationship should return 10 points")
                .isEqualTo(10.0);
        
        // Test 6 months (>= 6 months) - should return 25 points
        account.setCreated(LocalDateTime.now().minusMonths(6));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("6 months relationship should return 25 points")
                .isEqualTo(25.0);
        
        // Test 11 months (< 12 months) - should return 25 points
        account.setCreated(LocalDateTime.now().minusMonths(11));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("11 months relationship should return 25 points")
                .isEqualTo(25.0);
        
        // Test 12 months (>= 12 months) - should return 40 points
        account.setCreated(LocalDateTime.now().minusMonths(12));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("12 months relationship should return 40 points")
                .isEqualTo(40.0);
        
        // Test 23 months (< 24 months) - should return 40 points
        account.setCreated(LocalDateTime.now().minusMonths(23));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("23 months relationship should return 40 points")
                .isEqualTo(40.0);
        
        // Test 24 months (>= 24 months) - should return 50 points
        account.setCreated(LocalDateTime.now().minusMonths(24));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("24 months relationship should return 50 points")
                .isEqualTo(50.0);
        
        // Test 36 months (>= 24 months) - should return 50 points
        account.setCreated(LocalDateTime.now().minusMonths(36));
        assertThat(creditScoringService.calculateBankingRelationshipScore(accountNumber))
                .as("36 months relationship should return 50 points")
                .isEqualTo(50.0);
    }
}
