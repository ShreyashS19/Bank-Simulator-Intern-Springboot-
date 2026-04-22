package com.bank.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = "loan_id")
}, indexes = {
    @Index(name = "idx_account_number", columnList = "account_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_reference_number", columnList = "reference_number"),
    @Index(name = "idx_application_date", columnList = "application_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false, unique = true, length = 20)
    private String loanId;

    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "loan_purpose", nullable = false, length = 20)
    private String loanPurpose;

    @Column(name = "loan_tenure", nullable = false)
    private Integer loanTenure;

    @Column(name = "monthly_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "employment_type", nullable = false, length = 20)
    private String employmentType;

    @Column(name = "existing_emi", nullable = false, precision = 15, scale = 2)
    private BigDecimal existingEmi;


    @Column(name = "credit_score", nullable = false)
    private Integer creditScore;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "existing_loans", nullable = false)
    private Integer existingLoans;

    @Column(name = "has_collateral", nullable = false)
    private Boolean hasCollateral;

    @Column(name = "residence_years", nullable = false)
    private Integer residenceYears;

    @Column(name = "has_guarantor", nullable = false)
    private Boolean hasGuarantor;

    @Column(name = "repayment_history", nullable = false, length = 20)
    private String repaymentHistory;

    // Factor Scores (12 fields)
    @Column(name = "income_score", nullable = false)
    private Double incomeScore;

    @Column(name = "employment_score", nullable = false)
    private Double employmentScore;

    @Column(name = "dti_score", nullable = false)
    private Double dtiScore;

    @Column(name = "repayment_history_score", nullable = false)
    private Double repaymentHistoryScore;

    @Column(name = "age_score", nullable = false)
    private Double ageScore;

    @Column(name = "existing_loans_score", nullable = false)
    private Double existingLoansScore;

    @Column(name = "collateral_score", nullable = false)
    private Double collateralScore;

    @Column(name = "banking_relationship_score", nullable = false)
    private Double bankingRelationshipScore;

    @Column(name = "residence_score", nullable = false)
    private Double residenceScore;

    @Column(name = "loan_purpose_score", nullable = false)
    private Double loanPurposeScore;

    @Column(name = "guarantor_score", nullable = false)
    private Double guarantorScore;

    @Column(name = "credit_score_points", nullable = false)
    private Double creditScorePoints;

    @Column(name = "loan_to_income_score", nullable = false)
    private Double loanToIncomeScore;

    @Column(name = "tenure_score", nullable = false)
    private Double tenureScore;

    @Column(name = "eligibility_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal eligibilityScore;

    @Column(name = "dti_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal dtiRatio;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reference_number", length = 30)
    private String referenceNumber;

    @Column(name = "eligibility_status", length = 20)
    private String eligibilityStatus;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "emi", precision = 15, scale = 2)
    private BigDecimal emi;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "improvement_tips", columnDefinition = "TEXT")
    private String improvementTips;

    @CreationTimestamp
    @Column(name = "application_date", nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
