package com.bank.simulator.service;

import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Banking Relationship Score calculation in CreditScoringService
 * 
 * Property 12: Banking Relationship Score Calculation
 * **Validates: Requirements 10.3, 10.4, 10.5, 10.6**
 * 
 * For any banking relationship duration in months, the calculated score SHALL match the specified bracket rules:
 * - months >= 24 → 50 points
 * - 12 <= months < 24 → 40 points
 * - 6 <= months < 12 → 25 points
 * - months < 6 → 10 points
 */
@DisplayName("Banking Relationship Score Calculation Tests")
class CreditScoringServiceBankingRelationshipTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private CreditScoringService creditScoringService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        creditScoringService = new CreditScoringService(accountRepository, transactionRepository);
    }

    @Test
    @DisplayName("Should return 50 points when banking relationship is 24 months or more")
    void shouldReturn50PointsWhenRelationship24MonthsOrMore() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(24);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Should return 50 points when banking relationship is more than 24 months")
    void shouldReturn50PointsWhenRelationshipMoreThan24Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(36);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Should return 40 points when banking relationship is exactly 12 months")
    void shouldReturn40PointsWhenRelationshipExactly12Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(12);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should return 40 points when banking relationship is between 12 and 23 months")
    void shouldReturn40PointsWhenRelationshipBetween12And23Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(18);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should return 40 points when banking relationship is exactly 23 months")
    void shouldReturn40PointsWhenRelationshipExactly23Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(23);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should return 25 points when banking relationship is exactly 6 months")
    void shouldReturn25PointsWhenRelationshipExactly6Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(6);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Should return 25 points when banking relationship is between 6 and 11 months")
    void shouldReturn25PointsWhenRelationshipBetween6And11Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(9);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Should return 25 points when banking relationship is exactly 11 months")
    void shouldReturn25PointsWhenRelationshipExactly11Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(11);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Should return 10 points when banking relationship is less than 6 months")
    void shouldReturn10PointsWhenRelationshipLessThan6Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now().minusMonths(3);
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should return 10 points when banking relationship is 0 months (new account)")
    void shouldReturn10PointsWhenRelationship0Months() {
        // Arrange
        String accountNumber = "ACC123456";
        LocalDateTime accountCreated = LocalDateTime.now();
        AccountEntity account = createAccountWithCreationDate(accountCreated);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should return 10 points when account creation date is null")
    void shouldReturn10PointsWhenAccountCreationDateIsNull() {
        // Arrange
        String accountNumber = "ACC123456";
        AccountEntity account = createAccountWithCreationDate(null);
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should return 10 points when account is not found")
    void shouldReturn10PointsWhenAccountNotFound() {
        // Arrange
        String accountNumber = "NONEXISTENT";
        
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        // Act
        double score = creditScoringService.calculateBankingRelationshipScore(accountNumber);

        // Assert
        assertThat(score).isEqualTo(10.0);
    }

    /**
     * Helper method to create an AccountEntity with a specific creation date
     */
    private AccountEntity createAccountWithCreationDate(LocalDateTime created) {
        AccountEntity account = new AccountEntity();
        account.setCreated(created);
        return account;
    }
}
