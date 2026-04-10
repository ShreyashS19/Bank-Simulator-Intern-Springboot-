package com.bank.simulator.service;

import com.bank.simulator.dto.LoanApplicationRequest;
import com.bank.simulator.dto.LoanResponse;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.entity.TransactionEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.repository.LoanRepository;
import com.bank.simulator.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for end-to-end loan application processing
 * 
 * Tests complete flow from request to database persistence with real repositories
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
@Transactional
public class LoanApplicationIntegrationTest {

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

    private String testAccountNumber;

    @BeforeEach
    void setUp() {
        // Create a test customer first
        CustomerEntity customer = CustomerEntity.builder()
                .name("Test User")
                .email("testuser@example.com")
                .phoneNumber("9876543210")
                .dob(LocalDate.of(1990, 1, 1))
                .address("123 Test Street, Test City, Test State - 123456")
                .aadharNumber("123456789012")
                .customerPin("$2a$10$dummyHashedPin")
                .status("ACTIVE")
                .build();
        CustomerEntity savedCustomer = customerRepository.save(customer);

        // Create a test account with sufficient history
        AccountEntity account = AccountEntity.builder()
                .customer(savedCustomer)
                .accountNumber("ACC-TEST-12345678")
                .aadharNumber("123456789012")
                .amount(BigDecimal.valueOf(100000))
                .status("ACTIVE")
                .ifscCode("BANK0001234")
                .phoneNumberLinked("9876543210")
                .bankName("Test Bank")
                .nameOnAccount("Test User")
                .build();
        
        // Manually set created timestamp for testing banking relationship score
        AccountEntity savedAccount = accountRepository.save(account);
        // Note: We can't easily override @CreationTimestamp, so banking relationship score will be low
        testAccountNumber = savedAccount.getAccountNumber();

        // Create some test transactions for transaction pattern analysis
        for (int i = 0; i < 10; i++) {
            TransactionEntity transaction = new TransactionEntity();
            transaction.setTransactionId("TXN-TEST-" + i);
            transaction.setAccount(savedAccount); // Changed from setAccountId
            transaction.setSenderAccountNumber(testAccountNumber);
            transaction.setReceiverAccountNumber("ACC-RECEIVER-" + i);
            transaction.setAmount(BigDecimal.valueOf(1000 + i * 100));
            transaction.setTransactionType("CREDIT");
            transaction.setStatus("SUCCESS");
            // Note: createdDate is auto-set by @CreationTimestamp, but we can't override it easily
            // For this test, we'll accept that all transactions have recent timestamps
            transactionRepository.save(transaction);
        }
    }

    @Test
    void testApprovedLoanApplication() {
        // Given: A loan application with excellent credit profile
        LoanApplicationRequest request = createLoanRequest();
        request.setMonthlyIncome(BigDecimal.valueOf(100000)); // High income
        request.setEmploymentType("GOVERNMENT"); // Stable employment
        request.setExistingEmi(BigDecimal.valueOf(10000)); // Low DTI (0.10)
        request.setCreditScore(850); // Excellent credit score
        request.setAge(35); // Optimal age
        request.setExistingLoans(0); // No existing loans
        request.setHasCollateral(true);
        request.setResidenceYears(10);
        request.setHasGuarantor(true);
        request.setRepaymentHistory("CLEAN");

        // When: Applying for loan
        LoanResponse response = loanService.applyForLoan(request, testAccountNumber);

        // Then: Loan should be approved
        assertNotNull(response);
        assertNotNull(response.getLoanId());
        assertTrue(response.getLoanId().startsWith("LOAN-"));
        assertEquals("APPROVED", response.getStatus());
        assertEquals(testAccountNumber, response.getAccountNumber());
        
        // Verify eligibility score is high
        assertTrue(response.getEligibilityScore().doubleValue() >= 75.0,
                "Expected eligibility score >= 75.0 for approved loan");
        
        // Verify interest rate is assigned
        assertTrue(response.getInterestRate().compareTo(BigDecimal.ZERO) > 0,
                "Expected positive interest rate for approved loan");
        
        // Verify EMI is calculated
        assertTrue(response.getEmi().compareTo(BigDecimal.ZERO) > 0,
                "Expected positive EMI for approved loan");
        
        // Verify no rejection reason
        assertTrue(response.getRejectionReason() == null || response.getRejectionReason().isEmpty());
        
        // Verify no improvement tips for approved loan
        assertTrue(response.getImprovementTips().isEmpty(),
                "Expected no improvement tips for approved loan");
        
        // Verify factor scores are populated
        assertNotNull(response.getFactorScores());
        assertTrue(response.getFactorScores().getIncomeScore() > 0);
        assertTrue(response.getFactorScores().getEmploymentScore() > 0);
        assertTrue(response.getFactorScores().getDtiScore() > 0);
        assertTrue(response.getFactorScores().getBankingRelationshipScore() > 0);
        
        // Verify loan is persisted in database
        var savedLoan = loanRepository.findByLoanId(response.getLoanId());
        assertTrue(savedLoan.isPresent(), "Loan should be persisted in database");
        assertEquals(response.getStatus(), savedLoan.get().getStatus());
    }

    @Test
    void testRejectedLoanApplication() {
        // Given: A loan application with poor credit profile
        LoanApplicationRequest request = createLoanRequest();
        request.setMonthlyIncome(BigDecimal.valueOf(15000)); // Low income
        request.setEmploymentType("UNEMPLOYED"); // No employment
        request.setExistingEmi(BigDecimal.valueOf(10000)); // High DTI (0.67)
        request.setCreditScore(350); // Poor credit score
        request.setAge(65); // Higher age
        request.setExistingLoans(5); // Many existing loans
        request.setHasCollateral(false);
        request.setResidenceYears(0);
        request.setHasGuarantor(false);
        request.setRepaymentHistory("NOT_CLEAN");

        // When: Applying for loan
        LoanResponse response = loanService.applyForLoan(request, testAccountNumber);

        // Then: Loan should be rejected
        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        
        // Verify eligibility score is low
        assertTrue(response.getEligibilityScore().doubleValue() < 65.0,
                "Expected eligibility score < 65.0 for rejected loan");
        
        // Verify interest rate is 0 for rejected loan
        assertEquals(BigDecimal.ZERO, response.getInterestRate());
        
        // Verify EMI is 0 for rejected loan
        assertEquals(BigDecimal.ZERO, response.getEmi());
        
        // Verify rejection reason is provided
        assertNotNull(response.getRejectionReason());
        assertFalse(response.getRejectionReason().isEmpty());
        
        // Verify improvement tips are provided
        assertFalse(response.getImprovementTips().isEmpty(),
                "Expected improvement tips for rejected loan");
        
        // Verify loan is persisted in database
        var savedLoan = loanRepository.findByLoanId(response.getLoanId());
        assertTrue(savedLoan.isPresent(), "Loan should be persisted in database");
    }

    @Test
    void testUnderReviewLoanApplication() {
        // Given: A loan application with moderate credit profile
        LoanApplicationRequest request = createLoanRequest();
        request.setMonthlyIncome(BigDecimal.valueOf(50000)); // Moderate income
        request.setEmploymentType("SALARIED"); // Stable employment
        request.setExistingEmi(BigDecimal.valueOf(22000)); // Moderate DTI (0.44)
        request.setCreditScore(680); // Good credit score
        request.setAge(28); // Younger age
        request.setExistingLoans(2); // Some existing loans
        request.setHasCollateral(false);
        request.setResidenceYears(2);
        request.setHasGuarantor(false);
        request.setRepaymentHistory("CLEAN");

        // When: Applying for loan
        LoanResponse response = loanService.applyForLoan(request, testAccountNumber);

        // Then: Loan should be under review
        assertNotNull(response);
        assertEquals("UNDER_REVIEW", response.getStatus());
        
        // Verify eligibility score is moderate
        double eligibilityScore = response.getEligibilityScore().doubleValue();
        assertTrue(eligibilityScore >= 65.0 && eligibilityScore < 75.0 || 
                   response.getDtiRatio().doubleValue() >= 0.40,
                "Expected moderate eligibility score or high DTI for under review loan");
        
        // Verify interest rate is assigned
        assertTrue(response.getInterestRate().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify EMI is calculated
        assertTrue(response.getEmi().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify improvement tips may be provided
        // (Under review loans may have some weak factors)
        
        // Verify loan is persisted in database
        var savedLoan = loanRepository.findByLoanId(response.getLoanId());
        assertTrue(savedLoan.isPresent(), "Loan should be persisted in database");
    }

    @Test
    void testAllFactorScoresAreCalculated() {
        // Given: A loan application
        LoanApplicationRequest request = createLoanRequest();

        // When: Applying for loan
        LoanResponse response = loanService.applyForLoan(request, testAccountNumber);

        // Then: All 11 factor scores should be calculated and non-null
        assertNotNull(response.getFactorScores());
        assertNotNull(response.getFactorScores().getIncomeScore());
        assertNotNull(response.getFactorScores().getEmploymentScore());
        assertNotNull(response.getFactorScores().getDtiScore());
        assertNotNull(response.getFactorScores().getRepaymentHistoryScore());
        assertNotNull(response.getFactorScores().getAgeScore());
        assertNotNull(response.getFactorScores().getExistingLoansScore());
        assertNotNull(response.getFactorScores().getCollateralScore());
        assertNotNull(response.getFactorScores().getBankingRelationshipScore());
        assertNotNull(response.getFactorScores().getResidenceScore());
        assertNotNull(response.getFactorScores().getLoanPurposeScore());
        assertNotNull(response.getFactorScores().getGuarantorScore());
        
        // Verify all scores are within valid ranges
        assertTrue(response.getFactorScores().getIncomeScore() >= 0 && 
                   response.getFactorScores().getIncomeScore() <= 120);
        assertTrue(response.getFactorScores().getEmploymentScore() >= 0 && 
                   response.getFactorScores().getEmploymentScore() <= 80);
        assertTrue(response.getFactorScores().getDtiScore() >= 0 && 
                   response.getFactorScores().getDtiScore() <= 100);
    }

    @Test
    void testLoanIdUniqueness() {
        // Given: Two loan applications
        LoanApplicationRequest request1 = createLoanRequest();
        LoanApplicationRequest request2 = createLoanRequest();

        // When: Applying for both loans
        LoanResponse response1 = loanService.applyForLoan(request1, testAccountNumber);
        LoanResponse response2 = loanService.applyForLoan(request2, testAccountNumber);

        // Then: Loan IDs should be unique
        assertNotEquals(response1.getLoanId(), response2.getLoanId(),
                "Loan IDs should be unique");
    }

    private LoanApplicationRequest createLoanRequest() {
        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setLoanAmount(BigDecimal.valueOf(500000));
        request.setLoanPurpose("HOME");
        request.setLoanTenure(240);
        request.setMonthlyIncome(BigDecimal.valueOf(60000));
        request.setEmploymentType("SALARIED");
        request.setExistingEmi(BigDecimal.valueOf(15000));
        request.setCreditScore(720);
        request.setAge(35);
        request.setExistingLoans(1);
        request.setHasCollateral(true);
        request.setResidenceYears(5);
        request.setHasGuarantor(false);
        request.setRepaymentHistory("CLEAN");
        return request;
    }
}
