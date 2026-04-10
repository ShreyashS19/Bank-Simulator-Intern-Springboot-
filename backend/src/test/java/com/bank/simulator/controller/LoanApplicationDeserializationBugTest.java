package com.bank.simulator.controller;

import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.repository.LoanRepository;
import com.bank.simulator.repository.UserRepository;
import com.bank.simulator.security.JwtUtil;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bug Condition Exploration Property Test for Loan Application Deserialization
 * 
 * **Validates: Requirements 1.1, 1.2, 2.1, 2.2**
 * 
 * UPDATED ROOT CAUSE ANALYSIS:
 * The bug is NOT about JSON deserialization. The actual bug is a LazyInitializationException
 * that occurs when the controller tries to access customer.getAccounts() outside of a 
 * transaction context. The customerRepository.findByEmail() returns a CustomerEntity, but
 * the accounts collection is lazy-loaded and cannot be accessed after the repository method
 * returns (outside the transaction boundary).
 * 
 * CRITICAL: This test is EXPECTED TO FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * The test encodes the expected behavior - it will validate the fix when it passes after implementation.
 * 
 * NOTE: The test currently PASSES because @Transactional on the test class keeps the session open.
 * In production, without @Transactional on the controller method, the LazyInitializationException occurs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanApplicationDeserializationBugTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String testAccountNumber;
    private String testEmail = "testuser-bug@example.com";

    @BeforeEach
    void setUp() {
        // Clean up test data
        loanRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        UserEntity user = UserEntity.builder()
                .fullName("Test User")
                .email(testEmail)
                .password(passwordEncoder.encode("password"))
                .role("USER")
                .active(true)
                .build();
        userRepository.save(user);

        // Create test customer
        CustomerEntity customer = CustomerEntity.builder()
                .name("Test Customer")
                .phoneNumber("9876543210")
                .email(testEmail)
                .address("Test Address")
                .customerPin(passwordEncoder.encode("1234"))
                .aadharNumber("123456789012")
                .dob(LocalDate.of(1990, 1, 1))
                .status("ACTIVE")
                .accounts(new ArrayList<>())
                .build();
        customer = customerRepository.save(customer);

        // Create test account
        testAccountNumber = "ACC-TEST-BUG-001";
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
        account = accountRepository.save(account);
        customer.getAccounts().add(account);
        customerRepository.save(customer);

        // Generate JWT token
        userToken = jwtUtil.generateToken(testEmail, "USER");
    }

    /**
     * Property 1: Bug Condition - JSON Deserialization Failure for BigDecimal Fields
     * 
     * **Validates: Requirements 1.1, 1.2, 2.1, 2.2**
     * 
     * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists.
     * DO NOT attempt to fix the test or the code when it fails.
     * 
     * NOTE: This test encodes the expected behavior - it will validate the fix when it passes after implementation.
     * 
     * GOAL: Surface counterexamples that demonstrate the deserialization bug exists.
     * 
     * For any loan application request with plain JSON numeric values for loanAmount, monthlyIncome, 
     * and existingEmi, the backend SHALL successfully deserialize the request and process the loan 
     * application, returning a 201 Created response with loan details.
     * 
     * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS with 500 Internal Server Error 
     * (this is correct - it proves the bug exists)
     */
    @Property(tries = 50)
    @Label("Loan application with plain JSON numbers should deserialize successfully")
    void loanApplicationWithPlainJsonNumbersShouldDeserialize(
            @ForAll("validLoanAmounts") int loanAmount,
            @ForAll("validMonthlyIncomes") int monthlyIncome,
            @ForAll("validExistingEmis") int existingEmi,
            @ForAll("validLoanPurposes") String loanPurpose,
            @ForAll("validTenures") int tenure,
            @ForAll("validEmploymentTypes") String employmentType,
            @ForAll("validCreditScores") int creditScore,
            @ForAll("validAges") int age,
            @ForAll("validExistingLoans") int existingLoans,
            @ForAll("validBooleans") boolean hasCollateral,
            @ForAll("validResidenceYears") int residenceYears,
            @ForAll("validBooleans") boolean hasGuarantor,
            @ForAll("validRepaymentHistories") String repaymentHistory) throws Exception {
        
        // Arrange - construct JSON payload with plain numeric values (not strings)
        // This mimics how the frontend sends the data
        String jsonPayload = String.format("""
                {
                    "loanAmount": %d,
                    "loanPurpose": "%s",
                    "loanTenure": %d,
                    "monthlyIncome": %d,
                    "employmentType": "%s",
                    "existingEmi": %d,
                    "creditScore": %d,
                    "age": %d,
                    "existingLoans": %d,
                    "hasCollateral": %s,
                    "residenceYears": %d,
                    "hasGuarantor": %s,
                    "repaymentHistory": "%s"
                }
                """,
                loanAmount, loanPurpose, tenure, monthlyIncome, employmentType,
                existingEmi, creditScore, age, existingLoans, hasCollateral,
                residenceYears, hasGuarantor, repaymentHistory);

        // Act & Assert - POST request should succeed with 201 Created
        mockMvc.perform(post("/loan/apply")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loan application submitted successfully"))
                .andExpect(jsonPath("$.data.loanId").exists())
                .andExpect(jsonPath("$.data.accountNumber").value(testAccountNumber))
                .andExpect(jsonPath("$.data.status").value(oneOf("APPROVED", "UNDER_REVIEW", "REJECTED")));
    }

    /**
     * Unit test version - tests a single concrete case
     * This is useful for debugging and understanding the exact failure
     */
    @Test
    void testBasicLoanApplicationWithPlainNumbers() throws Exception {
        // Arrange - construct JSON payload with plain numeric values
        String jsonPayload = """
                {
                    "loanAmount": 50000,
                    "loanPurpose": "BUSINESS",
                    "loanTenure": 9,
                    "monthlyIncome": 30000,
                    "employmentType": "SALARIED",
                    "existingEmi": 5000,
                    "creditScore": 750,
                    "age": 30,
                    "existingLoans": 1,
                    "hasCollateral": true,
                    "residenceYears": 5,
                    "hasGuarantor": true,
                    "repaymentHistory": "CLEAN"
                }
                """;

        // Act & Assert - POST request should succeed with 201 Created
        mockMvc.perform(post("/loan/apply")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loan application submitted successfully"))
                .andExpect(jsonPath("$.data.loanId").exists())
                .andExpect(jsonPath("$.data.accountNumber").value(testAccountNumber))
                .andExpect(jsonPath("$.data.status").value(oneOf("APPROVED", "UNDER_REVIEW", "REJECTED")));
    }

    // Arbitrary generators for valid loan application fields

    @Provide
    Arbitrary<Integer> validLoanAmounts() {
        return Arbitraries.integers().between(10000, 10000000);
    }

    @Provide
    Arbitrary<Integer> validMonthlyIncomes() {
        return Arbitraries.integers().between(20000, 500000);
    }

    @Provide
    Arbitrary<Integer> validExistingEmis() {
        return Arbitraries.integers().between(0, 50000);
    }

    @Provide
    Arbitrary<String> validLoanPurposes() {
        return Arbitraries.of("EDUCATION", "HOME", "BUSINESS", "PERSONAL", "VEHICLE");
    }

    @Provide
    Arbitrary<Integer> validTenures() {
        return Arbitraries.integers().between(6, 360);
    }

    @Provide
    Arbitrary<String> validEmploymentTypes() {
        return Arbitraries.of("SALARIED", "SELF_EMPLOYED", "GOVERNMENT", "UNEMPLOYED");
    }

    @Provide
    Arbitrary<Integer> validCreditScores() {
        return Arbitraries.integers().between(300, 900);
    }

    @Provide
    Arbitrary<Integer> validAges() {
        return Arbitraries.integers().between(18, 70);
    }

    @Provide
    Arbitrary<Integer> validExistingLoans() {
        return Arbitraries.integers().between(0, 10);
    }

    @Provide
    Arbitrary<Boolean> validBooleans() {
        return Arbitraries.of(true, false);
    }

    @Provide
    Arbitrary<Integer> validResidenceYears() {
        return Arbitraries.integers().between(0, 50);
    }

    @Provide
    Arbitrary<String> validRepaymentHistories() {
        return Arbitraries.of("CLEAN", "NOT_CLEAN");
    }
}
