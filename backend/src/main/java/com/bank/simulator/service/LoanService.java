package com.bank.simulator.service;

import com.bank.simulator.dto.*;
import com.bank.simulator.entity.AccountEntity;
import com.bank.simulator.entity.LoanEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.AccountRepository;
import com.bank.simulator.repository.LoanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final CreditScoringService creditScoringService;
    private final NotificationService notificationService;
    private final LoanPdfService loanPdfService;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;
    
    private final Random random = new Random();

    /**
     * Generate unique loan ID in format LOAN-XXXXXXXX
     * Ensures uniqueness by checking repository (retry up to 3 times if collision)
     * Requirements: 1.2
     */
    public String generateLoanId() {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            // Generate 8 random digits
            int randomDigits = 10000000 + random.nextInt(90000000);
            String loanId = "LOAN-" + randomDigits;
            
            // Check if this ID already exists
            if (loanRepository.findByLoanId(loanId).isEmpty()) {
                return loanId;
            }
            
            log.warn("Loan ID collision detected: {}. Retrying... (attempt {}/{})", 
                     loanId, attempt + 1, maxRetries);
        }
        
        // If all retries failed, throw exception
        throw new BusinessException("Failed to generate unique loan ID after " + maxRetries + " attempts");
    }

    /**
     * Process loan application with automated credit scoring and decision logic
     * Requirements: 1.1, 1.2, 1.9, 1.10, 18.1, 18.2, 18.3, 18.4, 18.5, 27.2, 27.3
     */
    @Transactional
    public LoanEligibilityResultDto applyForLoan(LoanApplicationRequest request, String accountNumber) {
        log.info("Processing loan application for account: {}", accountNumber);

        String loanId = generateLoanId();
        String referenceNumber = generateReferenceNumber();
        log.info("Generated loan id {} and reference number {}", loanId, referenceNumber);

        CreditScoreResult scoreResult = creditScoringService.calculateCreditScore(request, accountNumber);
        BigDecimal eligibilityScore = scoreResult.getEligibilityScore();
        String eligibilityStatus = eligibilityScore.compareTo(BigDecimal.valueOf(65.0)) >= 0
                ? "ELIGIBLE"
                : "NOT_ELIGIBLE";

        String advisoryStatus = determineStatus(eligibilityScore.doubleValue(), scoreResult.getDtiRatio());
        String rejectionReason = generateRejectionReason(scoreResult, advisoryStatus);
        List<String> improvementTips = generateImprovementTips(scoreResult, advisoryStatus);

        String improvementTipsJson;
        try {
            improvementTipsJson = objectMapper.writeValueAsString(improvementTips);
        } catch (JsonProcessingException ex) {
            log.error("Error converting improvement tips to JSON", ex);
            improvementTipsJson = "[]";
        }

        LoanEntity loanEntity = LoanEntity.builder()
                .loanId(loanId)
                .accountNumber(accountNumber)
                .loanAmount(request.getLoanAmount())
                .loanPurpose(request.getLoanPurpose())
                .loanTenure(request.getLoanTenure())
                .monthlyIncome(request.getMonthlyIncome())
                .employmentType(request.getEmploymentType())
                .existingEmi(request.getExistingEmi())
                .creditScore(request.getCreditScore())
                .age(request.getAge())
                .existingLoans(request.getExistingLoans())
                .hasCollateral(request.getHasCollateral())
                .residenceYears(request.getResidenceYears())
                .hasGuarantor(request.getHasGuarantor())
                .repaymentHistory(request.getRepaymentHistory())
                .incomeScore(scoreResult.getIncomeScore())
                .employmentScore(scoreResult.getEmploymentScore())
                .dtiScore(scoreResult.getDtiScore())
                .repaymentHistoryScore(scoreResult.getRepaymentHistoryScore())
                .ageScore(scoreResult.getAgeScore())
                .existingLoansScore(scoreResult.getExistingLoansScore())
                .collateralScore(scoreResult.getCollateralScore())
                .bankingRelationshipScore(scoreResult.getBankingRelationshipScore())
                .residenceScore(scoreResult.getResidenceScore())
                .loanPurposeScore(scoreResult.getLoanPurposeScore())
                .guarantorScore(scoreResult.getGuarantorScore())
                .creditScorePoints(scoreResult.getCreditScorePoints())
                .loanToIncomeScore(scoreResult.getLoanToIncomeScore())
                .tenureScore(scoreResult.getTenureScore())
                .eligibilityScore(eligibilityScore)
                .dtiRatio(scoreResult.getDtiRatio())
                .status("PENDING_BANK_REVIEW")
                .referenceNumber(referenceNumber)
                .eligibilityStatus(eligibilityStatus)
                .interestRate(BigDecimal.ZERO)
                .emi(BigDecimal.ZERO)
                .rejectionReason(rejectionReason)
                .improvementTips(improvementTipsJson)
                .build();

        LoanEntity savedEntity = loanRepository.save(loanEntity);
        log.info("Loan advisory saved with id {} and reference {}", savedEntity.getId(), referenceNumber);

        AccountEntity accountEntity = accountRepository.findByAccountNumberWithCustomer(accountNumber)
                .orElseThrow(() -> new BusinessException("Account not found for account number: " + accountNumber));

        LoanEligibilityResultDto result = buildEligibilityResultFromLoan(savedEntity, accountEntity);

        try {
            notificationService.sendEligibilityResultEmail(result);
        } catch (Exception ex) {
            log.error("Failed to send eligibility email for reference {}", referenceNumber, ex);
        }

        try {
            loanPdfService.generateEligibilityPdf(result);
        } catch (Exception ex) {
            log.warn("Could not pre-generate eligibility PDF for reference {}: {}", referenceNumber, ex.getMessage());
        }

        return result;
    }

    public LoanEligibilityResultDto getEligibilityResultByReferenceNumber(String referenceNumber) {
        LoanEntity loanEntity = loanRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BusinessException("Loan not found for reference number: " + referenceNumber));

        AccountEntity accountEntity = accountRepository.findByAccountNumberWithCustomer(loanEntity.getAccountNumber())
                .orElseThrow(() -> new BusinessException("Account not found for account number: " + loanEntity.getAccountNumber()));

        return buildEligibilityResultFromLoan(loanEntity, accountEntity);
    }

    private LoanEligibilityResultDto buildEligibilityResultFromLoan(LoanEntity loanEntity, AccountEntity accountEntity) {
        String customerName = accountEntity.getCustomer() != null ? accountEntity.getCustomer().getName() : "Customer";
        String customerEmail = accountEntity.getCustomer() != null
                ? accountEntity.getCustomer().getEmail()
                : accountRepository.findCustomerEmailByAccountNumber(loanEntity.getAccountNumber()).orElse("");

        String eligibilityStatus = loanEntity.getEligibilityStatus() != null
                ? loanEntity.getEligibilityStatus()
                : (loanEntity.getEligibilityScore().compareTo(BigDecimal.valueOf(65.0)) >= 0 ? "ELIGIBLE" : "NOT_ELIGIBLE");

        List<String> improvementTips = List.of();
        try {
            if (loanEntity.getImprovementTips() != null && !loanEntity.getImprovementTips().isEmpty()) {
                improvementTips = objectMapper.readValue(loanEntity.getImprovementTips(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing improvement tips JSON", e);
        }

        return LoanEligibilityResultDto.builder()
                .referenceNumber(loanEntity.getReferenceNumber())
                .eligibilityStatus(eligibilityStatus)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .loanAmount(loanEntity.getLoanAmount())
                .loanPurpose(loanEntity.getLoanPurpose())
                .loanTenure(loanEntity.getLoanTenure())
                .eligibilityScore(loanEntity.getEligibilityScore())
                .eligibilityMessage(buildEligibilityMessage(eligibilityStatus, loanEntity.getEligibilityScore()))
                .requiredDocuments(buildRequiredDocuments(loanEntity.getLoanPurpose()))
                .specialNotes(buildSpecialNotes(loanEntity.getReferenceNumber()))
                .improvementTips(improvementTips)
                .generatedAt(loanEntity.getApplicationDate() != null ? loanEntity.getApplicationDate() : LocalDateTime.now())
                .pdfDownloadPath("/loan/pdf/" + loanEntity.getReferenceNumber())
                .build();
    }

    private String generateReferenceNumber() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int maxRetries = 5;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            StringBuilder suffix = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                suffix.append(characters.charAt(random.nextInt(characters.length())));
            }

            String referenceNumber = "LN-" + LocalDate.now().getYear() + "-" + suffix;
            if (loanRepository.findByReferenceNumber(referenceNumber).isEmpty()) {
                return referenceNumber;
            }
        }

        throw new BusinessException("Failed to generate unique reference number");
    }

    private String buildEligibilityMessage(String eligibilityStatus, BigDecimal eligibilityScore) {
        String scoreText = eligibilityScore != null ? eligibilityScore.toPlainString() : "0";
        if ("ELIGIBLE".equalsIgnoreCase(eligibilityStatus)) {
            return "Your profile cleared our preliminary eligibility checks with score " + scoreText
                    + ". Final approval and final terms will be confirmed by a bank officer after document verification.";
        }
        return "Your profile currently falls below the preferred threshold (score " + scoreText
                + "). You may still visit the branch with all documents for manual review and final decision.";
    }

    private List<String> buildRequiredDocuments(String loanPurpose) {
        List<String> documents = new ArrayList<>(List.of(
                "Aadhaar Card (original + 2 photocopies)",
                "PAN Card (original + 2 photocopies)",
                "Recent passport-size photographs (4 copies)",
                "Last 6 months bank statement",
                "Income proof (salary slips last 3 months OR ITR last 2 years)"
        ));

        String purpose = loanPurpose == null ? "" : loanPurpose.toUpperCase(Locale.ROOT);
        switch (purpose) {
            case "HOME", "HOME_LOAN" -> documents.addAll(List.of(
                    "Property documents / Sale deed",
                    "NOC from builder or society",
                    "Approved building plan copy",
                    "Property tax receipt"
            ));
            case "PERSONAL", "PERSONAL_LOAN" -> documents.addAll(List.of(
                    "Employment proof (offer letter or ID card)",
                    "Last 3 months salary slips"
            ));
            case "BUSINESS", "BUSINESS_LOAN" -> documents.addAll(List.of(
                    "Business registration certificate",
                    "GST registration certificate",
                    "Last 2 years business ITR",
                    "Balance sheet and P&L statement"
            ));
            case "EDUCATION", "EDUCATION_LOAN" -> documents.addAll(List.of(
                    "Admission letter from institution",
                    "Fee structure document",
                    "Institution accreditation proof"
            ));
            default -> {
                // Keep common documents only for unmatched purpose values.
            }
        }

        return documents;
    }

    private List<String> buildSpecialNotes(String referenceNumber) {
        String safeReference = referenceNumber == null ? "N/A" : referenceNumber;
        return List.of(
                "This is a PRELIMINARY check only.",
                "Final approval is at bank discretion after physical verification.",
                "This advisory is valid for 30 days from issue date.",
                "Quote reference number " + safeReference + " at the branch."
        );
    }
    
    /**
     * Convert LoanEntity to LoanResponse DTO
     */
    private LoanResponse convertToLoanResponse(LoanEntity entity) {
        // Convert JSON string back to List<String> for improvementTips
        List<String> improvementTips = List.of();
        try {
            if (entity.getImprovementTips() != null && !entity.getImprovementTips().isEmpty()) {
                improvementTips = objectMapper.readValue(entity.getImprovementTips(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing improvement tips JSON", e);
        }
        
        // Build FactorScores object
        FactorScores factorScores = FactorScores.builder()
                .incomeScore(entity.getIncomeScore())
                .employmentScore(entity.getEmploymentScore())
                .dtiScore(entity.getDtiScore())
                .repaymentHistoryScore(entity.getRepaymentHistoryScore())
                .ageScore(entity.getAgeScore())
                .existingLoansScore(entity.getExistingLoansScore())
                .collateralScore(entity.getCollateralScore())
                .bankingRelationshipScore(entity.getBankingRelationshipScore())
                .residenceScore(entity.getResidenceScore())
                .loanPurposeScore(entity.getLoanPurposeScore())
                .guarantorScore(entity.getGuarantorScore())
                .creditScorePoints(entity.getCreditScorePoints())
                .loanToIncomeScore(entity.getLoanToIncomeScore())
                .tenureScore(entity.getTenureScore())
                .build();
        
        return LoanResponse.builder()
                .loanId(entity.getLoanId())
                .accountNumber(entity.getAccountNumber())
                .loanAmount(entity.getLoanAmount())
                .loanPurpose(entity.getLoanPurpose())
                .loanTenure(entity.getLoanTenure())
                .eligibilityScore(entity.getEligibilityScore())
                .dtiRatio(entity.getDtiRatio())
                .status(entity.getStatus())
                .referenceNumber(entity.getReferenceNumber())
                .eligibilityStatus(entity.getEligibilityStatus())
                .interestRate(entity.getInterestRate())
                .emi(entity.getEmi())
                .rejectionReason(entity.getRejectionReason())
                .improvementTips(improvementTips)
                .applicationDate(entity.getApplicationDate())
                .lastUpdated(entity.getLastUpdated())
                .factorScores(factorScores)
                .build();
    }

    /**
     * Determine loan status based on eligibility score and DTI ratio
     * Requirements: 14.1, 14.2, 14.3
     */
    public String determineStatus(double eligibilityScore, BigDecimal dtiRatio) {
        // Convert eligibilityScore to BigDecimal for comparison
        BigDecimal score = BigDecimal.valueOf(eligibilityScore);
        
        // IF eligibility_score >= 750 AND dti_ratio < 0.40 THEN APPROVED
        if (score.compareTo(BigDecimal.valueOf(75.0)) >= 0 && 
            dtiRatio.compareTo(BigDecimal.valueOf(0.40)) < 0) {
            return "APPROVED";
        }
        
        // ELSE IF (650 <= eligibility_score < 750) OR (0.40 <= dti_ratio <= 0.50) THEN UNDER_REVIEW
        boolean scoreInReviewRange = score.compareTo(BigDecimal.valueOf(65.0)) >= 0 && 
                                      score.compareTo(BigDecimal.valueOf(75.0)) < 0;
        boolean dtiInReviewRange = dtiRatio.compareTo(BigDecimal.valueOf(0.40)) >= 0 && 
                                   dtiRatio.compareTo(BigDecimal.valueOf(0.50)) <= 0;
        
        if (scoreInReviewRange || dtiInReviewRange) {
            return "UNDER_REVIEW";
        }
        
        // ELSE IF eligibility_score < 650 OR dti_ratio > 0.50 THEN REJECTED
        return "REJECTED";
    }

    /**
     * Assign interest rate based on eligibility score and status
     * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5
     */
    public BigDecimal assignInterestRate(double eligibilityScore, String status) {
        // REJECTED → 0.0%
        if ("REJECTED".equals(status)) {
            return BigDecimal.ZERO;
        }
        
        // score >= 800 → 7.5%
        if (eligibilityScore >= 80.0) {
            return BigDecimal.valueOf(7.5);
        }
        
        // score >= 750 → 8.5%
        if (eligibilityScore >= 75.0) {
            return BigDecimal.valueOf(8.5);
        }
        
        // score >= 700 → 10.0%
        if (eligibilityScore >= 70.0) {
            return BigDecimal.valueOf(10.0);
        }
        
        // score >= 650 → 12.0%
        if (eligibilityScore >= 65.0) {
            return BigDecimal.valueOf(12.0);
        }
        
        // Default to 12.0% for scores below 650 (shouldn't happen for non-rejected)
        return BigDecimal.valueOf(12.0);
    }

    /**
     * Calculate EMI using standard loan amortization formula
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     * Requirements: 16.1, 16.2, 16.3, 16.4, 16.5
     */
    public BigDecimal calculateEmi(BigDecimal loanAmount, BigDecimal interestRate, int tenure) {
        // Handle edge case: return 0 if interest rate is 0 (rejected loans)
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // P = loan amount
        BigDecimal P = loanAmount;
        
        // r = monthly interest rate = (annual_rate / 12) / 100
        BigDecimal r = interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                                   .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        // n = tenure in months
        int n = tenure;
        
        // Calculate (1 + r)^n
        BigDecimal onePlusR = BigDecimal.ONE.add(r);
        BigDecimal onePlusRPowerN = onePlusR.pow(n);
        
        // Calculate numerator: P * r * (1+r)^n
        BigDecimal numerator = P.multiply(r).multiply(onePlusRPowerN);
        
        // Calculate denominator: (1+r)^n - 1
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
        
        // Calculate EMI = numerator / denominator
        BigDecimal emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        
        return emi;
    }

    /**
     * Generate rejection reason based on weak factors
     * Requirements: 14.4
     */
    public String generateRejectionReason(CreditScoreResult scoreResult, String status) {
        // Return empty string if status is not REJECTED
        if (!"REJECTED".equals(status)) {
            return "";
        }
        
        // Identify factors scoring below 50% of maximum
        StringBuilder reason = new StringBuilder("Loan application rejected due to: ");
        boolean hasWeakFactors = false;
        
        // Check each factor against 50% threshold
        if (scoreResult.getIncomeScore() < 60.0) { // 50% of 120
            reason.append("Low income, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getEmploymentScore() < 40.0) { // 50% of 80
            reason.append("Unstable employment, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getDtiScore() < 50.0) { // 50% of 100
            reason.append("High debt-to-income ratio, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getRepaymentHistoryScore() < 50.0) { // 50% of 100
            reason.append("Poor repayment history, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getAgeScore() < 30.0) { // 50% of 60
            reason.append("Age factor, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getExistingLoansScore() < 30.0) { // 50% of 60
            reason.append("Too many existing loans, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getCollateralScore() < 35.0) { // 50% of 70
            reason.append("No collateral, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getBankingRelationshipScore() < 25.0) { // 50% of 50
            reason.append("Short banking relationship, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getResidenceScore() < 20.0) { // 50% of 40
            reason.append("Unstable residence, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getLoanPurposeScore() < 20.0) { // 50% of 40
            reason.append("High-risk loan purpose, ");
            hasWeakFactors = true;
        }
        if (scoreResult.getGuarantorScore() < 15.0) { // 50% of 30
            reason.append("No guarantor, ");
            hasWeakFactors = true;
        }
        
        if (!hasWeakFactors) {
            return "Loan application rejected due to overall low eligibility score.";
        }
        
        // Remove trailing comma and space
        String finalReason = reason.toString();
        if (finalReason.endsWith(", ")) {
            finalReason = finalReason.substring(0, finalReason.length() - 2) + ".";
        }
        
        return finalReason;
    }

    /**
     * Generate improvement tips for weak factors
     * Requirements: 17.1, 17.2, 17.3, 17.4
     */
    public List<String> generateImprovementTips(CreditScoreResult scoreResult, String status) {
        // Return empty list if status is APPROVED
        if ("APPROVED".equals(status)) {
            return List.of();
        }
        
        List<String> tips = new java.util.ArrayList<>();
        
        // For each factor scoring below 50% of maximum, add corresponding tip
        if (scoreResult.getIncomeScore() < 60.0) { // 50% of 120
            tips.add("Increase your monthly income to improve eligibility. Consider additional income sources.");
        }
        if (scoreResult.getEmploymentScore() < 40.0) { // 50% of 80
            tips.add("Stable employment (salaried or government) improves your score significantly.");
        }
        if (scoreResult.getDtiScore() < 50.0) { // 50% of 100
            tips.add("Reduce existing EMI obligations to lower your debt-to-income ratio.");
        }
        if (scoreResult.getRepaymentHistoryScore() < 50.0) { // 50% of 100
            tips.add("Maintain a clean repayment history by paying all dues on time.");
        }
        if (scoreResult.getAgeScore() < 30.0) { // 50% of 60
            tips.add("Your age group affects eligibility. Optimal age range is 30-50 years.");
        }
        if (scoreResult.getExistingLoansScore() < 30.0) { // 50% of 60
            tips.add("Reduce the number of existing loans before applying.");
        }
        if (scoreResult.getCollateralScore() < 35.0) { // 50% of 70
            tips.add("Providing collateral can significantly improve your loan eligibility.");
        }
        if (scoreResult.getBankingRelationshipScore() < 25.0) { // 50% of 50
            tips.add("Build a longer banking relationship by maintaining your account.");
        }
        if (scoreResult.getResidenceScore() < 20.0) { // 50% of 40
            tips.add("Longer residence at current address indicates stability.");
        }
        if (scoreResult.getLoanPurposeScore() < 20.0) { // 50% of 40
            tips.add("Education and home loans receive higher scores due to lower risk.");
        }
        if (scoreResult.getGuarantorScore() < 15.0) { // 50% of 30
            tips.add("Having a guarantor can improve your loan eligibility.");
        }
        
        return tips;
    }

    /**
     * Retrieve loans by account number
     * Requirements: 19.1, 19.2, 19.3
     */
    public List<LoanResponse> getLoansByAccount(String accountNumber) {
        log.info("Retrieving loans for account: {}", accountNumber);
        
        List<LoanEntity> loans = loanRepository.findByAccountNumberOrderByApplicationDateDesc(accountNumber);
        
        return loans.stream()
                .map(this::convertToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve loan by ID
     * Requirements: 20.1, 20.2, 20.3
     */
    public LoanResponse getLoanById(String loanId) {
        log.info("Retrieving loan by ID: {}", loanId);
        
        LoanEntity loan = loanRepository.findByLoanId(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found with ID: " + loanId));
        
        return convertToLoanResponse(loan);
    }

    /**
     * Retrieve all loans (admin)
     * Requirements: 21.1, 21.2, 21.3
     */
    public List<LoanResponse> getAllLoans() {
        log.info("Retrieving all loans (admin)");
        
        List<LoanEntity> loans = loanRepository.findAllByOrderByApplicationDateDesc();
        
        return loans.stream()
                .map(this::convertToLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update loan status (admin)
     * Requirements: 22.1, 22.2, 22.3, 22.4, 22.5
     */
    @Transactional
    public LoanResponse updateLoanStatus(String loanId, UpdateLoanStatusRequest request) {
        log.info("Updating loan status for ID: {} to {}", loanId, request.getStatus());
        
        // Retrieve loan by ID, throw exception if not found
        LoanEntity loan = loanRepository.findByLoanId(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found with ID: " + loanId));
        
        // Update status and lastUpdated timestamp (lastUpdated is handled by @UpdateTimestamp)
        loan.setStatus(request.getStatus());
        
        // Save to database
        LoanEntity updatedLoan = loanRepository.save(loan);
        log.info("Loan status updated successfully for ID: {}", loanId);
        
        // Send email notification using NotificationService
        try {
            String emailSubject = "Loan Status Update - " + loanId;
            String emailBody = buildStatusUpdateEmailBody(loan);
            
            // Retrieve customer email from account repository
            Optional<String> customerEmailOpt = accountRepository.findCustomerEmailByAccountNumber(loan.getAccountNumber());
            if (customerEmailOpt.isPresent()) {
                String customerEmail = customerEmailOpt.get();
                boolean sent = notificationService.sendNotification(customerEmail, emailSubject, emailBody);
                if (sent) {
                    log.info("Email notification sent successfully for loan status update {}", loanId);
                } else {
                    log.warn("Failed to send email notification for loan status update {}", loanId);
                }
            } else {
                log.warn("Customer email not found for account {}", loan.getAccountNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send email notification for loan status update {}: {}", loanId, e.getMessage());
            // Don't fail the update if email fails
        }
        
        return convertToLoanResponse(updatedLoan);
    }

    /**
     * Build email body for loan status update notification
     */
    private String buildStatusUpdateEmailBody(LoanEntity loan) {
        StringBuilder body = new StringBuilder();
        body.append("Dear Customer,\n\n");
        body.append("Your loan application (").append(loan.getLoanId()).append(") status has been updated.\n\n");
        body.append("New Status: ").append(loan.getStatus()).append("\n");
        body.append("Loan Amount: ").append(loan.getLoanAmount()).append(" INR\n");
        
        if ("APPROVED".equals(loan.getStatus())) {
            body.append("\nCongratulations! Your loan has been approved.\n");
            body.append("Interest Rate: ").append(loan.getInterestRate()).append("%\n");
            body.append("Monthly EMI: ").append(loan.getEmi()).append(" INR\n");
        } else if ("REJECTED".equals(loan.getStatus())) {
            body.append("\nUnfortunately, your loan application has been rejected.\n");
            if (loan.getRejectionReason() != null && !loan.getRejectionReason().isEmpty()) {
                body.append("Reason: ").append(loan.getRejectionReason()).append("\n");
            }
        } else if ("UNDER_REVIEW".equals(loan.getStatus())) {
            body.append("\nYour loan application is currently under review.\n");
            body.append("We will notify you once a decision is made.\n");
        }
        
        body.append("\nThank you for choosing our bank.\n");
        body.append("Best regards,\nLoan Department");
        
        return body.toString();
    }

    /**
     * Get loan statistics (admin)
     * Requirements: 23.1, 23.2, 23.3, 23.4, 23.5
     */
    public LoanStatistics getLoanStatistics() {
        log.info("Calculating loan statistics (admin)");
        
        // Calculate total applications count
        Long totalApplications = loanRepository.count();
        
        // Calculate counts by status
        Long approvedCount = loanRepository.countByStatus("APPROVED");
        Long rejectedCount = loanRepository.countByStatus("REJECTED");
        Long underReviewCount = loanRepository.countByStatus("UNDER_REVIEW");
        Long pendingCount = loanRepository.countByStatus("PENDING")
            + loanRepository.countByStatus("PENDING_BANK_REVIEW");
        
        // Calculate total approved amount
        BigDecimal totalApprovedAmount = loanRepository.sumLoanAmountByStatus("APPROVED");
        
        // Calculate average eligibility score
        Double averageEligibilityScore = loanRepository.avgEligibilityScore();
        
        return LoanStatistics.builder()
                .totalApplications(totalApplications)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .underReviewCount(underReviewCount)
                .pendingCount(pendingCount)
                .totalApprovedAmount(totalApprovedAmount)
                .averageEligibilityScore(averageEligibilityScore)
                .build();
    }
}
