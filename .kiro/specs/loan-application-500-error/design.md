# Loan Application 500 Error Bugfix Design

## Overview

The bug occurs when users submit a loan application form, resulting in a 500 Internal Server Error. The root cause is a **LazyInitializationException** in the `LoanController.applyForLoan()` method. When the controller attempts to access `customer.getAccounts()` to retrieve the account number, Hibernate throws a LazyInitializationException because:

1. The `customerRepository.findByEmail(email)` method executes within a transaction and returns a `CustomerEntity`
2. The transaction completes and the Hibernate session closes
3. The controller then tries to access the lazy-loaded `accounts` collection outside the transaction boundary
4. Hibernate cannot fetch the collection because the session is closed, throwing LazyInitializationException
5. The `GlobalExceptionHandler` catches this exception and returns a 500 error with "Data access error. Please try again."

The fix involves ensuring the `accounts` collection is accessible when needed, either by:
1. Adding `@Transactional` to the controller method to keep the session open
2. Using `@EntityGraph` or `JOIN FETCH` in the repository to eagerly load the accounts
3. Moving the logic to a service layer method with proper transaction management

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when the `LoanController.applyForLoan()` method attempts to access `customer.getAccounts()` outside of a transaction context, causing a LazyInitializationException
- **Property (P)**: The desired behavior - the backend should successfully access the customer's accounts collection and process the loan application, returning a 201 Created response
- **Preservation**: All existing loan processing logic (credit scoring, status determination, interest rate assignment, EMI calculation, email notifications) must remain unchanged
- **LazyInitializationException**: A Hibernate exception thrown when attempting to access a lazy-loaded collection or proxy after the session has been closed
- **Transaction Boundary**: The scope within which database operations are executed and the Hibernate session remains open
- **Eager Loading**: Fetching associated entities immediately when the parent entity is loaded, avoiding lazy initialization issues
- **@Transactional**: Spring annotation that manages transaction boundaries, keeping the Hibernate session open for the duration of the method

## Bug Details

### Bug Condition

The bug manifests when the `LoanController.applyForLoan()` method attempts to access the lazy-loaded `accounts` collection on a `CustomerEntity` after the repository method has completed and the Hibernate session has closed. This occurs at line 52 of `LoanController.java`:

```java
String accountNumber = customer.getAccounts().get(0).getAccountNumber();
```

The `CustomerEntity` has a `@OneToMany` relationship with `AccountEntity`, and the `accounts` field is lazy-loaded by default. When `customerRepository.findByEmail(email)` executes, it runs within a transaction, fetches the customer, and then the transaction completes. The controller then tries to access the `accounts` collection outside the transaction boundary, triggering a LazyInitializationException.

**Formal Specification:**
```
FUNCTION isBugCondition(request)
  INPUT: request of type HTTP POST Request to /loan/apply
  OUTPUT: boolean
  
  customer := customerRepository.findByEmail(request.authenticatedEmail)
  // Transaction ends here, Hibernate session closes
  
  RETURN customer EXISTS
         AND customer.accounts IS LazyLoaded
         AND NOT withinTransactionBoundary()
         AND accessingLazyCollection(customer.accounts)
END FUNCTION
```

### Examples

- **Example 1**: User submits loan application with valid data → Controller calls `customerRepository.findByEmail()` → Transaction completes → Controller accesses `customer.getAccounts()` → LazyInitializationException → 500 error
- **Example 2**: The same issue occurs in `getLoansByAccount()` method at line 78 when accessing `customer.getAccounts().stream()`
- **Example 3**: The same issue occurs in `getLoanById()` method at line 115 when accessing `customer.getAccounts().stream()`
- **Edge Case**: If the controller method were annotated with `@Transactional`, the session would remain open and the bug would not occur

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Credit scoring calculation logic must continue to work exactly as before (all 12 factor scores)
- Loan status determination logic (APPROVED/UNDER_REVIEW/REJECTED) must remain unchanged
- Interest rate assignment based on eligibility score must remain unchanged
- EMI calculation using the amortization formula must remain unchanged
- Email notification sending to customers must continue to work
- Loan retrieval endpoints (/loan/account/{accountNumber}, /loan/{loanId}, /loan/all) must continue to work
- Loan statistics calculation must remain unchanged
- Authentication and authorization logic must remain unchanged

**Scope:**
All inputs that successfully deserialize (if any exist) should be completely unaffected by this fix. This includes:
- Any existing loan applications that were successfully processed
- All GET endpoints for retrieving loan data
- Admin endpoints for updating loan status and viewing statistics
- The entire credit scoring and decision logic pipeline

## Hypothesized Root Cause

Based on the bug exploration test and code analysis, the root cause is confirmed:

**LazyInitializationException in LoanController**: The `CustomerEntity.accounts` field is a lazy-loaded `@OneToMany` collection. When the controller accesses this collection outside of a transaction boundary, Hibernate cannot fetch the data because the session has been closed.

**Affected Methods**:
1. `applyForLoan()` - Line 52: `customer.getAccounts().get(0).getAccountNumber()`
2. `getLoansByAccount()` - Line 78: `customer.getAccounts().stream()`
3. `getLoanById()` - Line 115: `customer.getAccounts().stream()`

**Why This Happens**:
- The repository method `findByEmail()` executes within its own transaction
- When the repository method returns, the transaction commits and the Hibernate session closes
- The controller then tries to access the lazy-loaded `accounts` collection
- Hibernate needs an open session to fetch the collection, but the session is closed
- LazyInitializationException is thrown
- GlobalExceptionHandler catches it and returns "Data access error. Please try again."

## Correctness Properties

Property 1: Bug Condition - Successful Loan Application Processing

_For any_ HTTP POST request to `/loan/apply` where the authenticated user has a valid customer record with at least one account, the backend SHALL successfully access the customer's accounts collection and process the loan application, returning a 201 Created response with loan details, WITHOUT throwing a LazyInitializationException.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Existing Loan Processing Logic

_For any_ loan application request that is successfully processed, the backend SHALL execute the exact same credit scoring, status determination, interest rate assignment, EMI calculation, and notification logic as before the fix, producing identical results for identical inputs.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

The fix involves ensuring the `accounts` collection is accessible when the controller needs it. There are three viable approaches:

**File**: `backend/src/main/java/com/bank/simulator/controller/LoanController.java`

**Approach A - Add @Transactional to Controller Methods (Recommended)**:
Add `@Transactional(readOnly = true)` annotation to the three affected methods:
- `applyForLoan()` - Keep session open for the entire method execution
- `getLoansByAccount()` - Keep session open for account verification
- `getLoanById()` - Keep session open for account verification

This ensures the Hibernate session remains open while the controller accesses lazy-loaded collections.

**Approach B - Eager Fetch in Repository**:
Modify `CustomerRepository.findByEmail()` to use `@EntityGraph` or `JOIN FETCH`:
```java
@EntityGraph(attributePaths = {"accounts"})
Optional<CustomerEntity> findByEmail(String email);
```
This eagerly loads the accounts collection when fetching the customer.

**Approach C - Service Layer Refactoring**:
Move the customer lookup and account retrieval logic to a service layer method with `@Transactional`, then return only the account number to the controller. This separates concerns and keeps transaction management in the service layer.

**Recommended Approach**: Approach A (@Transactional on controller methods) is the simplest and most direct fix. It requires minimal code changes and clearly indicates that these methods need database access within a transaction context.

### Specific Changes for Approach A

**File**: `backend/src/main/java/com/bank/simulator/controller/LoanController.java`

1. Add import: `import org.springframework.transaction.annotation.Transactional;`
2. Add `@Transactional(readOnly = true)` annotation to `applyForLoan()` method (line 38)
3. Add `@Transactional(readOnly = true)` annotation to `getLoansByAccount()` method (line 68)
4. Add `@Transactional(readOnly = true)` annotation to `getLoanById()` method (line 97)

Note: We use `readOnly = true` because these methods only read data from the database (the actual loan creation happens in the service layer).

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm the root cause (LazyInitializationException when accessing customer.getAccounts() outside transaction).

**Test Plan**: Write integration tests that simulate HTTP POST requests to `/loan/apply` with valid loan application data. Run these tests on the UNFIXED code to observe LazyInitializationException and 500 errors.

**Test Cases**:
1. **Basic Loan Application Test**: POST request with valid loan data (will fail on unfixed code with LazyInitializationException)
2. **Multiple Loan Applications**: POST multiple requests to verify the bug is consistent (will fail on unfixed code)
3. **Get Loans by Account Test**: GET request to `/loan/account/{accountNumber}` (will fail on unfixed code at line 78)
4. **Get Loan by ID Test**: GET request to `/loan/{loanId}` (will fail on unfixed code at line 115)

**Expected Counterexamples**:
- HTTP 500 Internal Server Error responses
- Stack traces showing `LazyInitializationException` with message about accessing lazy-loaded collection
- Error message "Data access error. Please try again." from GlobalExceptionHandler
- Logs showing the exception occurs when accessing `customer.getAccounts()`

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (valid loan application requests from authenticated users with accounts), the fixed backend successfully processes the request without LazyInitializationException.

**Pseudocode:**
```
FOR ALL request WHERE isBugCondition(request) DO
  response := POST /loan/apply WITH request
  ASSERT response.status == 201
  ASSERT response.body.loanId IS NOT NULL
  ASSERT response.body.status IN ["APPROVED", "UNDER_REVIEW", "REJECTED"]
  ASSERT NO LazyInitializationException thrown
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs that are successfully processed, the fixed backend produces the same credit scoring, status determination, and loan processing results as the original code would have (if the LazyInitializationException had not occurred).

**Pseudocode:**
```
FOR ALL input WHERE validLoanApplicationRequest(input) DO
  ASSERT creditScoringLogic_fixed(input) == creditScoringLogic_original(input)
  ASSERT statusDetermination_fixed(input) == statusDetermination_original(input)
  ASSERT interestRateAssignment_fixed(input) == interestRateAssignment_original(input)
  ASSERT emiCalculation_fixed(input) == emiCalculation_original(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all valid inputs

**Test Plan**: Since the original code crashes on all requests, we cannot directly compare outputs. Instead, we will verify that the fixed code produces results consistent with the documented business logic (credit scoring formulas, status determination rules, interest rate tables, EMI formula).

**Test Cases**:
1. **Credit Scoring Preservation**: Verify that credit scores are calculated correctly according to the 12-factor formula
2. **Status Determination Preservation**: Verify that loan status (APPROVED/UNDER_REVIEW/REJECTED) follows the documented decision rules
3. **Interest Rate Preservation**: Verify that interest rates are assigned according to the eligibility score brackets
4. **EMI Calculation Preservation**: Verify that EMI is calculated using the correct amortization formula

### Unit Tests

- Test that `@Transactional` annotation keeps the session open for the duration of the method
- Test that accessing `customer.getAccounts()` within the transaction does not throw LazyInitializationException
- Test edge cases (customer with no accounts, customer with multiple accounts)
- Test that the fix works for all three affected methods (applyForLoan, getLoansByAccount, getLoanById)

### Property-Based Tests

- Generate random valid loan application requests and verify successful processing without LazyInitializationException
- Generate random loan applications and verify credit scoring produces consistent results
- Generate random eligibility scores and DTI ratios and verify status determination follows the documented rules
- Test that the fix works across a wide range of valid inputs

### Integration Tests

- Test full loan application flow from HTTP request to database persistence
- Test that email notifications are sent after successful loan processing
- Test that loan retrieval endpoints return the correctly processed loan data
- Test that the fix works with actual HTTP requests and transaction management
