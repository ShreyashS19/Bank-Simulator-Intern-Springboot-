package com.bank.simulator.service;

import com.bank.simulator.dto.LoanApplicationRequest;
import com.bank.simulator.dto.LoanEligibilityResultDto;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for end-to-end loan eligibility advisory processing.
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
    void testEligibleLoanApplicationCreatesPendingBankReviewRecord() {
        LoanApplicationRequest request = createLoanRequest();
        request.setMonthlyIncome(BigDecimal.valueOf(100000));
        request.setEmploymentType("GOVERNMENT");
        request.setExistingEmi(BigDecimal.valueOf(10000));
        request.setCreditScore(850);
        request.setAge(35);
        request.setExistingLoans(0);
        request.setHasCollateral(true);
        request.setResidenceYears(10);
        request.setHasGuarantor(true);
        request.setRepaymentHistory("CLEAN");

        LoanEligibilityResultDto response = loanService.applyForLoan(request, testAccountNumber);

        assertNotNull(response);
        assertNotNull(response.getReferenceNumber());
        assertTrue(response.getReferenceNumber().matches("LN-\\d{4}-[A-Z0-9]{6}"));
        assertEquals("ELIGIBLE", response.getEligibilityStatus());
        assertEquals("/loan/pdf/" + response.getReferenceNumber(), response.getPdfDownloadPath());
        assertEquals("testuser@example.com", response.getCustomerEmail());

        var savedLoan = loanRepository.findByReferenceNumber(response.getReferenceNumber());
        assertTrue(savedLoan.isPresent(), "Loan should be persisted in database");
        assertEquals("PENDING_BANK_REVIEW", savedLoan.get().getStatus());
        assertEquals("ELIGIBLE", savedLoan.get().getEligibilityStatus());
    }

    @Test
    void testNotEligibleLoanApplicationCreatesPendingBankReviewRecord() {
        LoanApplicationRequest request = createLoanRequest();
        request.setMonthlyIncome(BigDecimal.valueOf(15000));
        request.setEmploymentType("UNEMPLOYED");
        request.setExistingEmi(BigDecimal.valueOf(10000));
        request.setCreditScore(350);
        request.setAge(65);
        request.setExistingLoans(5);
        request.setHasCollateral(false);
        request.setResidenceYears(0);
        request.setHasGuarantor(false);
        request.setRepaymentHistory("NOT_CLEAN");

        LoanEligibilityResultDto response = loanService.applyForLoan(request, testAccountNumber);

        assertNotNull(response);
        assertEquals("NOT_ELIGIBLE", response.getEligibilityStatus());
        assertTrue(response.getEligibilityScore().doubleValue() < 65.0);

        var savedLoan = loanRepository.findByReferenceNumber(response.getReferenceNumber());
        assertTrue(savedLoan.isPresent(), "Loan should be persisted in database");
        assertEquals("PENDING_BANK_REVIEW", savedLoan.get().getStatus());
        assertEquals("NOT_ELIGIBLE", savedLoan.get().getEligibilityStatus());
    }

    @Test
    void testRequiredDocumentsAndSpecialNotesArePresent() {
        LoanApplicationRequest request = createLoanRequest();
        request.setLoanPurpose("HOME");

        LoanEligibilityResultDto response = loanService.applyForLoan(request, testAccountNumber);

        assertNotNull(response.getRequiredDocuments());
        assertTrue(response.getRequiredDocuments().size() >= 5);
        assertTrue(response.getRequiredDocuments().stream().anyMatch(doc -> doc.contains("Aadhaar")));
        assertTrue(response.getRequiredDocuments().stream().anyMatch(doc -> doc.contains("Property")));

        assertNotNull(response.getSpecialNotes());
        assertFalse(response.getSpecialNotes().isEmpty());
        assertTrue(response.getSpecialNotes().stream().anyMatch(note -> note.contains("PRELIMINARY")));
    }

    @Test
    void testReferenceNumberUniqueness() {
        LoanApplicationRequest request1 = createLoanRequest();
        LoanApplicationRequest request2 = createLoanRequest();

        LoanEligibilityResultDto response1 = loanService.applyForLoan(request1, testAccountNumber);
        LoanEligibilityResultDto response2 = loanService.applyForLoan(request2, testAccountNumber);

        assertNotEquals(response1.getReferenceNumber(), response2.getReferenceNumber(),
                "Reference numbers should be unique");
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
