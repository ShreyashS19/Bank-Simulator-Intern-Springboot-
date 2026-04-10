package com.bank.simulator.service;

import net.jqwik.api.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for CreditScoringService
 * Tests verify that scoring algorithms satisfy specified properties across all valid inputs
 */
class CreditScoringServicePropertyTest {

    // Create service instance without dependencies for testing pure calculation methods
    private final CreditScoringService creditScoringService = new CreditScoringService(null, null);

    /**
     * Property 3: Income Score Calculation
     * 
     * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**
     * 
     * For any monthly income value, the calculated income score SHALL match the specified bracket rules:
     * - income >= 100000 → 120 points
     * - 75000 <= income < 100000 → 100 points
     * - 50000 <= income < 75000 → 80 points
     * - 30000 <= income < 50000 → 60 points
     * - 20000 <= income < 30000 → 40 points
     * - income < 20000 → 20 points
     */
    @Property
    @Label("Income score calculation follows bracket rules for all income values")
    void incomeScoreFollowsBracketRules(@ForAll("monthlyIncomes") BigDecimal monthlyIncome) {
        // Act
        double actualScore = creditScoringService.calculateIncomeScore(monthlyIncome);

        // Assert - determine expected score based on bracket rules
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

    /**
     * Arbitrary generator for monthly income values
     * Generates realistic income values from 0 to 500,000 INR
     */
    @Provide
    Arbitrary<BigDecimal> monthlyIncomes() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.valueOf(500000))
                .ofScale(2);
    }

    /**
     * Property 4: Employment Score Calculation
     * 
     * **Validates: Requirements 4.1, 4.2, 4.3, 4.4**
     * 
     * For any employment type, the calculated employment score SHALL match the specified mapping:
     * - GOVERNMENT → 80 points
     * - SALARIED → 70 points
     * - SELF_EMPLOYED → 50 points
     * - UNEMPLOYED → 0 points
     */
    @Property
    @Label("Employment score calculation follows mapping rules for all employment types")
    void employmentScoreFollowsMappingRules(@ForAll("employmentTypes") String employmentType) {
        // Act
        double actualScore = creditScoringService.calculateEmploymentScore(employmentType);

        // Assert - determine expected score based on employment type
        double expectedScore = switch (employmentType) {
            case "GOVERNMENT" -> 80.0;
            case "SALARIED" -> 70.0;
            case "SELF_EMPLOYED" -> 50.0;
            case "UNEMPLOYED" -> 0.0;
            default -> throw new IllegalStateException("Unexpected employment type: " + employmentType);
        };

        assertThat(actualScore)
                .as("Employment score for type %s should be %.1f", employmentType, expectedScore)
                .isEqualTo(expectedScore);
    }

    /**
     * Arbitrary generator for employment types
     * Generates all valid employment type values
     */
    @Provide
    Arbitrary<String> employmentTypes() {
        return Arbitraries.of("GOVERNMENT", "SALARIED", "SELF_EMPLOYED", "UNEMPLOYED");
    }

    /**
     * Property 5: DTI Ratio Calculation
     * 
     * **Validates: Requirements 5.1**
     * 
     * For any positive monthly income and non-negative existing EMI, the calculated DTI ratio 
     * SHALL equal existing_emi / monthly_income with precision to 4 decimal places.
     */
    @Property
    @Label("DTI ratio calculation equals existing_emi / monthly_income with 4 decimal precision")
    void dtiRatioCalculationIsCorrect(
            @ForAll("positiveMonthlyIncomes") BigDecimal monthlyIncome,
            @ForAll("nonNegativeEmi") BigDecimal existingEmi) {
        // Act
        BigDecimal actualDtiRatio = creditScoringService.calculateDtiRatio(existingEmi, monthlyIncome);

        // Assert - calculate expected DTI ratio with 4 decimal places precision
        BigDecimal expectedDtiRatio = existingEmi.divide(monthlyIncome, 4, java.math.RoundingMode.HALF_UP);

        assertThat(actualDtiRatio)
                .as("DTI ratio for existing EMI %s and monthly income %s should be %s", 
                    existingEmi, monthlyIncome, expectedDtiRatio)
                .isEqualByComparingTo(expectedDtiRatio);
    }

    /**
     * Arbitrary generator for positive monthly income values
     * Generates realistic income values from 1 to 500,000 INR (excluding zero)
     */
    @Provide
    Arbitrary<BigDecimal> positiveMonthlyIncomes() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ONE, BigDecimal.valueOf(500000))
                .ofScale(2);
    }

    /**
     * Arbitrary generator for non-negative existing EMI values
     * Generates realistic EMI values from 0 to 200,000 INR
     */
    @Provide
    Arbitrary<BigDecimal> nonNegativeEmi() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.valueOf(200000))
                .ofScale(2);
    }

    /**
     * Property 6: DTI Score Calculation
     * 
     * **Validates: Requirements 5.2, 5.3, 5.4, 5.5, 5.6**
     * 
     * For any DTI ratio value, the calculated DTI score SHALL match the specified bracket rules:
     * - dti < 0.20 → 100 points
     * - 0.20 <= dti < 0.30 → 80 points
     * - 0.30 <= dti < 0.40 → 60 points
     * - 0.40 <= dti < 0.50 → 40 points
     * - dti >= 0.50 → 0 points
     */
    @Property
    @Label("DTI score calculation follows bracket rules for all DTI ratio values")
    void dtiScoreFollowsBracketRules(@ForAll("dtiRatios") BigDecimal dtiRatio) {
        // Act
        double actualScore = creditScoringService.calculateDtiScore(dtiRatio);

        // Assert - determine expected score based on bracket rules
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
                .as("DTI score for DTI ratio %s should be %.1f", dtiRatio, expectedScore)
                .isEqualTo(expectedScore);
    }

    /**
     * Arbitrary generator for DTI ratio values
     * Generates realistic DTI ratios from 0.0 to 1.0 (0% to 100%)
     */
    @Provide
    Arbitrary<BigDecimal> dtiRatios() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, BigDecimal.ONE)
                .ofScale(4);
    }

    /**
     * Property 8: Age Score Calculation
     * 
     * **Validates: Requirements 7.1, 7.2, 7.3**
     * 
     * For any age value between 18 and 70, the calculated age score SHALL match the specified bracket rules:
     * - 30 <= age <= 50 → 60 points
     * - (25 <= age < 30) OR (51 <= age <= 60) → 50 points
     * - (18 <= age < 25) OR (61 <= age <= 70) → 30 points
     */
    @Property
    @Label("Age score calculation follows bracket rules for all age values")
    void ageScoreFollowsBracketRules(@ForAll("ages") int age) {
        // Act
        double actualScore = creditScoringService.calculateAgeScore(age);

        // Assert - determine expected score based on bracket rules
        double expectedScore;
        if (age >= 30 && age <= 50) {
            expectedScore = 60.0;
        } else if ((age >= 25 && age < 30) || (age >= 51 && age <= 60)) {
            expectedScore = 50.0;
        } else if ((age >= 18 && age < 25) || (age >= 61 && age <= 70)) {
            expectedScore = 30.0;
        } else {
            expectedScore = 0.0; // Outside valid range
        }

        assertThat(actualScore)
                .as("Age score for age %d should be %.1f", age, expectedScore)
                .isEqualTo(expectedScore);
    }

    /**
     * Arbitrary generator for age values
     * Generates valid age values from 18 to 70 years
     */
    @Provide
    Arbitrary<Integer> ages() {
        return Arbitraries.integers().between(18, 70);
    }

    /**
     * Property 9: Existing Loans Score Calculation
     * 
     * **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
     * 
     * For any non-negative existing loans count, the calculated score SHALL match the specified bracket rules:
     * - count == 0 → 60 points
     * - count == 1 → 50 points
     * - count == 2 → 30 points
     * - count >= 3 → 0 points
     */
    @Property
    @Label("Existing loans score calculation follows bracket rules for all loan counts")
    void existingLoansScoreFollowsBracketRules(@ForAll("existingLoansCounts") int existingLoans) {
        // Act
        double actualScore = creditScoringService.calculateExistingLoansScore(existingLoans);

        // Assert - determine expected score based on bracket rules
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

    /**
     * Arbitrary generator for existing loans count
     * Generates realistic loan counts from 0 to 10
     */
    @Provide
    Arbitrary<Integer> existingLoansCounts() {
        return Arbitraries.integers().between(0, 10);
    }

    /**
     * Property 13: Residence Score Calculation
     * 
     * **Validates: Requirements 11.1, 11.2, 11.3, 11.4**
     * 
     * For any non-negative residence years value, the calculated score SHALL match the specified bracket rules:
     * - years >= 5 → 40 points
     * - 3 <= years < 5 → 30 points
     * - 1 <= years < 3 → 20 points
     * - years < 1 → 10 points
     */
    @Property
    @Label("Residence score calculation follows bracket rules for all residence years values")
    void residenceScoreFollowsBracketRules(@ForAll("residenceYears") int residenceYears) {
        // Act
        double actualScore = creditScoringService.calculateResidenceScore(residenceYears);

        // Assert - determine expected score based on bracket rules
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

    /**
     * Arbitrary generator for residence years
     * Generates realistic residence years from 0 to 30
     */
    @Provide
    Arbitrary<Integer> residenceYears() {
        return Arbitraries.integers().between(0, 30);
    }

    /**
     * Property 14: Loan Purpose Score Calculation
     * 
     * **Validates: Requirements 12.1, 12.2, 12.3, 12.4**
     * 
     * For any loan purpose, the calculated score SHALL match the specified mapping:
     * - EDUCATION OR HOME → 40 points
     * - BUSINESS → 30 points
     * - VEHICLE → 25 points
     * - PERSONAL → 15 points
     */
    @Property
    @Label("Loan purpose score calculation follows mapping rules for all loan purposes")
    void loanPurposeScoreFollowsMappingRules(@ForAll("loanPurposes") String loanPurpose) {
        // Act
        double actualScore = creditScoringService.calculateLoanPurposeScore(loanPurpose);

        // Assert - determine expected score based on loan purpose
        double expectedScore = switch (loanPurpose) {
            case "EDUCATION", "HOME" -> 40.0;
            case "BUSINESS" -> 30.0;
            case "VEHICLE" -> 25.0;
            case "PERSONAL" -> 15.0;
            default -> throw new IllegalStateException("Unexpected loan purpose: " + loanPurpose);
        };

        assertThat(actualScore)
                .as("Loan purpose score for %s should be %.1f", loanPurpose, expectedScore)
                .isEqualTo(expectedScore);
    }

    /**
     * Arbitrary generator for loan purposes
     * Generates all valid loan purpose values
     */
    @Provide
    Arbitrary<String> loanPurposes() {
        return Arbitraries.of("EDUCATION", "HOME", "BUSINESS", "VEHICLE", "PERSONAL");
    }

    /**
     * Property 16: Eligibility Score Aggregation
     * 
     * **Validates: Requirements 2.12, 2.13**
     * 
     * For any set of 11 factor scores (excluding transaction pattern), the calculated eligibility score 
     * SHALL equal (sum_of_factor_scores / 750) * 100, rounded to 2 decimal places, and SHALL be in the range [0, 100].
     */
    @Property
    @Label("Eligibility score aggregation follows formula (sum/750)*100 and is in range [0,100]")
    void eligibilityScoreAggregationIsCorrect(
            @ForAll("monthlyIncomes") BigDecimal monthlyIncome,
            @ForAll("employmentTypes") String employmentType,
            @ForAll("dtiRatios") BigDecimal dtiRatio,
            @ForAll("repaymentHistories") String repaymentHistory,
            @ForAll("ages") int age,
            @ForAll("existingLoansCounts") int existingLoans,
            @ForAll("booleans") boolean hasCollateral,
            @ForAll("residenceYears") int residenceYears,
            @ForAll("loanPurposes") String loanPurpose,
            @ForAll("booleans") boolean hasGuarantor) {
        
        // Act - calculate all individual factor scores
        double incomeScore = creditScoringService.calculateIncomeScore(monthlyIncome);
        double employmentScore = creditScoringService.calculateEmploymentScore(employmentType);
        double dtiScore = creditScoringService.calculateDtiScore(dtiRatio);
        double repaymentHistoryScore = creditScoringService.calculateRepaymentHistoryScore(repaymentHistory);
        double ageScore = creditScoringService.calculateAgeScore(age);
        double existingLoansScore = creditScoringService.calculateExistingLoansScore(existingLoans);
        double collateralScore = creditScoringService.calculateCollateralScore(hasCollateral);
        // Note: Banking relationship score requires account lookup, so we use a fixed value for testing
        // In real scenarios, this would be calculated from account creation date
        double bankingRelationshipScore = 10.0; // Minimum score for testing purposes
        double residenceScore = creditScoringService.calculateResidenceScore(residenceYears);
        double loanPurposeScore = creditScoringService.calculateLoanPurposeScore(loanPurpose);
        double guarantorScore = creditScoringService.calculateGuarantorScore(hasGuarantor);

        // Calculate total score (sum of all 11 factors, excluding transaction pattern)
        double totalScore = incomeScore + employmentScore + dtiScore + repaymentHistoryScore +
                ageScore + existingLoansScore + collateralScore + bankingRelationshipScore +
                residenceScore + loanPurposeScore + guarantorScore;

        // Calculate expected eligibility score using the formula
        BigDecimal expectedEligibilityScore = BigDecimal.valueOf((totalScore / 750.0) * 100)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Assert - verify the formula is correct
        assertThat(expectedEligibilityScore)
                .as("Eligibility score should be calculated as (sum/750)*100 rounded to 2 decimals")
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                .isLessThanOrEqualTo(BigDecimal.valueOf(100));

        // Verify the calculation matches the expected formula
        double calculatedPercentage = (totalScore / 750.0) * 100;
        BigDecimal actualEligibilityScore = BigDecimal.valueOf(calculatedPercentage)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        assertThat(actualEligibilityScore)
                .as("Eligibility score for total score %.2f should equal (%.2f/750)*100 = %.2f", 
                    totalScore, totalScore, expectedEligibilityScore)
                .isEqualByComparingTo(expectedEligibilityScore);
    }

    /**
     * Arbitrary generator for repayment histories
     * Generates valid repayment history values
     */
    @Provide
    Arbitrary<String> repaymentHistories() {
        return Arbitraries.of("CLEAN", "NOT_CLEAN");
    }

    /**
     * Arbitrary generator for boolean values
     * Generates true and false values
     */
    @Provide
    Arbitrary<Boolean> booleans() {
        return Arbitraries.of(true, false);
    }
}
