package com.bank.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactorScores {

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
}
