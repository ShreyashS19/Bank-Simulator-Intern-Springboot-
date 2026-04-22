package com.bank.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoreResult {

    private Double incomeScore;
    private Double employmentScore;
    private Double dtiScore;
    private Double repaymentHistoryScore;
    private Double ageScore;
    private Double existingLoansScore;
    private Double collateralScore;
    private Double bankingRelationshipScore;
    private Double residenceScore;
    private Double loanPurposeScore;
    private Double guarantorScore;
    private Double creditScorePoints;      // CIBIL score factor (max 150)
    private Double loanToIncomeScore;      // Loan amount vs annual income (max 60)
    private Double tenureScore;            // Loan tenure risk factor (max 30)
    private BigDecimal eligibilityScore;
    private BigDecimal dtiRatio;
}
