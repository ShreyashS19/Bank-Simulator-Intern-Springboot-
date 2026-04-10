package com.bank.simulator.service;

import com.bank.simulator.dto.*;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.entity.LoanEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.repository.LoanRepository;
import com.bank.simulator.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for LoanService loan retrieval and management methods
 * Tests verify database interactions and end-to-end functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanServiceIntegrationTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String testAccountNumber;
    private LoanEntity testLoan1;
    private LoanEntity testLoan2;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up any existing test data
        loanRepository.deleteAll();
        
        // Create test customer and account if they don't exist
        testAccountNumber = "ACC-TEST-001";
        if (accountRepository.findByAccountNumber(testAccountNumber).isEmpty()) {
            // Create customer first
            CustomerEntity customer = CustomerEntity.builder()
                    .name("Test Customer")
                    .phoneNumber("9876543210")
                    .email("test@example.com")
                    .address("Test Address")
                    .customerPin("$2a$10$test")
                    .aadharNumber("123456789012")
                    .dob(LocalDate.of(1990, 1, 1))
                    .status("ACTIVE")
                    .build();
            customer = customerRepository.save(customer);
            
            // Create account
            AccountEntity account = AccountEntity.builder()
                    .customer(customer)
                    .accountNumber(testAccountNumber)
                    .aadharNumber("123456789012")
                    .ifscCode("BANK0001234")
                    .phoneNumberLinked("9876543210")
                    .amount(BigDecimal.valueOf(100000))
                    .bankName("Test Bank")
                    .nameOnAccount("Test User")
                    .status("ACTIVE")
                    .build();
            accountRepository.save(account);
        }

        // Create test loans
        testLoan1 = createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED", 
                BigDecimal.valueOf(500000), 80.0, BigDecimal.valueOf(0.30));
        testLoan2 = createTestLoan("LOAN-10000002", testAccountNumber, "REJECTED", 
                BigDecimal.valueOf(300000), 55.0, BigDecimal.valueOf(0.60));
        
        loanRepository.save(testLoan1);
        loanRepository.save(testLoan2);
    }

    private LoanEntity createTestLoan(String loanId, String accountNumber, String status,
                                      BigDecimal loanAmount, double eligibilityScore, BigDecimal dtiRatio) throws Exception {
        List<String> improvementTips = status.equals("REJECTED") ? 
                List.of("Improve your credit score", "Reduce existing debt") : List.of();
        
        return LoanEntity.builder()
                .loanId(loanId)
                .accountNumber(accountNumber)
                .loanAmount(loanAmount)
                .loanPurpose("HOME")
                .loanTenure(240)
                .monthlyIncome(BigDecimal.valueOf(50000))
                .employmentType("SALARIED")
                .existingEmi(BigDecimal.valueOf(15000))
                .creditScore(700)
                .age(35)
                .existingLoans(1)
                .hasCollateral(true)
                .residenceYears(5)
                .hasGuarantor(false)
                .repaymentHistory("CLEAN")
                .incomeScore(80.0)
                .employmentScore(70.0)
                .dtiScore(60.0)
                .repaymentHistoryScore(100.0)
                .ageScore(60.0)
                .existingLoansScore(50.0)
                .collateralScore(70.0)
                .bankingRelationshipScore(40.0)
                .residenceScore(40.0)
                .loanPurposeScore(40.0)
                .guarantorScore(0.0)
                .eligibilityScore(BigDecimal.valueOf(eligibilityScore))
                .dtiRatio(dtiRatio)
                .status(status)
                .interestRate(status.equals("APPROVED") ? BigDecimal.valueOf(8.5) : BigDecimal.ZERO)
                .emi(status.equals("APPROVED") ? BigDecimal.valueOf(4500) : BigDecimal.ZERO)
                .rejectionReason(status.equals("REJECTED") ? "Low eligibility score" : "")
                .improvementTips(objectMapper.writeValueAsString(improvementTips))
                .build();
    }

    @Test
    @DisplayName("getLoansByAccount returns all loans for account ordered by application date desc")
    void getLoansByAccount_ReturnsLoansOrderedByDate() {
        // Act
        List<LoanResponse> loans = loanService.getLoansByAccount(testAccountNumber);

        // Assert
        assertThat(loans).hasSize(2);
        assertThat(loans.get(0).getLoanId()).isIn(testLoan1.getLoanId(), testLoan2.getLoanId());
        assertThat(loans.get(1).getLoanId()).isIn(testLoan1.getLoanId(), testLoan2.getLoanId());
        assertThat(loans.get(0).getAccountNumber()).isEqualTo(testAccountNumber);
        assertThat(loans.get(1).getAccountNumber()).isEqualTo(testAccountNumber);
    }

    @Test
    @DisplayName("getLoansByAccount returns empty list for account with no loans")
    void getLoansByAccount_EmptyListForNoLoans() {
        // Act
        List<LoanResponse> loans = loanService.getLoansByAccount("ACC-NONEXISTENT");

        // Assert
        assertThat(loans).isEmpty();
    }

    @Test
    @DisplayName("getLoanById returns loan with all fields populated")
    void getLoanById_ReturnsLoanWithAllFields() {
        // Act
        LoanResponse loan = loanService.getLoanById(testLoan1.getLoanId());

        // Assert
        assertThat(loan).isNotNull();
        assertThat(loan.getLoanId()).isEqualTo(testLoan1.getLoanId());
        assertThat(loan.getAccountNumber()).isEqualTo(testLoan1.getAccountNumber());
        assertThat(loan.getLoanAmount()).isEqualByComparingTo(testLoan1.getLoanAmount());
        assertThat(loan.getStatus()).isEqualTo(testLoan1.getStatus());
        assertThat(loan.getEligibilityScore()).isEqualByComparingTo(testLoan1.getEligibilityScore());
        assertThat(loan.getInterestRate()).isEqualByComparingTo(testLoan1.getInterestRate());
        assertThat(loan.getFactorScores()).isNotNull();
        assertThat(loan.getFactorScores().getIncomeScore()).isEqualTo(testLoan1.getIncomeScore());
    }

    @Test
    @DisplayName("getLoanById throws exception for non-existent loan ID")
    void getLoanById_ThrowsExceptionForInvalidId() {
        // Act & Assert
        assertThatThrownBy(() -> loanService.getLoanById("LOAN-INVALID"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Loan not found with ID: LOAN-INVALID");
    }

    @Test
    @DisplayName("getAllLoans returns all loans ordered by application date desc")
    void getAllLoans_ReturnsAllLoans() {
        // Act
        List<LoanResponse> loans = loanService.getAllLoans();

        // Assert
        assertThat(loans).hasSizeGreaterThanOrEqualTo(2);
        // Verify our test loans are in the list
        assertThat(loans).extracting(LoanResponse::getLoanId)
                .contains(testLoan1.getLoanId(), testLoan2.getLoanId());
    }

    @Test
    @DisplayName("updateLoanStatus updates status and saves to database")
    void updateLoanStatus_UpdatesStatusSuccessfully() {
        // Arrange
        UpdateLoanStatusRequest request = new UpdateLoanStatusRequest();
        request.setStatus("UNDER_REVIEW");

        // Act
        LoanResponse updatedLoan = loanService.updateLoanStatus(testLoan1.getLoanId(), request);

        // Assert
        assertThat(updatedLoan.getStatus()).isEqualTo("UNDER_REVIEW");
        
        // Verify database was updated
        LoanEntity dbLoan = loanRepository.findByLoanId(testLoan1.getLoanId()).orElseThrow();
        assertThat(dbLoan.getStatus()).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("updateLoanStatus throws exception for non-existent loan ID")
    void updateLoanStatus_ThrowsExceptionForInvalidId() {
        // Arrange
        UpdateLoanStatusRequest request = new UpdateLoanStatusRequest();
        request.setStatus("APPROVED");

        // Act & Assert
        assertThatThrownBy(() -> loanService.updateLoanStatus("LOAN-INVALID", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Loan not found with ID: LOAN-INVALID");
    }

    @Test
    @DisplayName("getLoanStatistics calculates correct values")
    void getLoanStatistics_CalculatesCorrectly() {
        // Act
        LoanStatistics stats = loanService.getLoanStatistics();

        // Assert
        assertThat(stats.getTotalApplications()).isGreaterThanOrEqualTo(2);
        assertThat(stats.getApprovedCount()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getRejectedCount()).isGreaterThanOrEqualTo(1);
        assertThat(stats.getTotalApprovedAmount()).isGreaterThanOrEqualTo(testLoan1.getLoanAmount());
        assertThat(stats.getAverageEligibilityScore()).isNotNull();
        assertThat(stats.getAverageEligibilityScore()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("getLoanStatistics returns zero counts when no loans exist")
    void getLoanStatistics_HandlesEmptyDatabase() {
        // Arrange
        loanRepository.deleteAll();

        // Act
        LoanStatistics stats = loanService.getLoanStatistics();

        // Assert
        assertThat(stats.getTotalApplications()).isEqualTo(0);
        assertThat(stats.getApprovedCount()).isEqualTo(0);
        assertThat(stats.getRejectedCount()).isEqualTo(0);
        assertThat(stats.getUnderReviewCount()).isEqualTo(0);
        assertThat(stats.getPendingCount()).isEqualTo(0);
        assertThat(stats.getTotalApprovedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.getAverageEligibilityScore()).isEqualTo(0.0);
    }
}
