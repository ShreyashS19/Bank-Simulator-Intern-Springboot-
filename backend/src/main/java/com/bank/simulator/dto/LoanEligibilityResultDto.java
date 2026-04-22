package com.bank.simulator.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanEligibilityResultDto {

    private String referenceNumber;
    private String eligibilityStatus;
    private String customerName;
    private String customerEmail;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private Integer loanTenure;
    private BigDecimal eligibilityScore;
    private String eligibilityMessage;
    private List<String> requiredDocuments;
    private List<String> specialNotes;
    private List<String> improvementTips;
    private LocalDateTime generatedAt;
    private String pdfDownloadPath;
}
