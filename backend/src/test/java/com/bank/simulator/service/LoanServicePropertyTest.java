package com.bank.simulator.service;

import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.LoanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for LoanService
 * Tests verify that loan service methods satisfy specified properties across all valid inputs
 */
class LoanServicePropertyTest {

    // Mock repository for testing
    private final LoanRepository mockLoanRepository = Mockito.mock(LoanRepository.class);
    private final CreditScoringService mockCreditScoringService = Mockito.mock(CreditScoringService.class);
    private final NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
    private final AccountRepository mockAccountRepository = Mockito.mock(AccountRepository.class);
    private final ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
    
    // Create service instance with mocked dependencies
    private final LoanService loanService = new LoanService(
            mockLoanRepository, 
            mockCreditScoringService, 
            mockNotificationService,
            mockAccountRepository,
            mockObjectMapper
    );

    /**
     * Property 1: Loan ID Format and Uniqueness
     * 
     * **Validates: Requirements 1.2**
     * 
     * For any set of loan applications, all generated loan IDs SHALL match the pattern 
     * "LOAN-[0-9]{8}" and SHALL be unique across all applications.
     */
    @Property(tries = 100)
    @Label("Generated loan IDs match pattern LOAN-[0-9]{8}")
    void loanIdMatchesPattern() {
        // Arrange - mock repository to return empty (no collision)
        when(mockLoanRepository.findByLoanId(anyString())).thenReturn(Optional.empty());
        
        // Act - generate loan ID
        String loanId = loanService.generateLoanId();
        
        // Assert - verify format matches LOAN-XXXXXXXX where X is a digit
        Pattern loanIdPattern = Pattern.compile("^LOAN-\\d{8}$");
        assertThat(loanId)
                .as("Loan ID should match pattern LOAN-[0-9]{8}")
                .matches(loanIdPattern);
    }

    /**
     * Property 1 (Extended): Loan ID Uniqueness Across Multiple Generations
     * 
     * **Validates: Requirements 1.2**
     * 
     * For any set of loan applications, all generated loan IDs SHALL be unique.
     * This test generates multiple loan IDs and verifies they are all distinct.
     */
    @Property(tries = 50)
    @Label("Generated loan IDs are unique across multiple generations")
    void loanIdsAreUnique() {
        // Arrange - mock repository to return empty (no collision)
        when(mockLoanRepository.findByLoanId(anyString())).thenReturn(Optional.empty());
        
        // Act - generate multiple loan IDs
        java.util.Set<String> generatedIds = new java.util.HashSet<>();
        int numberOfIds = 100;
        
        for (int i = 0; i < numberOfIds; i++) {
            String loanId = loanService.generateLoanId();
            generatedIds.add(loanId);
        }
        
        // Assert - verify all IDs are unique (set size equals number of generations)
        assertThat(generatedIds)
                .as("All generated loan IDs should be unique")
                .hasSize(numberOfIds);
    }

    /**
     * Property 17: Loan Decision Logic
     * 
     * **Validates: Requirements 14.1, 14.2, 14.3**
     * 
     * For any eligibility score and DTI ratio, the loan status SHALL be determined as follows:
     * - IF eligibility_score >= 750 AND dti_ratio < 0.40 THEN status = APPROVED
     * - ELSE IF (650 <= eligibility_score < 750) OR (0.40 <= dti_ratio <= 0.50) THEN status = UNDER_REVIEW
     * - ELSE IF eligibility_score < 650 OR dti_ratio > 0.50 THEN status = REJECTED
     */
    @Property
    @Label("Loan decision logic follows specified rules for all eligibility scores and DTI ratios")
    void loanDecisionLogicFollowsRules(
            @ForAll("eligibilityScores") double eligibilityScore,
            @ForAll("dtiRatios") BigDecimal dtiRatio) {
        
        // Act
        String actualStatus = loanService.determineStatus(eligibilityScore, dtiRatio);
        
        // Assert - determine expected status based on decision rules
        String expectedStatus;
        
        // IF eligibility_score >= 75.0 (representing 750 on 0-100 scale) AND dti_ratio < 0.40 THEN APPROVED
        if (eligibilityScore >= 75.0 && dtiRatio.compareTo(BigDecimal.valueOf(0.40)) < 0) {
            expectedStatus = "APPROVED";
        }
        // ELSE IF (65.0 <= eligibility_score < 75.0) OR (0.40 <= dti_ratio <= 0.50) THEN UNDER_REVIEW
        else if ((eligibilityScore >= 65.0 && eligibilityScore < 75.0) ||
                 (dtiRatio.compareTo(BigDecimal.valueOf(0.40)) >= 0 && 
                  dtiRatio.compareTo(BigDecimal.valueOf(0.50)) <= 0)) {
            expectedStatus = "UNDER_REVIEW";
        }
        // ELSE REJECTED
        else {
            expectedStatus = "REJECTED";
        }
        
        assertThat(actualStatus)
                .as("Status for eligibility score %.2f and DTI ratio %s should be %s", 
                    eligibilityScore, dtiRatio, expectedStatus)
                .isEqualTo(expectedStatus);
    }

    /**
     * Arbitrary generator for eligibility scores
     * Generates scores from 0 to 100 (representing 0-100 scale)
     */
    @Provide
    Arbitrary<Double> eligibilityScores() {
        return Arbitraries.doubles()
                .between(0.0, 100.0)
                .ofScale(2);
    }

    /**
     * Arbitrary generator for DTI ratios
     * Generates realistic DTI ratios from 0.0 to 1.0 (0% to 100%)
     */
    @Provide
    Arbitrary<BigDecimal> dtiRatios() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.ONE)
                .ofScale(4);
    }

    /**
     * Property 19: Interest Rate Assignment
     * 
     * **Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5**
     * 
     * For any eligibility score and loan status, the assigned interest rate SHALL match the specified rules:
     * - IF status == REJECTED THEN rate = 0.0%
     * - ELSE IF eligibility_score >= 800 THEN rate = 7.5%
     * - ELSE IF eligibility_score >= 750 THEN rate = 8.5%
     * - ELSE IF eligibility_score >= 700 THEN rate = 10.0%
     * - ELSE IF eligibility_score >= 650 THEN rate = 12.0%
     */
    @Property
    @Label("Interest rate assignment follows specified rules for all eligibility scores and statuses")
    void interestRateAssignmentFollowsRules(
            @ForAll("eligibilityScores") double eligibilityScore,
            @ForAll("loanStatuses") String status) {
        
        // Act
        BigDecimal actualRate = loanService.assignInterestRate(eligibilityScore, status);
        
        // Assert - determine expected rate based on rules
        BigDecimal expectedRate;
        
        // REJECTED → 0.0%
        if ("REJECTED".equals(status)) {
            expectedRate = BigDecimal.ZERO;
        }
        // score >= 80.0 (representing 800 on 0-100 scale) → 7.5%
        else if (eligibilityScore >= 80.0) {
            expectedRate = BigDecimal.valueOf(7.5);
        }
        // score >= 75.0 (representing 750 on 0-100 scale) → 8.5%
        else if (eligibilityScore >= 75.0) {
            expectedRate = BigDecimal.valueOf(8.5);
        }
        // score >= 70.0 (representing 700 on 0-100 scale) → 10.0%
        else if (eligibilityScore >= 70.0) {
            expectedRate = BigDecimal.valueOf(10.0);
        }
        // score >= 65.0 (representing 650 on 0-100 scale) → 12.0%
        else {
            expectedRate = BigDecimal.valueOf(12.0);
        }
        
        assertThat(actualRate)
                .as("Interest rate for eligibility score %.2f and status %s should be %s%%", 
                    eligibilityScore, status, expectedRate)
                .isEqualByComparingTo(expectedRate);
    }

    /**
     * Arbitrary generator for loan statuses
     * Generates all valid loan status values
     */
    @Provide
    Arbitrary<String> loanStatuses() {
        return Arbitraries.of("PENDING", "APPROVED", "REJECTED", "UNDER_REVIEW");
    }

    /**
     * Property 20: EMI Calculation Formula
     * 
     * **Validates: Requirements 16.1, 16.2, 16.3, 16.4, 16.5**
     * 
     * For any positive loan amount P, positive annual interest rate r (as percentage), 
     * and positive tenure n (in months), the calculated EMI SHALL equal:
     * monthly_rate = (r / 12) / 100
     * EMI = P * monthly_rate * (1 + monthly_rate)^n / ((1 + monthly_rate)^n - 1)
     * rounded to 2 decimal places.
     */
    @Property
    @Label("EMI calculation follows standard loan amortization formula")
    void emiCalculationFollowsFormula(
            @ForAll("loanAmounts") BigDecimal loanAmount,
            @ForAll("interestRates") BigDecimal interestRate,
            @ForAll("tenures") int tenure) {
        
        // Act
        BigDecimal actualEmi = loanService.calculateEmi(loanAmount, interestRate, tenure);
        
        // Assert - calculate expected EMI using the formula
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            // Edge case: interest rate is 0 (rejected loans)
            assertThat(actualEmi)
                    .as("EMI should be 0 when interest rate is 0")
                    .isEqualByComparingTo(BigDecimal.ZERO);
        } else {
            // Calculate monthly rate: r = (annual_rate / 12) / 100
            BigDecimal monthlyRate = interestRate
                    .divide(BigDecimal.valueOf(12), 10, java.math.RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP);
            
            // Calculate (1 + r)^n
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowerN = onePlusR.pow(tenure);
            
            // Calculate EMI = P * r * (1+r)^n / ((1+r)^n - 1)
            BigDecimal numerator = loanAmount.multiply(monthlyRate).multiply(onePlusRPowerN);
            BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
            BigDecimal expectedEmi = numerator.divide(denominator, 2, java.math.RoundingMode.HALF_UP);
            
            assertThat(actualEmi)
                    .as("EMI for loan amount %s, interest rate %s%%, tenure %d months should be %s",
                        loanAmount, interestRate, tenure, expectedEmi)
                    .isEqualByComparingTo(expectedEmi);
        }
    }

    /**
     * Arbitrary generator for loan amounts
     * Generates realistic loan amounts between 10,000 and 10,000,000 INR
     */
    @Provide
    Arbitrary<BigDecimal> loanAmounts() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(10000), BigDecimal.valueOf(10000000))
                .ofScale(2);
    }

    /**
     * Arbitrary generator for interest rates
     * Generates realistic interest rates between 0% and 20% (including 0 for rejected loans)
     */
    @Provide
    Arbitrary<BigDecimal> interestRates() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.valueOf(20))
                .ofScale(2);
    }

    /**
     * Arbitrary generator for loan tenures
     * Generates realistic tenures between 6 and 360 months
     */
    @Provide
    Arbitrary<Integer> tenures() {
        return Arbitraries.integers().between(6, 360);
    }

    /**
     * Property 18: Rejection Reason Generation
     * 
     * **Validates: Requirements 14.4**
     * 
     * For any loan application with status REJECTED, the rejection reason SHALL be non-empty 
     * and SHALL mention at least one factor that scored below 50% of its maximum value.
     */
    @Property
    @Label("Rejection reason is non-empty for rejected loans and mentions weak factors")
    void rejectionReasonForRejectedLoans(
            @ForAll("creditScoreResults") com.bank.simulator.dto.CreditScoreResult scoreResult) {
        
        // Act - generate rejection reason for REJECTED status
        String rejectionReason = loanService.generateRejectionReason(scoreResult, "REJECTED");
        
        // Assert - rejection reason should be non-empty
        assertThat(rejectionReason)
                .as("Rejection reason should be non-empty for REJECTED status")
                .isNotEmpty();
        
        // Assert - rejection reason should mention at least one weak factor
        // A factor is weak if it scores below 50% of its maximum
        boolean hasWeakFactor = 
                scoreResult.getIncomeScore() < 60.0 ||  // 50% of 120
                scoreResult.getEmploymentScore() < 40.0 ||  // 50% of 80
                scoreResult.getDtiScore() < 50.0 ||  // 50% of 100
                scoreResult.getRepaymentHistoryScore() < 50.0 ||  // 50% of 100
                scoreResult.getAgeScore() < 30.0 ||  // 50% of 60
                scoreResult.getExistingLoansScore() < 30.0 ||  // 50% of 60
                scoreResult.getCollateralScore() < 35.0 ||  // 50% of 70
                scoreResult.getBankingRelationshipScore() < 25.0 ||  // 50% of 50
                scoreResult.getResidenceScore() < 20.0 ||  // 50% of 40
                scoreResult.getLoanPurposeScore() < 20.0 ||  // 50% of 40
                scoreResult.getGuarantorScore() < 15.0;  // 50% of 30
        
        if (hasWeakFactor) {
            // If there are weak factors, the reason should mention specific factors
            assertThat(rejectionReason)
                    .as("Rejection reason should mention specific weak factors")
                    .containsAnyOf("income", "employment", "debt-to-income", "repayment", 
                                   "age", "loans", "collateral", "banking", "residence", 
                                   "purpose", "guarantor", "Low", "Unstable", "High", 
                                   "Poor", "Too many", "No", "Short");
        }
    }

    /**
     * Property 18 (Extended): No Rejection Reason for Non-Rejected Loans
     * 
     * **Validates: Requirements 14.4**
     * 
     * For any loan application with status other than REJECTED, the rejection reason SHALL be empty.
     */
    @Property
    @Label("Rejection reason is empty for non-rejected loans")
    void noRejectionReasonForNonRejectedLoans(
            @ForAll("creditScoreResults") com.bank.simulator.dto.CreditScoreResult scoreResult,
            @ForAll("nonRejectedStatuses") String status) {
        
        // Act
        String rejectionReason = loanService.generateRejectionReason(scoreResult, status);
        
        // Assert - rejection reason should be empty for non-rejected statuses
        assertThat(rejectionReason)
                .as("Rejection reason should be empty for status %s", status)
                .isEmpty();
    }

    /**
     * Arbitrary generator for non-rejected statuses
     */
    @Provide
    Arbitrary<String> nonRejectedStatuses() {
        return Arbitraries.of("PENDING", "APPROVED", "UNDER_REVIEW");
    }

    /**
     * Property 21: Improvement Tips Generation for Weak Factors
     * 
     * **Validates: Requirements 17.1, 17.2, 17.3**
     * 
     * For any loan application where one or more factor scores are below 50% of their maximum value, 
     * the improvement tips list SHALL contain at least one tip corresponding to each weak factor.
     */
    @Property
    @Label("Improvement tips are generated for each weak factor")
    void improvementTipsForWeakFactors(
            @ForAll("creditScoreResults") com.bank.simulator.dto.CreditScoreResult scoreResult,
            @ForAll("nonApprovedStatuses") String status) {
        
        // Act
        java.util.List<String> tips = loanService.generateImprovementTips(scoreResult, status);
        
        // Assert - count weak factors
        int weakFactorCount = 0;
        if (scoreResult.getIncomeScore() < 60.0) weakFactorCount++;
        if (scoreResult.getEmploymentScore() < 40.0) weakFactorCount++;
        if (scoreResult.getDtiScore() < 50.0) weakFactorCount++;
        if (scoreResult.getRepaymentHistoryScore() < 50.0) weakFactorCount++;
        if (scoreResult.getAgeScore() < 30.0) weakFactorCount++;
        if (scoreResult.getExistingLoansScore() < 30.0) weakFactorCount++;
        if (scoreResult.getCollateralScore() < 35.0) weakFactorCount++;
        if (scoreResult.getBankingRelationshipScore() < 25.0) weakFactorCount++;
        if (scoreResult.getResidenceScore() < 20.0) weakFactorCount++;
        if (scoreResult.getLoanPurposeScore() < 20.0) weakFactorCount++;
        if (scoreResult.getGuarantorScore() < 15.0) weakFactorCount++;
        
        // Assert - tips list should have at least as many tips as weak factors
        assertThat(tips)
                .as("Should have at least one tip for each weak factor (expected %d tips)", weakFactorCount)
                .hasSizeGreaterThanOrEqualTo(weakFactorCount);
    }

    /**
     * Arbitrary generator for non-approved statuses
     */
    @Provide
    Arbitrary<String> nonApprovedStatuses() {
        return Arbitraries.of("PENDING", "REJECTED", "UNDER_REVIEW");
    }

    /**
     * Property 22: No Improvement Tips for Approved Loans
     * 
     * **Validates: Requirements 17.4**
     * 
     * For any loan application with status APPROVED, the improvement tips list SHALL be empty.
     */
    @Property
    @Label("No improvement tips for approved loans")
    void noImprovementTipsForApprovedLoans(
            @ForAll("creditScoreResults") com.bank.simulator.dto.CreditScoreResult scoreResult) {
        
        // Act
        java.util.List<String> tips = loanService.generateImprovementTips(scoreResult, "APPROVED");
        
        // Assert - tips list should be empty for approved loans
        assertThat(tips)
                .as("Improvement tips should be empty for APPROVED status")
                .isEmpty();
    }

    /**
     * Arbitrary generator for CreditScoreResult
     * Generates realistic credit score results with all factor scores
     */
    @Provide
    Arbitrary<com.bank.simulator.dto.CreditScoreResult> creditScoreResults() {
        // Generate individual factor scores
        Arbitrary<Double> incomeScore = Arbitraries.doubles().between(0.0, 120.0);
        Arbitrary<Double> employmentScore = Arbitraries.doubles().between(0.0, 80.0);
        Arbitrary<Double> dtiScore = Arbitraries.doubles().between(0.0, 100.0);
        Arbitrary<Double> repaymentHistoryScore = Arbitraries.doubles().between(0.0, 100.0);
        Arbitrary<Double> ageScore = Arbitraries.doubles().between(0.0, 60.0);
        Arbitrary<Double> existingLoansScore = Arbitraries.doubles().between(0.0, 60.0);
        Arbitrary<Double> collateralScore = Arbitraries.doubles().between(0.0, 70.0);
        Arbitrary<Double> bankingRelationshipScore = Arbitraries.doubles().between(0.0, 50.0);
        Arbitrary<Double> residenceScore = Arbitraries.doubles().between(0.0, 40.0);
        Arbitrary<Double> loanPurposeScore = Arbitraries.doubles().between(0.0, 40.0);
        Arbitrary<Double> guarantorScore = Arbitraries.doubles().between(0.0, 30.0);
        
        // Combine first 8 scores
        return Combinators.combine(
                incomeScore, employmentScore, dtiScore, repaymentHistoryScore,
                ageScore, existingLoansScore, collateralScore, bankingRelationshipScore
        ).flatAs((income, employment, dti, repayment, age, existing, collateral, banking) -> {
            // Combine remaining 3 scores
            return Combinators.combine(residenceScore, loanPurposeScore, guarantorScore)
                    .as((residence, purpose, guarantor) -> {
                        // Calculate eligibility score: (sum / 750) * 100
                        double sum = income + employment + dti + repayment + age + existing + 
                                    collateral + banking + residence + purpose + guarantor;
                        BigDecimal eligibilityScore = BigDecimal.valueOf((sum / 750.0) * 100.0)
                                .setScale(2, java.math.RoundingMode.HALF_UP);
                        
                        // Generate random DTI ratio
                        BigDecimal dtiRatio = BigDecimal.valueOf(Math.random())
                                .setScale(4, java.math.RoundingMode.HALF_UP);
                        
                        return com.bank.simulator.dto.CreditScoreResult.builder()
                                .incomeScore(income)
                                .employmentScore(employment)
                                .dtiScore(dti)
                                .repaymentHistoryScore(repayment)
                                .ageScore(age)
                                .existingLoansScore(existing)
                                .collateralScore(collateral)
                                .bankingRelationshipScore(banking)
                                .residenceScore(residence)
                                .loanPurposeScore(purpose)
                                .guarantorScore(guarantor)
                                .eligibilityScore(eligibilityScore)
                                .dtiRatio(dtiRatio)
                                .build();
                    });
        });
    }
}
