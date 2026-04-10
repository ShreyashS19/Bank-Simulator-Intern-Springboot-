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
 * Property-based tests for loan application enum validation
 * 
 * **Validates: Requirements 1.4, 1.5**
 */
public class LoanApplicationEnumPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Property 25: Enum Validation
     * 
     * For any loan application with loan purpose or employment type not in the specified enum sets,
     * the validation SHALL reject the application.
     * 
     * **Validates: Requirements 1.4, 1.5**
     */
    @Property
    @Label("Property 25: Enum Validation - Invalid enum values are rejected")
    void invalidEnumValuesShouldBeRejected(@ForAll("requestsWithInvalidEnums") LoanApplicationRequest request) {
        // When: Validating a request with invalid enum values
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);
        
        // Then: Validation should fail
        assert !violations.isEmpty() : "Expected validation violations for invalid enum values";
    }

    @Property
    @Label("Property 25: Enum Validation - Valid enum values are accepted")
    void validEnumValuesShouldBeAccepted(@ForAll("requestsWithValidEnums") LoanApplicationRequest request) {
        // When: Validating a request with valid enum values
        Set<ConstraintViolation<LoanApplicationRequest>> violations = validator.validate(request);
        
        // Then: Validation should pass (no violations)
        assert violations.isEmpty() : "Expected no validation violations for valid enum values, but got: " + violations;
    }

    @Provide
    Arbitrary<LoanApplicationRequest> requestsWithInvalidEnums() {
        LoanApplicationRequest req1 = createValidRequest();
        req1.setLoanPurpose("INVALID_PURPOSE");
        
        LoanApplicationRequest req2 = createValidRequest();
        req2.setLoanPurpose("TRAVEL");
        
        LoanApplicationRequest req3 = createValidRequest();
        req3.setLoanPurpose("MEDICAL");
        
        LoanApplicationRequest req4 = createValidRequest();
        req4.setLoanPurpose("home"); // lowercase
        
        LoanApplicationRequest req5 = createValidRequest();
        req5.setLoanPurpose("");
        
        LoanApplicationRequest req6 = createValidRequest();
        req6.setEmploymentType("INVALID_TYPE");
        
        LoanApplicationRequest req7 = createValidRequest();
        req7.setEmploymentType("RETIRED");
        
        LoanApplicationRequest req8 = createValidRequest();
        req8.setEmploymentType("STUDENT");
        
        LoanApplicationRequest req9 = createValidRequest();
        req9.setEmploymentType("salaried"); // lowercase
        
        LoanApplicationRequest req10 = createValidRequest();
        req10.setEmploymentType("");
        
        return Arbitraries.of(req1, req2, req3, req4, req5, req6, req7, req8, req9, req10);
    }

    @Provide
    Arbitrary<LoanApplicationRequest> requestsWithValidEnums() {
        Arbitrary<String> validLoanPurposes = Arbitraries.of("EDUCATION", "HOME", "BUSINESS", "PERSONAL", "VEHICLE");
        Arbitrary<String> validEmploymentTypes = Arbitraries.of("SALARIED", "SELF_EMPLOYED", "GOVERNMENT", "UNEMPLOYED");
        
        return Combinators.combine(validLoanPurposes, validEmploymentTypes)
            .as((purpose, employment) -> {
                LoanApplicationRequest request = createValidRequest();
                request.setLoanPurpose(purpose);
                request.setEmploymentType(employment);
                return request;
            });
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
