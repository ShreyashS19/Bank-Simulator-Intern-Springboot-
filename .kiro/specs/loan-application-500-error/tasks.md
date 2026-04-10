# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - LazyInitializationException when accessing customer.getAccounts()
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the LazyInitializationException exists
  - **Scoped PBT Approach**: Scope the property to concrete failing cases where the controller accesses lazy-loaded collections outside transaction boundaries
  - Test that POST /loan/apply with valid loan application data successfully processes without LazyInitializationException
  - The test assertions should verify: response status is 201 Created, response body contains loanId, status is one of [APPROVED, UNDER_REVIEW, REJECTED]
  - Run test on UNFIXED code WITHOUT @Transactional on the test class (to reproduce production behavior)
  - **EXPECTED OUTCOME**: Test FAILS with 500 Internal Server Error and LazyInitializationException (this is correct - it proves the bug exists)
  - Document counterexamples found: specific requests that trigger LazyInitializationException, stack traces showing the exception when accessing customer.getAccounts()
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Loan Processing Logic Preservation
  - **IMPORTANT**: Follow observation-first methodology
  - Since the unfixed code crashes on all requests, we cannot observe actual behavior
  - Instead, write property-based tests that verify the documented business logic is preserved:
    - Credit scoring calculation follows the 12-factor formula
    - Loan status determination follows documented decision rules (APPROVED/UNDER_REVIEW/REJECTED)
    - Interest rate assignment follows eligibility score brackets
    - EMI calculation uses the correct amortization formula
  - These tests will establish the baseline behavior that must be preserved after the fix
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests with @Transactional on test class to allow them to pass (simulating post-fix behavior)
  - **EXPECTED OUTCOME**: Tests pass when @Transactional is present, establishing the baseline
  - Mark task complete when tests are written and baseline behavior is validated
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 3. Fix for LazyInitializationException

  - [x] 3.1 Add @Transactional to applyForLoan method
    - Open file: backend/src/main/java/com/bank/simulator/controller/LoanController.java
    - Add import: import org.springframework.transaction.annotation.Transactional;
    - Add @Transactional(readOnly = true) annotation to applyForLoan() method (before @PostMapping("/apply"))
    - This keeps the Hibernate session open for the entire method execution, allowing access to lazy-loaded collections
    - _Bug_Condition: isBugCondition(request) where controller accesses customer.getAccounts() outside transaction_
    - _Expected_Behavior: Backend successfully accesses accounts collection and returns 201 Created with loan details_
    - _Preservation: All loan processing logic (credit scoring, status determination, interest rate, EMI calculation, notifications) remains unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 3.2 Add @Transactional to getLoansByAccount method
    - Open file: backend/src/main/java/com/bank/simulator/controller/LoanController.java
    - Add @Transactional(readOnly = true) annotation to getLoansByAccount() method (before @GetMapping("/account/{accountNumber}"))
    - This fixes the same LazyInitializationException at line 78 when accessing customer.getAccounts().stream()
    - _Bug_Condition: isBugCondition(request) where controller accesses customer.getAccounts() outside transaction_
    - _Expected_Behavior: Backend successfully accesses accounts collection for authorization check_
    - _Preservation: Authorization logic remains unchanged_
    - _Requirements: 1.2, 1.3, 2.2, 2.3_

  - [x] 3.3 Add @Transactional to getLoanById method
    - Open file: backend/src/main/java/com/bank/simulator/controller/LoanController.java
    - Add @Transactional(readOnly = true) annotation to getLoanById() method (before @GetMapping("/{loanId}"))
    - This fixes the same LazyInitializationException at line 115 when accessing customer.getAccounts().stream()
    - _Bug_Condition: isBugCondition(request) where controller accesses customer.getAccounts() outside transaction_
    - _Expected_Behavior: Backend successfully accesses accounts collection for authorization check_
    - _Preservation: Authorization logic remains unchanged_
    - _Requirements: 1.2, 1.3, 2.2, 2.3_

  - [x] 3.4 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Successful Loan Application Processing
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1 WITHOUT @Transactional on test class
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify response status is 201 Created
    - Verify response body contains valid loanId
    - Verify loan status is one of [APPROVED, UNDER_REVIEW, REJECTED]
    - Verify NO LazyInitializationException is thrown
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.5 Verify preservation tests still pass
    - **Property 2: Preservation** - Loan Processing Logic Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Verify credit scoring calculation produces correct results
    - Verify loan status determination follows documented rules
    - Verify interest rate assignment is correct
    - Verify EMI calculation is accurate
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 4. Checkpoint - Ensure all tests pass
  - Run all tests (bug condition exploration test + preservation tests)
  - Verify bug condition test passes WITHOUT @Transactional on test class (confirms fix works in production-like environment)
  - Verify preservation tests pass (confirms no regressions)
  - Ensure all tests pass, ask the user if questions arise
