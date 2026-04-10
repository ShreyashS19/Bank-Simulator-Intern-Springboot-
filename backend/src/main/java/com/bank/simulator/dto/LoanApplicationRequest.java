package com.bank.simulator.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000", message = "Loan amount must be at least 10,000 INR")
    @DecimalMax(value = "10000000", message = "Loan amount must not exceed 10,000,000 INR")
    private BigDecimal loanAmount;

    @NotBlank(message = "Loan purpose is required")
    @Pattern(regexp = "EDUCATION|HOME|BUSINESS|PERSONAL|VEHICLE", message = "Loan purpose must be one of: EDUCATION, HOME, BUSINESS, PERSONAL, VEHICLE")
    private String loanPurpose;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 6, message = "Loan tenure must be at least 6 months")
    @Max(value = 360, message = "Loan tenure must not exceed 360 months")
    private Integer loanTenure;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    @NotBlank(message = "Employment type is required")
    @Pattern(regexp = "SALARIED|SELF_EMPLOYED|GOVERNMENT|UNEMPLOYED", message = "Employment type must be one of: SALARIED, SELF_EMPLOYED, GOVERNMENT, UNEMPLOYED")
    private String employmentType;

    @NotNull(message = "Existing EMI is required")
    @DecimalMin(value = "0", message = "Existing EMI cannot be negative")
    private BigDecimal existingEmi;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score must not exceed 900")
    private Integer creditScore;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18 years")
    @Max(value = 70, message = "Age must not exceed 70 years")
    private Integer age;

    @NotNull(message = "Existing loans count is required")
    @Min(value = 0, message = "Existing loans count cannot be negative")
    private Integer existingLoans;

    @NotNull(message = "Collateral information is required")
    private Boolean hasCollateral;

    @NotNull(message = "Residence years is required")
    @Min(value = 0, message = "Residence years cannot be negative")
    private Integer residenceYears;

    @NotNull(message = "Guarantor information is required")
    private Boolean hasGuarantor;

    @NotBlank(message = "Repayment history is required")
    @Pattern(regexp = "CLEAN|NOT_CLEAN", message = "Repayment history must be either CLEAN or NOT_CLEAN")
    private String repaymentHistory;
}
