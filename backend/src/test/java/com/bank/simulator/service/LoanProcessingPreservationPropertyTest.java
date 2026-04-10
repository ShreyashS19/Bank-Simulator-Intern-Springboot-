package com.bank.simulator.service;

import com.bank.simulator.dto.CreditScoreResult;
import com.bank.simulator.dto.LoanApplicationRequest;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.CustomerEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.CustomerRepository;
import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 2: Preservation - Loan Processing Logic Preservation
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**
 * 
 * These property-based tests verify that the loan processing logic (credit scoring,
 * status determination, interest rate assignment, EMI calculation) remains unchanged
 * after the LazyInitializationException fix.
 * 
 * Since the unfixed code crashes on all requests, we cannot observe actual behavior.
 * Instead, we verify that the fixed code produces results consistent with the
 * documented business logic.
 * 
 * The @Transactional annotation on the test class simulates the post-fix behavior
 * where the Hibernate session remains open, allowing us to establish the baseline.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@JqwikSpringSupport
class LoanProcessingPreservationPropertyTest {

    @Autowired
    private CreditScoringService creditScoringService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testAccountNumber;

    /**
     * Setup test data before each property test
     * Creates a test customer and account for credit scoring calculations
     */
    @BeforeEach
    void setUp() {
        // Clean up test data
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer
        CustomerEntity customer = CustomerEntity.builder()
                .name("Test Customer")
                .phoneNumber("9876543210")
                .email("test@example.com")
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
    }

    /**
     * Property 2.1: Credit Scoring Calculation Preservation
     * 
     * **Validates: Requirements 3.1**
     * 
     * For any valid loan application request, the credit scoring calculation SHALL
     * follow the documented 12-factor formula:
     * - Income Score (max 120)
     * - Employment Score (max 80)
     * - DTI Score (max 100)
     * - Repayment History Score (max 100)
     * - Age Score (max 60)
     * - Existing Loans Score (max 60)
     * - Collateral Score (max 70)
     * - Banking Relationship Score (max 50)
     * - Residence Score (max 40)
     * - Loan Purpose Score (max 40)
     * - Guarantor Score (max 30)
     * - Eligibility Score = (sum / 750) * 100
     */
    @Property(tries = 50)
    @Label("Credit scoring calculation follows 12-factor formula")
    void creditScoringFollowsDocumentedFormula(
            @ForAll("loanApplicationRequests") LoanApplicationRequest request) {
        
        // Act - calculate credit score using the test account number
        CreditScoreResult result = creditScoringService.calculateCreditScore(request, testAccountNumber);
        
        // Assert - verify each factor score follows documented rules
        assertIncomeScoreIsCorrect(request.getMonthlyIncome(), result.getIncomeScore());
        assertEmploymentScoreIsCorrect(request.getEmploymentType(), result.getEmploymentScore());
        assertRepaymentHistoryScoreIsCorrect(request.getRepaymentHistory(), result.getRepaymentHistoryScore());
        assertAgeScoreIsCorrect(request.getAge(), result.getAgeScore());
        assertExistingLoansScoreIsCorrect(request.getExistingLoans(), result.getExistingLoansScore());
        assertCollateralScoreIsCorrect(request.getHasCollateral(), result.getCollateralScore());
        assertResidenceScoreIsCorrect(request.getResidenceYears(), result.getResidenceScore());
        assertLoanPurposeScoreIsCorrect(request.getLoanPurpose(), result.getLoanPurposeScore());
        assertGuarantorScoreIsCorrect(request.getHasGuarantor(), result.getGuarantorScore());
        
        // Assert - verify DTI ratio calculation
        BigDecimal expectedDtiRatio = request.getExistingEmi()
                .divide(request.getMonthlyIncome(), 4, RoundingMode.HALF_UP);
        assertThat(result.getDtiRatio())
                .as("DTI ratio should be existingEmi / monthlyIncome")
                .isEqualByComparingTo(expectedDtiRatio);
        
        // Assert - verify DTI score
        assertDtiScoreIsCorrect(result.getDtiRatio(), result.getDtiScore());
        
        // Assert - verify eligibility score calculation
        double totalScore = result.getIncomeScore() + result.getEmploymentScore() + 
                result.getDtiScore() + result.getRepaymentHistoryScore() + result.getAgeScore() +
                result.getExistingLoansScore() + result.getCollateralScore() + 
                result.getBankingRelationshipScore() + result.getResidenceScore() +
                result.getLoanPurposeScore() + result.getGuarantorScore();
        
        BigDecimal expectedEligibilityScore = BigDecimal.valueOf((totalScore / 750.0) * 100)
                .setScale(2, RoundingMode.HALF_UP);
        
        assertThat(result.getEligibilityScore())
                .as("Eligibility score should be (sum / 750) * 100")
                .isEqualByComparingTo(expectedEligibilityScore);
    }

    /**
     * Property 2.2: Loan Status Determination Preservation
     * 
     * **Validates: Requirements 3.2**
     * 
     * For any eligibility score and DTI ratio, the loan status SHALL be determined
     * according to the documented decision rules:
     * - IF eligibility_score >= 75.0 AND dti_ratio < 0.40 THEN APPROVED
     * - ELSE IF (65.0 <= eligibility_score < 75.0) OR (0.40 <= dti_ratio <= 0.50) THEN UNDER_REVIEW
     * - ELSE REJECTED
     */
    @Property(tries = 100)
    @Label("Loan status determination follows documented decision rules")
    void loanStatusDeterminationFollowsRules(
            @ForAll("eligibilityScores") double eligibilityScore,
            @ForAll("dtiRatios") BigDecimal dtiRatio) {
        
        // Act
        String actualStatus = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert - determine expected status based on documented rules
        String expectedStatus;
        
        if (eligibilityScore >= 75.0 && dtiRatio.compareTo(BigDecimal.valueOf(0.40)) < 0) {
            expectedStatus = "APPROVED";
        } else if ((eligibilityScore >= 65.0 && eligibilityScore < 75.0) ||
                   (dtiRatio.compareTo(BigDecimal.valueOf(0.40)) >= 0 && 
                    dtiRatio.compareTo(BigDecimal.valueOf(0.50)) <= 0)) {
            expectedStatus = "UNDER_REVIEW";
        } else {
            expectedStatus = "REJECTED";
        }
        
        assertThat(actualStatus)
                .as("Status for eligibility score %.2f and DTI ratio %s should be %s",
                    eligibilityScore, dtiRatio, expectedStatus)
                .isEqualTo(expectedStatus);
    }

    /**
     * Property 2.3: Interest Rate Assignment Preservation
     * 
     * **Validates: Requirements 3.3**
     * 
     * For any eligibility score and loan status, the assigned interest rate SHALL
     * follow the documented rules:
     * - IF status == REJECTED THEN rate = 0.0%
     * - ELSE IF eligibility_score >= 80.0 THEN rate = 7.5%
     * - ELSE IF eligibility_score >= 75.0 THEN rate = 8.5%
     * - ELSE IF eligibility_score >= 70.0 THEN rate = 10.0%
     * - ELSE IF eligibility_score >= 65.0 THEN rate = 12.0%
     */
    @Property(tries = 100)
    @Label("Interest rate assignment follows documented eligibility score brackets")
    void interestRateAssignmentFollowsRules(
            @ForAll("eligibilityScores") double eligibilityScore,
            @ForAll("loanStatuses") String status) {
        
        // Act
        BigDecimal actualRate = loanService.assignInterestRate(eligibilityScore, status);
        
        // Assert - determine expected rate based on documented rules
        BigDecimal expectedRate;
        
        if ("REJECTED".equals(status)) {
            expectedRate = BigDecimal.ZERO;
        } else if (eligibilityScore >= 80.0) {
            expectedRate = BigDecimal.valueOf(7.5);
        } else if (eligibilityScore >= 75.0) {
            expectedRate = BigDecimal.valueOf(8.5);
        } else if (eligibilityScore >= 70.0) {
            expectedRate = BigDecimal.valueOf(10.0);
        } else {
            expectedRate = BigDecimal.valueOf(12.0);
        }
        
        assertThat(actualRate)
                .as("Interest rate for eligibility score %.2f and status %s should be %s%%",
                    eligibilityScore, status, expectedRate)
                .isEqualByComparingTo(expectedRate);
    }

    /**
     * Property 2.4: EMI Calculation Preservation
     * 
     * **Validates: Requirements 3.4**
     * 
     * For any positive loan amount P, positive annual interest rate r, and positive
     * tenure n, the calculated EMI SHALL follow the standard amortization formula:
     * monthly_rate = (r / 12) / 100
     * EMI = P * monthly_rate * (1 + monthly_rate)^n / ((1 + monthly_rate)^n - 1)
     */
    @Property(tries = 100)
    @Label("EMI calculation follows standard loan amortization formula")
    void emiCalculationFollowsFormula(
            @ForAll("loanAmounts") BigDecimal loanAmount,
            @ForAll("interestRates") BigDecimal interestRate,
            @ForAll("tenures") int tenure) {
        
        // Act
        BigDecimal actualEmi = loanService.calculateEmi(loanAmount, interestRate, tenure);
        
        // Assert - calculate expected EMI using the documented formula
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            assertThat(actualEmi)
                    .as("EMI should be 0 when interest rate is 0")
                    .isEqualByComparingTo(BigDecimal.ZERO);
        } else {
            BigDecimal monthlyRate = interestRate
                    .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowerN = onePlusR.pow(tenure);
            
            BigDecimal numerator = loanAmount.multiply(monthlyRate).multiply(onePlusRPowerN);
            BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
            BigDecimal expectedEmi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
            
            assertThat(actualEmi)
                    .as("EMI for loan amount %s, interest rate %s%%, tenure %d months should be %s",
                        loanAmount, interestRate, tenure, expectedEmi)
                    .isEqualByComparingTo(expectedEmi);
        }
    }

    // ==================== Arbitrary Generators ====================

    /**
     * Generator for valid loan application requests
     */
    @Provide
    Arbitrary<LoanApplicationRequest> loanApplicationRequests() {
        // Combine first 8 parameters
        return Combinators.combine(
                loanAmounts(),
                loanPurposes(),
                tenures(),
                monthlyIncomes(),
                employmentTypes(),
                existingEmis(),
                creditScores(),
                ages()
        ).flatAs((loanAmount, purpose, tenure, income, employment, emi, creditScore, age) -> {
            // Combine remaining 5 parameters
            return Combinators.combine(
                    existingLoans(),
                    Arbitraries.of(true, false), // hasCollateral
                    residenceYears(),
                    Arbitraries.of(true, false), // hasGuarantor
                    repaymentHistories()
            ).as((loans, collateral, residence, guarantor, repayment) -> {
                LoanApplicationRequest request = new LoanApplicationRequest();
                request.setLoanAmount(loanAmount);
                request.setLoanPurpose(purpose);
                request.setLoanTenure(tenure);
                request.setMonthlyIncome(income);
                request.setEmploymentType(employment);
                request.setExistingEmi(emi);
                request.setCreditScore(creditScore);
                request.setAge(age);
                request.setExistingLoans(loans);
                request.setHasCollateral(collateral);
                request.setResidenceYears(residence);
                request.setHasGuarantor(guarantor);
                request.setRepaymentHistory(repayment);
                return request;
            });
        });
    }

    @Provide
    Arbitrary<BigDecimal> loanAmounts() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(10000), BigDecimal.valueOf(10000000))
                .ofScale(2);
    }

    @Provide
    Arbitrary<String> loanPurposes() {
        return Arbitraries.of("EDUCATION", "HOME", "BUSINESS", "VEHICLE", "PERSONAL");
    }

    @Provide
    Arbitrary<Integer> tenures() {
        return Arbitraries.integers().between(6, 360);
    }

    @Provide
    Arbitrary<BigDecimal> monthlyIncomes() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(10000), BigDecimal.valueOf(500000))
                .ofScale(2);
    }

    @Provide
    Arbitrary<String> employmentTypes() {
        return Arbitraries.of("GOVERNMENT", "SALARIED", "SELF_EMPLOYED", "UNEMPLOYED");
    }

    @Provide
    Arbitrary<BigDecimal> existingEmis() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.valueOf(100000))
                .ofScale(2);
    }

    @Provide
    Arbitrary<Integer> creditScores() {
        return Arbitraries.integers().between(300, 900);
    }

    @Provide
    Arbitrary<Integer> ages() {
        return Arbitraries.integers().between(18, 70);
    }

    @Provide
    Arbitrary<Integer> existingLoans() {
        return Arbitraries.integers().between(0, 5);
    }

    @Provide
    Arbitrary<Integer> residenceYears() {
        return Arbitraries.integers().between(0, 20);
    }

    @Provide
    Arbitrary<String> repaymentHistories() {
        return Arbitraries.of("CLEAN", "DEFAULTED");
    }

    @Provide
    Arbitrary<Double> eligibilityScores() {
        return Arbitraries.doubles().between(0.0, 100.0).ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> dtiRatios() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.ONE)
                .ofScale(4);
    }

    @Provide
    Arbitrary<String> loanStatuses() {
        return Arbitraries.of("APPROVED", "REJECTED", "UNDER_REVIEW");
    }

    @Provide
    Arbitrary<BigDecimal> interestRates() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.valueOf(20))
                .ofScale(2);
    }

    // ==================== Assertion Helper Methods ====================

    private void assertIncomeScoreIsCorrect(BigDecimal monthlyIncome, double actualScore) {
        double expectedScore;
        if (monthlyIncome.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            expectedScore = 120.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(75000)) >= 0) {
            expectedScore = 100.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            expectedScore = 80.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(30000)) >= 0) {
            expectedScore = 60.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(20000)) >= 0) {
            expectedScore = 40.0;
        } else {
            expectedScore = 20.0;
        }
        
        assertThat(actualScore)
                .as("Income score for monthly income %s should be %.1f", monthlyIncome, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertEmploymentScoreIsCorrect(String employmentType, double actualScore) {
        double expectedScore = switch (employmentType) {
            case "GOVERNMENT" -> 80.0;
            case "SALARIED" -> 70.0;
            case "SELF_EMPLOYED" -> 50.0;
            case "UNEMPLOYED" -> 0.0;
            default -> 0.0;
        };
        
        assertThat(actualScore)
                .as("Employment score for type %s should be %.1f", employmentType, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertDtiScoreIsCorrect(BigDecimal dtiRatio, double actualScore) {
        double expectedScore;
        if (dtiRatio.compareTo(BigDecimal.valueOf(0.20)) < 0) {
            expectedScore = 100.0;
        } else if (dtiRatio.compareTo(BigDecimal.valueOf(0.30)) < 0) {
            expectedScore = 80.0;
        } else if (dtiRatio.compareTo(BigDecimal.valueOf(0.40)) < 0) {
            expectedScore = 60.0;
        } else if (dtiRatio.compareTo(BigDecimal.valueOf(0.50)) < 0) {
            expectedScore = 40.0;
        } else {
            expectedScore = 0.0;
        }
        
        assertThat(actualScore)
                .as("DTI score for ratio %s should be %.1f", dtiRatio, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertRepaymentHistoryScoreIsCorrect(String repaymentHistory, double actualScore) {
        double expectedScore = "CLEAN".equals(repaymentHistory) ? 100.0 : 0.0;
        
        assertThat(actualScore)
                .as("Repayment history score for %s should be %.1f", repaymentHistory, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertAgeScoreIsCorrect(int age, double actualScore) {
        double expectedScore;
        if (age >= 30 && age <= 50) {
            expectedScore = 60.0;
        } else if ((age >= 25 && age <= 29) || (age >= 51 && age <= 60)) {
            expectedScore = 50.0;
        } else if ((age >= 18 && age <= 24) || (age >= 61 && age <= 70)) {
            expectedScore = 30.0;
        } else {
            expectedScore = 0.0;
        }
        
        assertThat(actualScore)
                .as("Age score for age %d should be %.1f", age, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertExistingLoansScoreIsCorrect(int existingLoans, double actualScore) {
        double expectedScore;
        if (existingLoans == 0) {
            expectedScore = 60.0;
        } else if (existingLoans == 1) {
            expectedScore = 50.0;
        } else if (existingLoans == 2) {
            expectedScore = 30.0;
        } else {
            expectedScore = 0.0;
        }
        
        assertThat(actualScore)
                .as("Existing loans score for %d loans should be %.1f", existingLoans, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertCollateralScoreIsCorrect(boolean hasCollateral, double actualScore) {
        double expectedScore = hasCollateral ? 70.0 : 0.0;
        
        assertThat(actualScore)
                .as("Collateral score for hasCollateral=%b should be %.1f", hasCollateral, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertResidenceScoreIsCorrect(int residenceYears, double actualScore) {
        double expectedScore;
        if (residenceYears >= 5) {
            expectedScore = 40.0;
        } else if (residenceYears >= 3) {
            expectedScore = 30.0;
        } else if (residenceYears >= 1) {
            expectedScore = 20.0;
        } else {
            expectedScore = 10.0;
        }
        
        assertThat(actualScore)
                .as("Residence score for %d years should be %.1f", residenceYears, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertLoanPurposeScoreIsCorrect(String loanPurpose, double actualScore) {
        double expectedScore = switch (loanPurpose) {
            case "EDUCATION", "HOME" -> 40.0;
            case "BUSINESS" -> 30.0;
            case "VEHICLE" -> 25.0;
            case "PERSONAL" -> 15.0;
            default -> 0.0;
        };
        
        assertThat(actualScore)
                .as("Loan purpose score for %s should be %.1f", loanPurpose, expectedScore)
                .isEqualTo(expectedScore);
    }

    private void assertGuarantorScoreIsCorrect(boolean hasGuarantor, double actualScore) {
        double expectedScore = hasGuarantor ? 30.0 : 0.0;
        
        assertThat(actualScore)
                .as("Guarantor score for hasGuarantor=%b should be %.1f", hasGuarantor, expectedScore)
                .isEqualTo(expectedScore);
    }
}
