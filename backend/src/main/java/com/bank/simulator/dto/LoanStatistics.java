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
public class LoanStatistics {

    private Long totalApplications;
    private Long approvedCount;
    private Long rejectedCount;
    private Long underReviewCount;
    private Long pendingCount;
    private BigDecimal totalApprovedAmount;
    private Double averageEligibilityScore;
}
