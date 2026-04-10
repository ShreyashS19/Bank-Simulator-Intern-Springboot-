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
 * Property-based tests for loan application boundary value validation
 * 
 * **Validates: Requirements 1.3, 1.6, 1.7, 1.8**
 */
public class LoanApplicationBoundaryPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Property 23: Boundary Value Acceptance
     * 
     * For any loan application with values at the exact boundaries of acceptable ranges,
     * the validation SHALL accept the application.
     * 
     * **Validates: Requirements 1.3, 1.6, 1.7, 1.8**
     */
    @Property
    @Label("Property 23: Boundary Value Acceptance - Exact boundary values are accepted")
    void exactBoundaryValuesShouldBeAccepted(@ForAll("validBoundaryRequests") LoanApplicationRequest request) {
        // When: Validating a request with exact boundary values
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);
        
        // Then: Validation should pass (no violations)
        assert violations.isEmpty() : "Expected no validation violations for boundary values, but got: " + violations;
    }

    /**
     * Property 24: Boundary Value Rejection
     * 
     * For any loan application with values just outside the acceptable ranges,
     * the validation SHALL reject the application.
     * 
     * **Validates: Requirements 1.3, 1.6, 1.7, 1.8**
     */
    @Property
    @Label("Property 24: Boundary Value Rejection - Values outside boundaries are rejected")
    void valuesOutsideBoundariesShouldBeRejected(@ForAll("invalidBoundaryRequests") LoanApplicationRequest request) {
        // When: Validating a request with values outside boundaries
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);
        
        // Then: Validation should fail
        assert !violations.isEmpty() : "Expected validation violations for out-of-bounds values";
    }

    @Provide
    Arbitrary<LoanApplicationRequest> validBoundaryRequests() {
        LoanApplicationRequest req1 = createValidRequest();
        req1.setLoanAmount(BigDecimal.valueOf(10000));
        
        LoanApplicationRequest req2 = createValidRequest();
        req2.setLoanAmount(BigDecimal.valueOf(10000000));
        
        LoanApplicationRequest req3 = createValidRequest();
        req3.setLoanTenure(6);
        
        LoanApplicationRequest req4 = createValidRequest();
        req4.setLoanTenure(360);
        
        LoanApplicationRequest req5 = createValidRequest();
        req5.setAge(18);
        
        LoanApplicationRequest req6 = createValidRequest();
        req6.setAge(70);
        
        LoanApplicationRequest req7 = createValidRequest();
        req7.setCreditScore(300);
        
        LoanApplicationRequest req8 = createValidRequest();
        req8.setCreditScore(900);
        
        return Arbitraries.of(req1, req2, req3, req4, req5, req6, req7, req8);
    }

    @Provide
    Arbitrary<LoanApplicationRequest> invalidBoundaryRequests() {
        LoanApplicationRequest req1 = createValidRequest();
        req1.setLoanAmount(BigDecimal.valueOf(9999));
        
        LoanApplicationRequest req2 = createValidRequest();
        req2.setLoanAmount(BigDecimal.valueOf(10000001));
        
        LoanApplicationRequest req3 = createValidRequest();
        req3.setLoanTenure(5);
        
        LoanApplicationRequest req4 = createValidRequest();
        req4.setLoanTenure(361);
        
        LoanApplicationRequest req5 = createValidRequest();
        req5.setAge(17);
        
        LoanApplicationRequest req6 = createValidRequest();
        req6.setAge(71);
        
        LoanApplicationRequest req7 = createValidRequest();
        req7.setCreditScore(299);
        
        LoanApplicationRequest req8 = createValidRequest();
        req8.setCreditScore(901);
        
        return Arbitraries.of(req1, req2, req3, req4, req5, req6, req7, req8);
    }

    private static LoanApplicationRequest createValidRequest() {
        LoanApplicationRequest request = new LoanApplicationRequest();
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
        return request;
    }
}
