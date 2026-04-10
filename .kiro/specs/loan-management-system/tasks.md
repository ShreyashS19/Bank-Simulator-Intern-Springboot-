# Implementation Plan: Loan Management System

## Overview

This implementation plan breaks down the Loan Management System into discrete, manageable coding tasks. The system will be built incrementally, starting with core backend entities and services, then adding API endpoints, frontend components, and comprehensive testing. Each task builds on previous work, with checkpoints to ensure quality and integration.

The implementation follows a non-breaking integration approach - all code is additive with no modifications to existing functionality.

## Tasks

- [x] 1. Set up backend foundation and core entities
  - [x] 1.1 Create LoanEntity with JPA annotations
    - Define all 40+ fields including loan details, factor scores, and timestamps
    - Add JPA annotations (@Entity, @Table, @Id, @GeneratedValue, @Column)
    - Configure @CreationTimestamp and @UpdateTimestamp for audit fields
    - Store improvementTips as JSON text field
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 27.1, 27.2, 27.3, 27.4, 27.5_
  
  - [x] 1.2 Create LoanRepository interface
    - Extend JpaRepository<LoanEntity, Long>
    - Add custom query methods: findByAccountNumberOrderByApplicationDateDesc, findByLoanId, findAllByOrderByApplicationDateDesc
    - Add statistics methods: countByStatus, custom queries for sumLoanAmountByStatus and avgEligibilityScore
    - _Requirements: 19.1, 19.2, 19.3, 20.1, 21.1, 21.2, 21.3, 23.1, 23.2, 23.3, 23.4, 23.5_
  
  - [x] 1.3 Create DTO classes
    - Create LoanApplicationRequest with validation annotations
    - Create LoanResponse with nested FactorScores object
    - Create LoanStatistics DTO
    - Create UpdateLoanStatusRequest DTO
    - Create CreditScoreResult internal DTO for service layer
    - _Requirements: 1.1, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8_

- [x] 2. Implement credit scoring service with 12-factor algorithm
  - [x] 2.1 Create CreditScoringService with factor calculation methods
    - Implement calculateIncomeScore with 6 income brackets (max 120 points)
    - Implement calculateEmploymentScore with 4 employment types (max 80 points)
    - Implement calculateDtiScore with 5 DTI brackets (max 100 points)
    - Implement calculateRepaymentHistoryScore (max 100 points)
    - Implement calculateAgeScore with 3 age brackets (max 60 points)
    - Implement calculateExistingLoansScore with 4 loan count brackets (max 60 points)
    - Implement calculateCollateralScore (max 70 points)
    - Implement calculateResidenceScore with 4 residence brackets (max 40 points)
    - Implement calculateLoanPurposeScore with 5 purpose types (max 40 points)
    - Implement calculateGuarantorScore (max 30 points)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 4.1, 4.2, 4.3, 4.4, 5.2, 5.3, 5.4, 5.5, 5.6, 6.1, 6.2, 7.1, 7.2, 7.3, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 11.1, 11.2, 11.3, 11.4, 12.1, 12.2, 12.3, 12.4, 13.1, 13.2_
  
  - [x] 2.2 Write property test for income score calculation
    - **Property 3: Income Score Calculation**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**
  
  - [x] 2.3 Write property test for employment score calculation
    - **Property 4: Employment Score Calculation**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4**
  
  - [x] 2.4 Write property test for DTI score calculation
    - **Property 6: DTI Score Calculation**
    - **Validates: Requirements 5.2, 5.3, 5.4, 5.5, 5.6**
  
  - [x] 2.5 Write property test for age score calculation
    - **Property 8: Age Score Calculation**
    - **Validates: Requirements 7.1, 7.2, 7.3**
  
  - [x] 2.6 Write property test for existing loans score calculation
    - **Property 9: Existing Loans Score Calculation**
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
  
  - [x] 2.7 Write property test for residence score calculation
    - **Property 13: Residence Score Calculation**
    - **Validates: Requirements 11.1, 11.2, 11.3, 11.4**
  
  - [x] 2.8 Write property test for loan purpose score calculation
    - **Property 14: Loan Purpose Score Calculation**
    - **Validates: Requirements 12.1, 12.2, 12.3, 12.4**

- [x] 3. Implement banking relationship and transaction pattern scoring
  - [x] 3.1 Implement calculateBankingRelationshipScore
    - Inject AccountRepository dependency
    - Retrieve account creation date using findByAccountNumber
    - Calculate months between creation date and current date using ChronoUnit.MONTHS
    - Apply 4 bracket rules (max 50 points)
    - Handle errors: default to 10 points if account not found or date is null
    - _Requirements: 2.8, 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_
  
  - [x] 3.2 Implement calculateTransactionPatternScore
    - Inject TransactionRepository dependency
    - Retrieve last 6 months of transactions using findByAccountNumberAndCreatedDateAfter
    - Calculate transaction volume and frequency
    - Return informational data (not included in eligibility score)
    - Handle errors: skip analysis if transaction retrieval fails
    - _Requirements: 30.1, 30.2, 30.3, 30.4_
  
  - [x] 3.3 Write property test for banking relationship score calculation
    - **Property 12: Banking Relationship Score Calculation**
    - **Validates: Requirements 10.3, 10.4, 10.5, 10.6**
  
  - [x] 3.4 Implement calculateDtiRatio method
    - Calculate DTI as existingEmi / monthlyIncome
    - Handle division by zero: return 1.0 if monthlyIncome is zero
    - Round to 4 decimal places
    - _Requirements: 5.1_
  
  - [x] 3.5 Write property test for DTI ratio calculation
    - **Property 5: DTI Ratio Calculation**
    - **Validates: Requirements 5.1**

- [x] 4. Implement main credit scoring orchestration
  - [x] 4.1 Implement calculateCreditScore main method
    - Call all 11 factor calculation methods
    - Calculate DTI ratio
    - Sum all factor scores (excluding transaction pattern)
    - Calculate eligibility score: (sum / 750) * 100
    - Round eligibility score to 2 decimal places
    - Return CreditScoreResult with all factor scores, eligibility score, and DTI ratio
    - _Requirements: 2.12, 2.13_
  
  - [x] 4.2 Write property test for eligibility score aggregation
    - **Property 16: Eligibility Score Aggregation**
    - **Validates: Requirements 2.12, 2.13**
  
  - [x] 4.3 Write unit tests for credit scoring edge cases
    - Test division by zero in DTI calculation
    - Test account not found scenario
    - Test transaction retrieval failure
    - Test boundary values for all factors

- [x] 5. Checkpoint - Ensure credit scoring tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement loan service with decision logic
  - [x] 6.1 Create LoanService with loan ID generation
    - Implement generateLoanId using UUID: "LOAN-" + 8 random digits
    - Ensure uniqueness by checking repository (retry up to 3 times if collision)
    - _Requirements: 1.2_
  
  - [x] 6.2 Write property test for loan ID format and uniqueness
    - **Property 1: Loan ID Format and Uniqueness**
    - **Validates: Requirements 1.2**
  
  - [x] 6.3 Implement determineStatus method
    - Apply decision logic based on eligibility score and DTI ratio
    - IF eligibility_score >= 750 AND dti_ratio < 0.40 THEN APPROVED
    - ELSE IF (650 <= eligibility_score < 750) OR (0.40 <= dti_ratio <= 0.50) THEN UNDER_REVIEW
    - ELSE IF eligibility_score < 650 OR dti_ratio > 0.50 THEN REJECTED
    - _Requirements: 14.1, 14.2, 14.3_
  
  - [x] 6.4 Write property test for loan decision logic
    - **Property 17: Loan Decision Logic**
    - **Validates: Requirements 14.1, 14.2, 14.3**
  
  - [x] 6.5 Implement assignInterestRate method
    - Apply interest rate rules based on eligibility score and status
    - REJECTED → 0.0%, score >= 800 → 7.5%, score >= 750 → 8.5%, score >= 700 → 10.0%, score >= 650 → 12.0%
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_
  
  - [x] 6.6 Write property test for interest rate assignment
    - **Property 19: Interest Rate Assignment**
    - **Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5**

- [x] 7. Implement EMI calculation and improvement tips
  - [x] 7.1 Implement calculateEmi method
    - Use formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
    - P = loan amount, r = monthly interest rate, n = tenure in months
    - Handle edge cases: return 0 if interest rate is 0 (rejected loans)
    - Round to 2 decimal places
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_
  
  - [x] 7.2 Write property test for EMI calculation formula
    - **Property 20: EMI Calculation Formula**
    - **Validates: Requirements 16.1, 16.2, 16.3, 16.4, 16.5**
  
  - [x] 7.3 Implement generateRejectionReason method
    - Identify factors scoring below 50% of maximum
    - Generate rejection reason mentioning weak factors
    - Return empty string if status is not REJECTED
    - _Requirements: 14.4_
  
  - [x] 7.4 Write property test for rejection reason generation
    - **Property 18: Rejection Reason Generation**
    - **Validates: Requirements 14.4**
  
  - [x] 7.5 Implement generateImprovementTips method
    - For each factor scoring below 50% of maximum, add corresponding tip
    - Use predefined tip messages from design document
    - Return empty list if status is APPROVED
    - _Requirements: 17.1, 17.2, 17.3, 17.4_
  
  - [x] 7.6 Write property test for improvement tips generation
    - **Property 21: Improvement Tips Generation for Weak Factors**
    - **Property 22: No Improvement Tips for Approved Loans**
    - **Validates: Requirements 17.1, 17.2, 17.3, 17.4**

- [x] 8. Implement loan application processing
  - [x] 8.1 Implement applyForLoan method in LoanService
    - Validate input using Bean Validation annotations
    - Generate unique loan ID
    - Call CreditScoringService.calculateCreditScore
    - Determine loan status using decision logic
    - Assign interest rate based on eligibility score
    - Calculate EMI for approved/under review loans
    - Generate rejection reason if rejected
    - Generate improvement tips
    - Create LoanEntity and populate all fields
    - Save to database using LoanRepository
    - Send email notification using NotificationService (handle failures gracefully)
    - Convert entity to LoanResponse DTO and return
    - _Requirements: 1.1, 1.2, 1.9, 1.10, 18.1, 18.2, 18.3, 18.4, 18.5, 27.2, 27.3_
  
  - [x] 8.2 Write property test for input validation completeness
    - **Property 2: Input Validation Completeness**
    - **Validates: Requirements 1.1**
  
  - [x] 8.3 Write property test for boundary value acceptance
    - **Property 23: Boundary Value Acceptance**
    - **Validates: Requirements 1.3, 1.6, 1.7, 1.8**
  
  - [x] 8.4 Write property test for boundary value rejection
    - **Property 24: Boundary Value Rejection**
    - **Validates: Requirements 1.3, 1.6, 1.7, 1.8**
  
  - [x] 8.5 Write property test for enum validation
    - **Property 25: Enum Validation**
    - **Validates: Requirements 1.4, 1.5**
  
  - [x] 8.6 Write integration test for end-to-end loan application
    - Test complete flow from request to database persistence
    - Verify email notification is sent
    - Test with real AccountRepository and TransactionRepository
    - Verify all factor scores are calculated correctly

- [x] 9. Checkpoint - Ensure loan application tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Implement loan retrieval and management methods
  - [x] 10.1 Implement getLoansByAccount method
    - Retrieve loans using findByAccountNumberOrderByApplicationDateDesc
    - Convert entities to LoanResponse DTOs
    - Return list ordered by application date descending
    - _Requirements: 19.1, 19.2, 19.3_
  
  - [x] 10.2 Implement getLoanById method
    - Retrieve loan using findByLoanId
    - Throw exception if not found
    - Convert entity to LoanResponse DTO
    - _Requirements: 20.1, 20.2, 20.3_
  
  - [x] 10.3 Implement getAllLoans method (admin)
    - Retrieve all loans using findAllByOrderByApplicationDateDesc
    - Convert entities to LoanResponse DTOs
    - Return list ordered by application date descending
    - _Requirements: 21.1, 21.2, 21.3_
  
  - [x] 10.4 Implement updateLoanStatus method (admin)
    - Validate new status is in allowed set
    - Retrieve loan by ID, throw exception if not found
    - Update status and lastUpdated timestamp
    - Save to database
    - Send email notification using NotificationService
    - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5_
  
  - [x] 10.5 Implement getLoanStatistics method (admin)
    - Calculate total applications count
    - Calculate counts by status using countByStatus
    - Calculate total approved amount using custom query
    - Calculate average eligibility score using custom query
    - Return LoanStatistics DTO
    - _Requirements: 23.1, 23.2, 23.3, 23.4, 23.5_
  
  - [x] 10.6 Write integration tests for loan retrieval and management
    - Test getLoansByAccount with multiple loans
    - Test getLoanById with valid and invalid IDs
    - Test getAllLoans returns all loans
    - Test updateLoanStatus updates database and sends notification
    - Test getLoanStatistics calculates correct values

- [x] 11. Implement REST API controller
  - [x] 11.1 Create LoanController with dependency injection
    - Inject LoanService
    - Add @RestController and @RequestMapping("/loan") annotations
    - Add @CrossOrigin for frontend integration
    - _Requirements: 28.1, 28.2, 28.3, 28.4, 28.5, 29.6_
  
  - [x] 11.2 Implement POST /loan/apply endpoint
    - Accept LoanApplicationRequest in request body
    - Extract account number from JWT authentication principal
    - Call LoanService.applyForLoan
    - Wrap response in ApiResponse with success message
    - Handle validation errors and return 400 Bad Request
    - Handle business errors and return appropriate status codes
    - _Requirements: 1.1, 1.2, 28.1_
  
  - [x] 11.3 Implement GET /loan/account/{accountNumber} endpoint
    - Extract account number from path variable
    - Verify authenticated user can only access their own account
    - Call LoanService.getLoansByAccount
    - Wrap response in ApiResponse
    - _Requirements: 19.1, 19.2, 19.3, 28.3_
  
  - [x] 11.4 Implement GET /loan/{loanId} endpoint
    - Extract loan ID from path variable
    - Call LoanService.getLoanById
    - Verify authenticated user can only access loans for their account (or is admin)
    - Wrap response in ApiResponse
    - Handle not found errors and return 404
    - _Requirements: 20.1, 20.2, 20.3_
  
  - [x] 11.5 Implement GET /loan/all endpoint (admin only)
    - Require ADMIN role
    - Call LoanService.getAllLoans
    - Wrap response in ApiResponse
    - _Requirements: 21.1, 21.2, 21.3, 28.2_
  
  - [x] 11.6 Implement PUT /loan/{loanId}/status endpoint (admin only)
    - Require ADMIN role
    - Extract loan ID from path variable
    - Accept UpdateLoanStatusRequest in request body
    - Call LoanService.updateLoanStatus
    - Wrap response in ApiResponse
    - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5, 28.2_
  
  - [x] 11.7 Implement GET /loan/statistics endpoint (admin only)
    - Require ADMIN role
    - Call LoanService.getLoanStatistics
    - Wrap response in ApiResponse
    - _Requirements: 23.1, 23.2, 23.3, 23.4, 23.5, 28.2_
  
  - [x] 11.8 Write integration tests for all API endpoints
    - Test POST /loan/apply with valid and invalid requests
    - Test authentication and authorization for all endpoints
    - Test admin endpoints return 403 for non-admin users
    - Test unauthenticated requests return 401
    - Verify ApiResponse wrapper format

- [x] 12. Update security configuration
  - [x] 12.1 Add loan endpoints to SecurityConfig
    - Open SecurityConfig.java
    - Add loan endpoint authorization rules to apiFilterChain method
    - POST /loan/apply requires authentication
    - GET /loan/account/**, /loan/{loanId} require authentication
    - GET /loan/all, /loan/statistics require ADMIN role
    - PUT /loan/{loanId}/status requires ADMIN role
    - Do not modify any existing security rules
    - _Requirements: 28.1, 28.2, 28.3, 28.4, 28.5, 29.1, 29.2_
  
  - [x] 12.2 Write integration test for security configuration
    - Test loan endpoints require JWT authentication
    - Test admin endpoints require ADMIN role
    - Test customers can only access their own loans

- [x] 13. Checkpoint - Ensure backend integration tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. Create frontend loan application form
  - [x] 14.1 Create LoanApplicationForm component with multi-step structure
    - Set up React component with TypeScript
    - Create state management for 3 steps: Personal Info, Financial Info, Loan Details
    - Implement step navigation (Next, Previous, Submit)
    - Add progress indicator showing current step
    - _Requirements: 25.1, 25.2_
  
  - [x] 14.2 Implement Step 1: Personal Information
    - Add form fields: age (number), employmentType (select), residenceYears (number), hasGuarantor (checkbox)
    - Add field validation with inline error messages
    - Employment type options: SALARIED, SELF_EMPLOYED, GOVERNMENT, UNEMPLOYED
    - _Requirements: 25.2, 25.3_
  
  - [x] 14.3 Implement Step 2: Financial Information
    - Add form fields: monthlyIncome (number), existingEmi (number), creditScore (number), existingLoans (number), hasCollateral (checkbox), repaymentHistory (select)
    - Add field validation with inline error messages
    - Repayment history options: CLEAN, NOT_CLEAN
    - _Requirements: 25.2, 25.3_
  
  - [x] 14.4 Implement Step 3: Loan Details
    - Add form fields: loanAmount (number), loanPurpose (select), loanTenure (number)
    - Add field validation with inline error messages
    - Loan purpose options: EDUCATION, HOME, BUSINESS, PERSONAL, VEHICLE
    - _Requirements: 25.2, 25.3_
  
  - [x] 14.5 Implement form submission and results display
    - Create API service method to call POST /loan/apply
    - Handle loading state during submission
    - Display results: eligibility score, status, interest rate, EMI, improvement tips
    - Handle errors and display error messages
    - _Requirements: 25.4, 25.5, 25.6_
  
  - [x] 14.6 Write frontend tests for loan application form
    - Test form validation
    - Test step navigation
    - Test form submission with valid data
    - Test error handling

- [x] 15. Create frontend loan dashboard
  - [x] 15.1 Create LoanDashboard component structure
    - Set up React component with TypeScript
    - Create API service method to call GET /loan/account/{accountNumber}
    - Fetch loan data on component mount
    - Handle loading and error states
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7_
  
  - [x] 15.2 Implement top metrics cards
    - Display Credit Score, Max Loan Eligibility, DTI Ratio, Active Loans
    - Use card layout with icons and values
    - _Requirements: 24.7_
  
  - [x] 15.3 Implement credit score gauge chart
    - Use Recharts RadialBarChart for gauge visualization
    - Range: 300-900
    - Color coding: red (300-550), yellow (550-700), green (700-900)
    - _Requirements: 24.1_
  
  - [x] 15.4 Implement factor scores bar chart
    - Use Recharts BarChart with horizontal orientation
    - Display all 11 factor scores
    - Show factor name and score value
    - _Requirements: 24.2_
  
  - [x] 15.5 Implement factor information grid
    - Create 3x4 grid of factor cards
    - Each card shows: factor name, score, max score, progress bar
    - Use color coding for progress bars
    - _Requirements: 24.3, 24.4_
  
  - [x] 15.6 Implement improvement tips section
    - Display improvement tips in alert box
    - Only show when status is REJECTED or UNDER_REVIEW
    - List all tips with bullet points
    - _Requirements: 24.5_
  
  - [x] 15.7 Implement loan history table
    - Display all loan applications in table format
    - Columns: Loan ID, Amount, Purpose, Status, Interest Rate, EMI, Application Date
    - Add status badges with color coding
    - _Requirements: 24.6_
  
  - [x] 15.8 Write frontend tests for loan dashboard
    - Test component renders with loan data
    - Test charts render correctly
    - Test improvement tips only show for rejected/under review
    - Test loan history table displays all loans

- [x] 16. Create frontend admin loan management
  - [x] 16.1 Create AdminLoanManagement component structure
    - Set up React component with TypeScript
    - Create API service methods for GET /loan/all and GET /loan/statistics
    - Fetch data on component mount
    - Handle loading and error states
    - _Requirements: 26.1, 26.2, 26.3, 26.4, 26.5_
  
  - [x] 16.2 Implement statistics cards
    - Display Total Applications, Approved Count, Rejected Count, Under Review Count, Total Approved Amount, Average Eligibility Score
    - Use card layout with icons and values
    - _Requirements: 26.1_
  
  - [x] 16.3 Implement loan management table
    - Display all loans in table format
    - Columns: Loan ID, Account Number, Amount, Status, Eligibility Score, Interest Rate, Application Date, Actions
    - Add status dropdown in Actions column
    - _Requirements: 26.2_
  
  - [x] 16.4 Implement status update functionality
    - Add dropdown with status options: PENDING, APPROVED, REJECTED, UNDER_REVIEW
    - Create API service method to call PUT /loan/{loanId}/status
    - Handle status change with immediate API call
    - Refresh table after successful update
    - Display success/error messages
    - _Requirements: 26.3, 26.5_
  
  - [x] 16.5 Implement analytics charts
    - Create loan purpose distribution pie chart using Recharts
    - Create loan status by month line chart using Recharts
    - _Requirements: 26.4_
  
  - [x] 16.6 Write frontend tests for admin loan management
    - Test component renders with loan data
    - Test statistics cards display correct values
    - Test status update calls API and refreshes table
    - Test charts render correctly

- [x] 17. Integrate frontend with existing application
  - [x] 17.1 Add loan routes to App.tsx
    - Add route for /loans → LoanDashboard
    - Add route for /loans/apply → LoanApplicationForm
    - Add route for /admin/loans → AdminLoanManagement (admin only)
    - Do not modify existing routes
    - _Requirements: 29.5_
  
  - [x] 17.2 Add loan navigation to DashboardLayout
    - Add "Loans" navigation item linking to /loans
    - Add "Apply for Loan" sub-item linking to /loans/apply
    - Do not modify existing navigation items
    - _Requirements: 29.3_
  
  - [x] 17.3 Add loan section to AdminDashboard
    - Add "Loan Management" section with link to /admin/loans
    - Do not modify existing admin sections
    - _Requirements: 29.4_
  
  - [x] 17.4 Create loanService.ts API client
    - Create API service with methods for all loan endpoints
    - Use existing axios instance with JWT interceptor
    - Export methods: applyForLoan, getLoansByAccount, getLoanById, getAllLoans, updateLoanStatus, getLoanStatistics
    - Do not modify existing API services
    - _Requirements: 29.7_

- [x] 18. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 19. Documentation and deployment preparation
  - [x] 19.1 Add jqwik dependency to pom.xml
    - Add jqwik dependency with version 1.8.2 in test scope
    - Do not modify existing dependencies
    - _Requirements: Testing framework_
  
  - [x] 19.2 Add recharts dependency to package.json
    - Add recharts dependency with version ^2.10.0
    - Run npm install to update package-lock.json
    - Do not modify existing dependencies
    - _Requirements: Frontend charting_
  
  - [x] 19.3 Verify database schema creation
    - Start application and verify loan_applications table is created automatically
    - Check all columns, indexes, and constraints are correct
    - Verify no modifications to existing tables
    - _Requirements: 27.1, 27.2, 27.3, 27.4, 27.5_
  
  - [x] 19.4 Test email notifications
    - Submit loan application and verify email is sent
    - Update loan status as admin and verify email is sent
    - Verify email content includes all required information
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5_
  
  - [x] 19.5 Perform end-to-end manual testing
    - Test complete loan application flow as customer
    - Test loan dashboard with multiple loans
    - Test admin loan management and status updates
    - Test all edge cases and error scenarios
    - Verify non-breaking integration (existing features still work)

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation and quality
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Integration tests validate component interactions and database operations
- All code is additive - no modifications to existing entities, services, or controllers
- The implementation uses Java for backend and TypeScript/React for frontend as specified in the design
