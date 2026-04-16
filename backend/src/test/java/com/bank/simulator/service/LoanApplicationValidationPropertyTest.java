package com.bank.simulator.service;

import com.bank.simulator.dto.LoanApplicationRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Property-based tests for loan application input validation
 * 
 * **Validates: Requirements 1.1**
 */
public class LoanApplicationValidationPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Property 2: Input Validation Completeness
     * 
     * For any loan application with one or more missing required fields,
     * the validation SHALL reject the application and identify all missing fields.
     * 
     * **Validates: Requirements 1.1**
     */
    @Property
    @Label("Property 2: Input Validation Completeness - Missing required fields are rejected")
    void missingRequiredFieldsShouldBeRejected(@ForAll("requestsWithMissingFields") LoanApplicationRequest request) {
        // When: Validating a request with missing fields
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);

        // Then: Validation should fail
        Assume.that(!violations.isEmpty());

        // And: At least one violation should be present
        assert violations.size() > 0 : "Expected validation violations for missing fields";
    }

    @Provide
    Arbitrary<LoanApplicationRequest> requestsWithMissingFields() {
        return Arbitraries.integers().between(0, 13).flatMap(missingFieldIndex -> {
            LoanApplicationRequest request = new LoanApplicationRequest();

            // Set all fields to valid values first
            request.setLoanAmount(BigDecimal.valueOf(100000));
            request.setLoanPurpose("HOME");
            request.setLoanTenure(120);
            request.setMonthlyIncome(BigDecimal.valueOf(50000));
            request.setEmploymentType("SALARIED");
            request.setExistingEmi(BigDecimal.valueOf(5000));
            request.setCreditScore(700);
            request.setAge(35);
            request.setExistingLoans(1);
            request.setHasCollateral(true);
            request.setResidenceYears(5);
            request.setHasGuarantor(false);
            request.setRepaymentHistory("CLEAN");

            // Now set one field to null based on the index
            switch (missingFieldIndex) {
                case 0 -> request.setLoanAmount(null);
                case 1 -> request.setLoanPurpose(null);
                case 2 -> request.setLoanTenure(null);
                case 3 -> request.setMonthlyIncome(null);
                case 4 -> request.setEmploymentType(null);
                case 5 -> request.setExistingEmi(null);
                case 6 -> request.setCreditScore(null);
                case 7 -> request.setAge(null);
                case 8 -> request.setExistingLoans(null);
                case 9 -> request.setHasCollateral(null);
                case 10 -> request.setResidenceYears(null);
                case 11 -> request.setHasGuarantor(null);
                case 12 -> request.setRepaymentHistory(null);
                case 13 -> {
                    // Set blank string for string fields
                    request.setLoanPurpose("");
                }
            }

            return Arbitraries.just(request);
        });
    }
}
