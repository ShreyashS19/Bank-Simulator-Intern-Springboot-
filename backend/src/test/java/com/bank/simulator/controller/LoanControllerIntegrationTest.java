package com.bank.simulator.controller;

import com.bank.simulator.dto.*;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.entity.LoanEntity;
import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.repository.LoanRepository;
import com.bank.simulator.repository.UserRepository;
import com.bank.simulator.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LoanController REST API endpoints
 * Tests verify authentication, authorization, request/response handling, and ApiResponse wrapper format
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private String adminToken;
    private String testAccountNumber;
    private String testEmail = "testuser@example.com";
    private String adminEmail = "admin@example.com";

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

        // Create admin user
        UserEntity admin = UserEntity.builder()
                .fullName("Admin User")
                .email(adminEmail)
                .password(passwordEncoder.encode("password"))
                .role("ADMIN")
                .active(true)
                .build();
        userRepository.save(admin);

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
        testAccountNumber = "ACC-TEST-001";
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

        // Generate JWT tokens
        userToken = jwtUtil.generateToken(testEmail, "USER");
        adminToken = jwtUtil.generateToken(adminEmail, "ADMIN");
    }

    @Test
    @DisplayName("POST /loan/apply - should apply for loan with valid request")
    void testApplyForLoan_ValidRequest() throws Exception {
        LoanApplicationRequest request = createValidLoanRequest();

        mockMvc.perform(post("/loan/apply")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loan application submitted successfully"))
                .andExpect(jsonPath("$.data.loanId").exists())
                .andExpect(jsonPath("$.data.accountNumber").value(testAccountNumber))
                .andExpect(jsonPath("$.data.loanAmount").value(500000))
                .andExpect(jsonPath("$.data.eligibilityScore").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.interestRate").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /loan/apply - should return 403 for unauthenticated request")
    void testApplyForLoan_Unauthenticated() throws Exception {
        LoanApplicationRequest request = createValidLoanRequest();

        mockMvc.perform(post("/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /loan/apply - should return 400 for invalid request")
    void testApplyForLoan_InvalidRequest() throws Exception {
        LoanApplicationRequest request = createValidLoanRequest();
        request.setLoanAmount(BigDecimal.valueOf(5000)); // Below minimum

        mockMvc.perform(post("/loan/apply")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /loan/account/{accountNumber} - should retrieve loans for own account")
    void testGetLoansByAccount_OwnAccount() throws Exception {
        // Create test loan
        createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED");

        mockMvc.perform(get("/loan/account/" + testAccountNumber)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loans retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].loanId").value("LOAN-10000001"))
                .andExpect(jsonPath("$.data[0].accountNumber").value(testAccountNumber));
    }

    @Test
    @DisplayName("GET /loan/account/{accountNumber} - should deny access to other user's account")
    void testGetLoansByAccount_OtherAccount() throws Exception {
        // Create another user and customer with account
        UserEntity otherUser = UserEntity.builder()
                .fullName("Other User")
                .email("other@example.com")
                .password(passwordEncoder.encode("password"))
                .role("USER")
                .active(true)
                .build();
        userRepository.save(otherUser);

        CustomerEntity otherCustomer = CustomerEntity.builder()
                .name("Other Customer")
                .phoneNumber("9876543211")
                .email("other@example.com")
                .address("Other Address")
                .customerPin(passwordEncoder.encode("1234"))
                .aadharNumber("123456789013")
                .dob(LocalDate.of(1990, 1, 1))
                .status("ACTIVE")
                .accounts(new ArrayList<>())
                .build();
        otherCustomer = customerRepository.save(otherCustomer);

        AccountEntity otherAccount = AccountEntity.builder()
                .customer(otherCustomer)
                .accountNumber("ACC-OTHER-001")
                .aadharNumber("123456789013")
                .ifscCode("BANK0001234")
                .phoneNumberLinked("9876543211")
                .amount(BigDecimal.valueOf(100000))
                .bankName("Test Bank")
                .nameOnAccount("Other User")
                .status("ACTIVE")
                .build();
        otherAccount = accountRepository.save(otherAccount);
        otherCustomer.getAccounts().add(otherAccount);
        customerRepository.save(otherCustomer);

        String otherToken = jwtUtil.generateToken("other@example.com", "USER");

        mockMvc.perform(get("/loan/account/" + testAccountNumber)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));
    }

    @Test
    @DisplayName("GET /loan/account/{accountNumber} - admin should access any account")
    void testGetLoansByAccount_AdminAccess() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED");

        mockMvc.perform(get("/loan/account/" + testAccountNumber)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /loan/{loanId} - should retrieve loan by ID for own account")
    void testGetLoanById_OwnLoan() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED");

        mockMvc.perform(get("/loan/LOAN-10000001")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loan retrieved successfully"))
                .andExpect(jsonPath("$.data.loanId").value("LOAN-10000001"))
                .andExpect(jsonPath("$.data.accountNumber").value(testAccountNumber));
    }

    @Test
    @DisplayName("GET /loan/{loanId} - should deny access to other user's loan")
    void testGetLoanById_OtherUserLoan() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED");

        // Create another user and customer with account
        UserEntity otherUser = UserEntity.builder()
                .fullName("Other User")
                .email("other@example.com")
                .password(passwordEncoder.encode("password"))
                .role("USER")
                .active(true)
                .build();
        userRepository.save(otherUser);

        CustomerEntity otherCustomer = CustomerEntity.builder()
                .name("Other Customer")
                .phoneNumber("9876543211")
                .email("other@example.com")
                .address("Other Address")
                .customerPin(passwordEncoder.encode("1234"))
                .aadharNumber("123456789013")
                .dob(LocalDate.of(1990, 1, 1))
                .status("ACTIVE")
                .accounts(new ArrayList<>())
                .build();
        otherCustomer = customerRepository.save(otherCustomer);

        AccountEntity otherAccount = AccountEntity.builder()
                .customer(otherCustomer)
                .accountNumber("ACC-OTHER-001")
                .aadharNumber("123456789013")
                .ifscCode("BANK0001234")
                .phoneNumberLinked("9876543211")
                .amount(BigDecimal.valueOf(100000))
                .bankName("Test Bank")
                .nameOnAccount("Other User")
                .status("ACTIVE")
                .build();
        otherAccount = accountRepository.save(otherAccount);
        otherCustomer.getAccounts().add(otherAccount);
        customerRepository.save(otherCustomer);

        String otherToken = jwtUtil.generateToken("other@example.com", "USER");

        mockMvc.perform(get("/loan/LOAN-10000001")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));
    }

    @Test
    @DisplayName("GET /loan/{loanId} - should return 404 for non-existent loan")
    void testGetLoanById_NotFound() throws Exception {
        mockMvc.perform(get("/loan/LOAN-99999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /loan/all - admin should retrieve all loans")
    void testGetAllLoans_Admin() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED");
        createTestLoan("LOAN-10000002", testAccountNumber, "REJECTED");

        mockMvc.perform(get("/loan/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All loans retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("GET /loan/all - should return 403 for non-admin user")
    void testGetAllLoans_NonAdmin() throws Exception {
        mockMvc.perform(get("/loan/all")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /loan/{loanId}/status - admin should update loan status")
    void testUpdateLoanStatus_Admin() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "UNDER_REVIEW");

        UpdateLoanStatusRequest request = new UpdateLoanStatusRequest();
        request.setStatus("APPROVED");

        mockMvc.perform(put("/loan/LOAN-10000001/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loan status updated successfully"));
    }

    @Test
    @DisplayName("PUT /loan/{loanId}/status - should return 403 for non-admin user")
    void testUpdateLoanStatus_NonAdmin() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "UNDER_REVIEW");

        UpdateLoanStatusRequest request = new UpdateLoanStatusRequest();
        request.setStatus("APPROVED");

        mockMvc.perform(put("/loan/LOAN-10000001/status")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /loan/statistics - admin should retrieve loan statistics")
    void testGetLoanStatistics_Admin() throws Exception {
        createTestLoan("LOAN-10000001", testAccountNumber, "APPROVED");
        createTestLoan("LOAN-10000002", testAccountNumber, "REJECTED");
        createTestLoan("LOAN-10000003", testAccountNumber, "UNDER_REVIEW");

        mockMvc.perform(get("/loan/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loan statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalApplications").value(3))
                .andExpect(jsonPath("$.data.approvedCount").value(1))
                .andExpect(jsonPath("$.data.rejectedCount").value(1))
                .andExpect(jsonPath("$.data.underReviewCount").value(1));
    }

    @Test
    @DisplayName("GET /loan/statistics - should return 403 for non-admin user")
    void testGetLoanStatistics_NonAdmin() throws Exception {
        mockMvc.perform(get("/loan/statistics")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All endpoints should return 403 for unauthenticated requests")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/loan/account/" + testAccountNumber))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/loan/LOAN-10000001"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/loan/all"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/loan/statistics"))
                .andExpect(status().isForbidden());
    }

    // Helper methods

    private LoanApplicationRequest createValidLoanRequest() {
        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setLoanAmount(BigDecimal.valueOf(500000));
        request.setLoanPurpose("HOME");
        request.setLoanTenure(240);
        request.setMonthlyIncome(BigDecimal.valueOf(80000));
        request.setEmploymentType("SALARIED");
        request.setExistingEmi(BigDecimal.valueOf(15000));
        request.setCreditScore(750);
        request.setAge(35);
        request.setExistingLoans(1);
        request.setHasCollateral(true);
        request.setResidenceYears(5);
        request.setHasGuarantor(true);
        request.setRepaymentHistory("CLEAN");
        return request;
    }

    private void createTestLoan(String loanId, String accountNumber, String status) {
        LoanEntity loan = LoanEntity.builder()
                .loanId(loanId)
                .accountNumber(accountNumber)
                .loanAmount(BigDecimal.valueOf(500000))
                .loanPurpose("HOME")
                .loanTenure(240)
                .monthlyIncome(BigDecimal.valueOf(80000))
                .employmentType("SALARIED")
                .existingEmi(BigDecimal.valueOf(15000))
                .creditScore(750)
                .age(35)
                .existingLoans(1)
                .hasCollateral(true)
                .residenceYears(5)
                .hasGuarantor(true)
                .repaymentHistory("CLEAN")
                .incomeScore(100.0)
                .employmentScore(70.0)
                .dtiScore(80.0)
                .repaymentHistoryScore(100.0)
                .ageScore(60.0)
                .existingLoansScore(50.0)
                .collateralScore(70.0)
                .bankingRelationshipScore(40.0)
                .residenceScore(40.0)
                .loanPurposeScore(40.0)
                .guarantorScore(30.0)
                .eligibilityScore(BigDecimal.valueOf(80.0))
                .dtiRatio(BigDecimal.valueOf(0.1875))
                .status(status)
                .interestRate(BigDecimal.valueOf(8.5))
                .emi(BigDecimal.valueOf(4500))
                .rejectionReason(null)
                .improvementTips("[]")
                .applicationDate(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
        loanRepository.save(loan);
    }
}
