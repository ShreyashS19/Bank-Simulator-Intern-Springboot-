package com.bank.simulator.service;

import com.bank.simulator.dto.CreditScoreResult;
import com.bank.simulator.dto.LoanApplicationRequest;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.TransactionEntity;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditScoringService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Calculate comprehensive credit score based on 12 factors
     */
    public CreditScoreResult calculateCreditScore(LoanApplicationRequest request, String accountNumber) {
        log.info("Calculating credit score for account: {}", accountNumber);

        // Calculate DTI ratio first as it's used in multiple places
        BigDecimal dtiRatio = calculateDtiRatio(request.getExistingEmi(), request.getMonthlyIncome());

        // Calculate all factor scores
        double incomeScore             = calculateIncomeScore(request.getMonthlyIncome());
        double employmentScore         = calculateEmploymentScore(request.getEmploymentType());
        double dtiScore                = calculateDtiScore(dtiRatio);
        double repaymentHistoryScore   = calculateRepaymentHistoryScore(request.getRepaymentHistory());
        double ageScore                = calculateAgeScore(request.getAge());
        double existingLoansScore      = calculateExistingLoansScore(request.getExistingLoans());
        double collateralScore         = calculateCollateralScore(request.getHasCollateral());
        double bankingRelationshipScore= calculateBankingRelationshipScore(accountNumber);
        double residenceScore          = calculateResidenceScore(request.getResidenceYears());
        double loanPurposeScore        = calculateLoanPurposeScore(request.getLoanPurpose());
        double guarantorScore          = calculateGuarantorScore(request.getHasGuarantor());
        // Previously ignored parameters — now correctly scored
        double creditScorePoints       = calculateCreditScorePoints(request.getCreditScore());
        double loanToIncomeScore       = calculateLoanToIncomeScore(request.getLoanAmount(), request.getMonthlyIncome());
        double tenureScore             = calculateTenureScore(request.getLoanTenure());

        // Total max = 120+80+100+100+60+60+70+50+40+40+30+150+60+30 = 990
        double totalScore = incomeScore + employmentScore + dtiScore + repaymentHistoryScore
                + ageScore + existingLoansScore + collateralScore + bankingRelationshipScore
                + residenceScore + loanPurposeScore + guarantorScore
                + creditScorePoints + loanToIncomeScore + tenureScore;

        BigDecimal eligibilityScore = BigDecimal.valueOf((totalScore / 990.0) * 100)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Credit score calculation complete. Eligibility score: {}", eligibilityScore);

        return CreditScoreResult.builder()
                .incomeScore(incomeScore)
                .employmentScore(employmentScore)
                .dtiScore(dtiScore)
                .repaymentHistoryScore(repaymentHistoryScore)
                .ageScore(ageScore)
                .existingLoansScore(existingLoansScore)
                .collateralScore(collateralScore)
                .bankingRelationshipScore(bankingRelationshipScore)
                .residenceScore(residenceScore)
                .loanPurposeScore(loanPurposeScore)
                .guarantorScore(guarantorScore)
                .creditScorePoints(creditScorePoints)
                .loanToIncomeScore(loanToIncomeScore)
                .tenureScore(tenureScore)
                .eligibilityScore(eligibilityScore)
                .dtiRatio(dtiRatio)
                .build();
    }

    /**
     * Calculate income score based on monthly income brackets (Max: 120 points)
     */
    public double calculateIncomeScore(BigDecimal monthlyIncome) {
        if (monthlyIncome.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            return 120.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(75000)) >= 0) {
            return 100.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return 80.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(30000)) >= 0) {
            return 60.0;
        } else if (monthlyIncome.compareTo(BigDecimal.valueOf(20000)) >= 0) {
            return 40.0;
        } else {
            return 20.0;
        }
    }

    /**
     * Calculate employment score based on employment type (Max: 80 points)
     */
    public double calculateEmploymentScore(String employmentType) {
        return switch (employmentType) {
            case "GOVERNMENT" -> 80.0;
            case "SALARIED" -> 70.0;
            case "SELF_EMPLOYED" -> 50.0;
            case "UNEMPLOYED" -> 0.0;
            default -> 0.0;
        };
    }

    /**
     * Calculate DTI score based on debt-to-income ratio (Max: 100 points)
     */
    public double calculateDtiScore(BigDecimal dtiRatio) {
        if (dtiRatio.compareTo(BigDecimal.valueOf(0.20)) < 0) {
            return 100.0;
        } else if (dtiRatio.compareTo(BigDecimal.valueOf(0.30)) < 0) {
            return 80.0;
        } else if (dtiRatio.compareTo(BigDecimal.valueOf(0.40)) < 0) {
            return 60.0;
        } else if (dtiRatio.compareTo(BigDecimal.valueOf(0.50)) < 0) {
            return 40.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculate repayment history score (Max: 100 points)
     */
    public double calculateRepaymentHistoryScore(String repaymentHistory) {
        return "CLEAN".equals(repaymentHistory) ? 100.0 : 0.0;
    }

    /**
     * Calculate age score based on age brackets (Max: 60 points)
     */
    public double calculateAgeScore(int age) {
        if (age >= 30 && age <= 50) {
            return 60.0;
        } else if ((age >= 25 && age <= 29) || (age >= 51 && age <= 60)) {
            return 50.0;
        } else if ((age >= 18 && age <= 24) || (age >= 61 && age <= 70)) {
            return 30.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculate existing loans score based on loan count (Max: 60 points)
     */
    public double calculateExistingLoansScore(int existingLoans) {
        if (existingLoans == 0) {
            return 60.0;
        } else if (existingLoans == 1) {
            return 50.0;
        } else if (existingLoans == 2) {
            return 30.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculate collateral score (Max: 70 points)
     */
    public double calculateCollateralScore(boolean hasCollateral) {
        return hasCollateral ? 70.0 : 0.0;
    }

    /**
     * Calculate banking relationship score based on account age (Max: 50 points)
     */
    public double calculateBankingRelationshipScore(String accountNumber) {
        try {
            AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

            LocalDateTime accountCreated = account.getCreated();
            
            // Handle null creation date - default to 10 points as per requirements 10.1-10.6
            if (accountCreated == null) {
                log.warn("Account creation date is null for account {}, defaulting to 10 points", accountNumber);
                return 10.0;
            }
            
            long monthsSinceCreation = ChronoUnit.MONTHS.between(accountCreated, LocalDateTime.now());

            if (monthsSinceCreation >= 24) {
                return 50.0;
            } else if (monthsSinceCreation >= 12) {
                return 40.0;
            } else if (monthsSinceCreation >= 6) {
                return 25.0;
            } else {
                return 10.0;
            }
        } catch (Exception e) {
            log.error("Error calculating banking relationship score for account {}: {}", accountNumber, e.getMessage(), e);
            return 10.0; // Default to minimum score if account not found
        }
    }

    /**
     * Calculate residence score based on years at current residence (Max: 40 points)
     */
    public double calculateResidenceScore(int residenceYears) {
        if (residenceYears >= 5) {
            return 40.0;
        } else if (residenceYears >= 3) {
            return 30.0;
        } else if (residenceYears >= 1) {
            return 20.0;
        } else {
            return 10.0;
        }
    }

    /**
     * Calculate loan purpose score based on purpose type (Max: 40 points)
     */
    public double calculateLoanPurposeScore(String loanPurpose) {
        return switch (loanPurpose) {
            case "EDUCATION", "HOME" -> 40.0;
            case "BUSINESS" -> 30.0;
            case "VEHICLE" -> 25.0;
            case "PERSONAL" -> 15.0;
            default -> 0.0;
        };
    }

    /**
     * Calculate guarantor score (Max: 30 points)
     */
    public double calculateGuarantorScore(boolean hasGuarantor) {
        return hasGuarantor ? 30.0 : 0.0;
    }

    /**
     * Calculate transaction pattern score based on last 6 months of transactions
     * This is informational only and not included in eligibility score
     * Returns transaction volume and frequency data
     */
    public double calculateTransactionPatternScore(String accountNumber) {
        try {
            // Calculate date 6 months ago
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            
            // Retrieve transactions from last 6 months
            List<TransactionEntity> transactions = transactionRepository
                    .findByAccountNumberAndCreatedDateAfter(accountNumber, sixMonthsAgo);
            
            // Calculate transaction volume (total count)
            int transactionVolume = transactions.size();
            
            // Calculate transaction frequency (transactions per month)
            double transactionFrequency = transactionVolume / 6.0;
            
            log.info("Transaction pattern for account {}: volume={}, frequency={} per month", 
                    accountNumber, transactionVolume, transactionFrequency);
            
            // Return transaction volume as informational data
            // This is not scored but can be used for display purposes
            return transactionVolume;
            
        } catch (Exception e) {
            log.error("Error calculating transaction pattern score for account {}: {}", 
                    accountNumber, e.getMessage());
            // Skip analysis if transaction retrieval fails
            return 0.0;
        }
    }

    /**
     * Calculate DTI ratio (existing EMI / monthly income)
     */
    public BigDecimal calculateDtiRatio(BigDecimal existingEmi, BigDecimal monthlyIncome) {
        if (monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return existingEmi.divide(monthlyIncome, 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate CIBIL credit score factor (Max: 150 points)
     * This is the most important single factor in real banking.
     * Range: 300–900
     */
    public double calculateCreditScorePoints(int creditScore) {
        if (creditScore >= 800) {
            return 150.0;
        } else if (creditScore >= 750) {
            return 120.0;
        } else if (creditScore >= 700) {
            return 90.0;
        } else if (creditScore >= 650) {
            return 60.0;
        } else if (creditScore >= 600) {
            return 30.0;
        } else {
            return 0.0;  // < 600 CIBIL — very poor credit
        }
    }

    /**
     * Calculate Loan-to-Income (LTI) ratio score (Max: 60 points)
     * loanAmount / (monthlyIncome * 12) = annual LTI
     * High LTI = higher default risk
     */
    public double calculateLoanToIncomeScore(BigDecimal loanAmount, BigDecimal monthlyIncome) {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal annualIncome = monthlyIncome.multiply(BigDecimal.valueOf(12));
        double lti = loanAmount.divide(annualIncome, 4, RoundingMode.HALF_UP).doubleValue();

        if (lti < 2.0) {
            return 60.0;   // Loan < 2x annual income — very safe
        } else if (lti < 4.0) {
            return 45.0;   // 2x–4x annual income — acceptable
        } else if (lti < 6.0) {
            return 25.0;   // 4x–6x annual income — stretched
        } else if (lti < 8.0) {
            return 10.0;   // 6x–8x annual income — risky
        } else {
            return 0.0;    // > 8x annual income — too high
        }
    }

    /**
     * Calculate tenure risk score (Max: 30 points)
     * Shorter tenure = less risk; longer tenure = higher exposure
     * Tenure in months
     */
    public double calculateTenureScore(int tenureMonths) {
        if (tenureMonths <= 36) {
            return 30.0;   // Up to 3 years — low risk
        } else if (tenureMonths <= 84) {
            return 20.0;   // 3–7 years — moderate
        } else if (tenureMonths <= 180) {
            return 10.0;   // 7–15 years — higher exposure
        } else {
            return 5.0;    // > 15 years — maximum exposure
        }
    }
}
