package com.bank.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private String loanId;
    private String accountNumber;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private Integer loanTenure;
    private BigDecimal eligibilityScore;
    private BigDecimal dtiRatio;
    private String status;
    private BigDecimal interestRate;
    private BigDecimal emi;
    private String rejectionReason;
    private List<String> improvementTips;
    private LocalDateTime applicationDate;
    private LocalDateTime lastUpdated;
    private FactorScores factorScores;
}
