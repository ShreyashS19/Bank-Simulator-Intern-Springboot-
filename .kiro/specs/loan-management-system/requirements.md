# Requirements Document

## Introduction

This document specifies the requirements for a comprehensive Loan Management System to be integrated into an existing full-stack banking simulator. The system will enable customers to apply for loans, receive automated credit scoring and eligibility assessment, and allow administrators to manage loan applications. The feature must be built without modifying any existing functionality.

## Glossary

- **Loan_System**: The complete loan management feature including application, scoring, and administration
- **Credit_Scorer**: The algorithmic component that evaluates loan applications using 12 factors
- **Loan_Application**: A customer's request for a loan with associated financial and personal information
- **Eligibility_Score**: A numerical score (0-100) calculated from 12 weighted factors
- **Credit_Score**: A numerical score (300-900) representing creditworthiness
- **DTI_Ratio**: Debt-to-Income ratio, calculated as total monthly EMI divided by monthly income
- **EMI**: Equated Monthly Installment, the monthly payment amount for a loan
- **Loan_Status**: The current state of a loan application (PENDING, APPROVED, REJECTED, UNDER_REVIEW)
- **Interest_Rate**: The annual percentage rate charged on the approved loan amount
- **Notification_Service**: The existing email service used to send loan status updates
- **Admin_User**: A user with ADMIN role who can review and modify loan applications
- **Customer_User**: A regular user who can apply for loans and view their loan history
- **Account_Repository**: Existing repository for accessing account creation dates
- **Transaction_Repository**: Existing repository for accessing transaction history

## Requirements

### Requirement 1: Loan Application Submission

**User Story:** As a customer, I want to submit a loan application with my financial and personal details, so that I can request a loan from the bank.

#### Acceptance Criteria

1. WHEN a customer submits a loan application, THE Loan_System SHALL validate all required fields
2. THE Loan_System SHALL generate a unique loan identifier in format LOAN-XXXXXXXX
3. THE Loan_System SHALL accept loan amounts between 10,000 and 10,000,000 INR
4. THE Loan_System SHALL accept loan purposes from the set (EDUCATION, HOME, BUSINESS, PERSONAL, VEHICLE)
5. THE Loan_System SHALL accept employment types from the set (SALARIED, SELF_EMPLOYED, GOVERNMENT, UNEMPLOYED)
6. THE Loan_System SHALL accept loan tenure between 6 and 360 months
7. THE Loan_System SHALL accept age between 18 and 70 years
8. THE Loan_System SHALL accept credit scores between 300 and 900
9. THE Loan_System SHALL store the application date as the current timestamp
10. THE Loan_System SHALL link the loan application to the customer's account number

### Requirement 2: Credit Score Calculation

**User Story:** As the system, I want to calculate a comprehensive credit score using 12 factors, so that I can make informed lending decisions.

#### Acceptance Criteria

1. THE Credit_Scorer SHALL calculate a credit score based on monthly income with maximum weight 120 points
2. THE Credit_Scorer SHALL calculate a score based on employment type with maximum weight 80 points
3. THE Credit_Scorer SHALL calculate a DTI ratio score with maximum weight 100 points
4. THE Credit_Scorer SHALL calculate a repayment history score with maximum weight 100 points
5. THE Credit_Scorer SHALL calculate an age-based score with maximum weight 60 points
6. THE Credit_Scorer SHALL calculate an existing loans score with maximum weight 60 points
7. THE Credit_Scorer SHALL calculate a collateral score with maximum weight 70 points
8. THE Credit_Scorer SHALL calculate a banking relationship score with maximum weight 50 points
9. THE Credit_Scorer SHALL calculate a residence stability score with maximum weight 40 points
10. THE Credit_Scorer SHALL calculate a loan purpose score with maximum weight 40 points
11. THE Credit_Scorer SHALL calculate a guarantor score with maximum weight 30 points
12. THE Credit_Scorer SHALL sum all factor scores to produce a total eligibility score between 0 and 100
13. THE Credit_Scorer SHALL store individual factor scores for transparency

### Requirement 3: Income-Based Scoring

**User Story:** As the credit scoring system, I want to evaluate applicants based on their monthly income, so that I can assess their repayment capacity.

#### Acceptance Criteria

1. WHEN monthly income is greater than or equal to 100,000 INR, THE Credit_Scorer SHALL assign 120 points
2. WHEN monthly income is between 75,000 and 99,999 INR, THE Credit_Scorer SHALL assign 100 points
3. WHEN monthly income is between 50,000 and 74,999 INR, THE Credit_Scorer SHALL assign 80 points
4. WHEN monthly income is between 30,000 and 49,999 INR, THE Credit_Scorer SHALL assign 60 points
5. WHEN monthly income is between 20,000 and 29,999 INR, THE Credit_Scorer SHALL assign 40 points
6. WHEN monthly income is less than 20,000 INR, THE Credit_Scorer SHALL assign 20 points

### Requirement 4: Employment Type Scoring

**User Story:** As the credit scoring system, I want to evaluate employment stability, so that I can assess income reliability.

#### Acceptance Criteria

1. WHEN employment type is GOVERNMENT, THE Credit_Scorer SHALL assign 80 points
2. WHEN employment type is SALARIED, THE Credit_Scorer SHALL assign 70 points
3. WHEN employment type is SELF_EMPLOYED, THE Credit_Scorer SHALL assign 50 points
4. WHEN employment type is UNEMPLOYED, THE Credit_Scorer SHALL assign 0 points

### Requirement 5: DTI Ratio Scoring

**User Story:** As the credit scoring system, I want to calculate and score the debt-to-income ratio, so that I can assess existing debt burden.

#### Acceptance Criteria

1. THE Credit_Scorer SHALL calculate DTI ratio as total monthly EMI divided by monthly income
2. WHEN DTI ratio is less than 0.20, THE Credit_Scorer SHALL assign 100 points
3. WHEN DTI ratio is between 0.20 and 0.29, THE Credit_Scorer SHALL assign 80 points
4. WHEN DTI ratio is between 0.30 and 0.39, THE Credit_Scorer SHALL assign 60 points
5. WHEN DTI ratio is between 0.40 and 0.49, THE Credit_Scorer SHALL assign 40 points
6. WHEN DTI ratio is greater than or equal to 0.50, THE Credit_Scorer SHALL assign 0 points

### Requirement 6: Repayment History Scoring

**User Story:** As the credit scoring system, I want to evaluate past repayment behavior, so that I can predict future payment reliability.

#### Acceptance Criteria

1. WHEN repayment history is clean, THE Credit_Scorer SHALL assign 100 points
2. WHEN repayment history is not clean, THE Credit_Scorer SHALL assign 0 points

### Requirement 7: Age-Based Scoring

**User Story:** As the credit scoring system, I want to evaluate applicant age, so that I can assess earning potential and risk.

#### Acceptance Criteria

1. WHEN age is between 30 and 50 years, THE Credit_Scorer SHALL assign 60 points
2. WHEN age is between 25 and 29 years OR between 51 and 60 years, THE Credit_Scorer SHALL assign 50 points
3. WHEN age is between 18 and 24 years OR between 61 and 70 years, THE Credit_Scorer SHALL assign 30 points

### Requirement 8: Existing Loans Scoring

**User Story:** As the credit scoring system, I want to evaluate existing loan burden, so that I can assess additional debt capacity.

#### Acceptance Criteria

1. WHEN existing loans count is 0, THE Credit_Scorer SHALL assign 60 points
2. WHEN existing loans count is 1, THE Credit_Scorer SHALL assign 50 points
3. WHEN existing loans count is 2, THE Credit_Scorer SHALL assign 30 points
4. WHEN existing loans count is greater than or equal to 3, THE Credit_Scorer SHALL assign 0 points

### Requirement 9: Collateral Scoring

**User Story:** As the credit scoring system, I want to evaluate collateral availability, so that I can assess loan security.

#### Acceptance Criteria

1. WHEN collateral is provided, THE Credit_Scorer SHALL assign 70 points
2. WHEN collateral is not provided, THE Credit_Scorer SHALL assign 0 points

### Requirement 10: Banking Relationship Scoring

**User Story:** As the credit scoring system, I want to evaluate the duration of banking relationship, so that I can assess customer loyalty and history.

#### Acceptance Criteria

1. THE Credit_Scorer SHALL retrieve account creation date from Account_Repository
2. THE Credit_Scorer SHALL calculate banking relationship duration in months
3. WHEN banking relationship is greater than or equal to 24 months, THE Credit_Scorer SHALL assign 50 points
4. WHEN banking relationship is between 12 and 23 months, THE Credit_Scorer SHALL assign 40 points
5. WHEN banking relationship is between 6 and 11 months, THE Credit_Scorer SHALL assign 25 points
6. WHEN banking relationship is less than 6 months, THE Credit_Scorer SHALL assign 10 points

### Requirement 11: Residence Stability Scoring

**User Story:** As the credit scoring system, I want to evaluate residence stability, so that I can assess applicant stability.

#### Acceptance Criteria

1. WHEN residence years is greater than or equal to 5, THE Credit_Scorer SHALL assign 40 points
2. WHEN residence years is between 3 and 4, THE Credit_Scorer SHALL assign 30 points
3. WHEN residence years is between 1 and 2, THE Credit_Scorer SHALL assign 20 points
4. WHEN residence years is less than 1, THE Credit_Scorer SHALL assign 10 points

### Requirement 12: Loan Purpose Scoring

**User Story:** As the credit scoring system, I want to evaluate loan purpose, so that I can assess loan risk profile.

#### Acceptance Criteria

1. WHEN loan purpose is EDUCATION OR HOME, THE Credit_Scorer SHALL assign 40 points
2. WHEN loan purpose is BUSINESS, THE Credit_Scorer SHALL assign 30 points
3. WHEN loan purpose is VEHICLE, THE Credit_Scorer SHALL assign 25 points
4. WHEN loan purpose is PERSONAL, THE Credit_Scorer SHALL assign 15 points

### Requirement 13: Guarantor Scoring

**User Story:** As the credit scoring system, I want to evaluate guarantor availability, so that I can assess additional repayment security.

#### Acceptance Criteria

1. WHEN a guarantor is provided, THE Credit_Scorer SHALL assign 30 points
2. WHEN a guarantor is not provided, THE Credit_Scorer SHALL assign 0 points

### Requirement 14: Loan Decision Logic

**User Story:** As the system, I want to automatically approve, reject, or flag applications for review, so that I can process loans efficiently.

#### Acceptance Criteria

1. WHEN eligibility score is greater than or equal to 750 AND DTI ratio is less than 0.40, THE Loan_System SHALL set status to APPROVED
2. WHEN eligibility score is between 650 and 749 OR DTI ratio is between 0.40 and 0.50, THE Loan_System SHALL set status to UNDER_REVIEW
3. WHEN eligibility score is less than 650 OR DTI ratio is greater than 0.50, THE Loan_System SHALL set status to REJECTED
4. WHEN status is REJECTED, THE Loan_System SHALL generate a rejection reason based on weak factors

### Requirement 15: Interest Rate Assignment

**User Story:** As the system, I want to assign interest rates based on creditworthiness, so that I can price loans appropriately.

#### Acceptance Criteria

1. WHEN eligibility score is greater than or equal to 800, THE Loan_System SHALL assign interest rate 7.5 percent
2. WHEN eligibility score is between 750 and 799, THE Loan_System SHALL assign interest rate 8.5 percent
3. WHEN eligibility score is between 700 and 749, THE Loan_System SHALL assign interest rate 10.0 percent
4. WHEN eligibility score is between 650 and 699, THE Loan_System SHALL assign interest rate 12.0 percent
5. WHEN status is REJECTED, THE Loan_System SHALL set interest rate to 0.0 percent

### Requirement 16: EMI Calculation

**User Story:** As the system, I want to calculate monthly EMI for approved loans, so that customers know their repayment obligation.

#### Acceptance Criteria

1. THE Loan_System SHALL calculate EMI using the formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
2. THE Loan_System SHALL use P as the approved loan amount
3. THE Loan_System SHALL use r as the monthly interest rate (annual rate divided by 12 and 100)
4. THE Loan_System SHALL use n as the loan tenure in months
5. THE Loan_System SHALL return EMI rounded to 2 decimal places

### Requirement 17: Improvement Tips Generation

**User Story:** As a customer, I want to receive personalized improvement tips when my loan is rejected or under review, so that I can improve my eligibility.

#### Acceptance Criteria

1. WHEN a factor score is below 50 percent of its maximum, THE Loan_System SHALL generate an improvement tip for that factor
2. THE Loan_System SHALL provide specific actionable advice for each weak factor
3. THE Loan_System SHALL return improvement tips as a list
4. WHEN status is APPROVED, THE Loan_System SHALL return an empty improvement tips list

### Requirement 18: Email Notification

**User Story:** As a customer, I want to receive email notifications about my loan application status, so that I stay informed.

#### Acceptance Criteria

1. WHEN a loan application is submitted, THE Loan_System SHALL send an email notification using Notification_Service
2. THE Loan_System SHALL include loan ID, status, and eligibility score in the email
3. WHEN status is APPROVED, THE Loan_System SHALL include approved amount, interest rate, and EMI in the email
4. WHEN status is REJECTED, THE Loan_System SHALL include rejection reason in the email
5. IF email sending fails, THE Loan_System SHALL log the error but not fail the loan application

### Requirement 19: Loan Retrieval by Account

**User Story:** As a customer, I want to view all my loan applications, so that I can track my loan history.

#### Acceptance Criteria

1. WHEN a customer requests their loans, THE Loan_System SHALL retrieve all loan applications for their account number
2. THE Loan_System SHALL order loans by application date in descending order
3. THE Loan_System SHALL return all loan fields including factor scores and improvement tips

### Requirement 20: Loan Retrieval by ID

**User Story:** As a customer or admin, I want to view detailed information about a specific loan, so that I can review its status and scoring.

#### Acceptance Criteria

1. WHEN a loan ID is provided, THE Loan_System SHALL retrieve the loan application
2. IF the loan ID does not exist, THE Loan_System SHALL return an error
3. THE Loan_System SHALL return all loan fields including individual factor scores and improvement tips

### Requirement 21: Admin Loan Listing

**User Story:** As an admin, I want to view all loan applications in the system, so that I can manage and review them.

#### Acceptance Criteria

1. WHEN an admin requests all loans, THE Loan_System SHALL retrieve all loan applications
2. THE Loan_System SHALL order loans by application date in descending order
3. THE Loan_System SHALL return all loan fields for each application

### Requirement 22: Admin Status Update

**User Story:** As an admin, I want to manually update loan application status, so that I can override automated decisions.

#### Acceptance Criteria

1. WHEN an admin updates a loan status, THE Loan_System SHALL validate the new status is in the set (PENDING, APPROVED, REJECTED, UNDER_REVIEW)
2. THE Loan_System SHALL update the loan status in the database
3. THE Loan_System SHALL update the lastUpdated timestamp
4. THE Loan_System SHALL send an email notification to the customer using Notification_Service
5. IF the loan ID does not exist, THE Loan_System SHALL return an error

### Requirement 23: Loan Statistics

**User Story:** As an admin, I want to view loan statistics, so that I can monitor the loan portfolio.

#### Acceptance Criteria

1. THE Loan_System SHALL calculate total number of loan applications
2. THE Loan_System SHALL calculate count of loans by status (PENDING, APPROVED, REJECTED, UNDER_REVIEW)
3. THE Loan_System SHALL calculate total approved loan amount
4. THE Loan_System SHALL calculate average eligibility score across all applications
5. THE Loan_System SHALL return all statistics in a single response

### Requirement 24: Frontend Loan Dashboard

**User Story:** As a customer, I want to view my loan eligibility and history in a visual dashboard, so that I can understand my loan status.

#### Acceptance Criteria

1. THE Loan_System SHALL display credit score in a gauge chart ranging from 300 to 900
2. THE Loan_System SHALL display factor scores in a horizontal bar chart
3. THE Loan_System SHALL display 12 factor information cards in a 3x4 grid
4. THE Loan_System SHALL display factor scores as progress bars
5. THE Loan_System SHALL display improvement tips when status is REJECTED or UNDER_REVIEW
6. THE Loan_System SHALL display loan history in a table format
7. THE Loan_System SHALL display top metrics (Credit Score, Max Loan Eligibility, DTI Ratio, Active Loans) in cards

### Requirement 25: Frontend Loan Application Form

**User Story:** As a customer, I want to apply for a loan through a multi-step form, so that I can provide all required information easily.

#### Acceptance Criteria

1. THE Loan_System SHALL provide a 3-step application form
2. THE Loan_System SHALL validate all required fields before submission
3. THE Loan_System SHALL display validation errors inline
4. WHEN the form is submitted, THE Loan_System SHALL send the application to the backend API
5. WHEN the submission is successful, THE Loan_System SHALL display the results including eligibility score and status
6. WHEN the submission fails, THE Loan_System SHALL display an error message

### Requirement 26: Frontend Admin Loan Management

**User Story:** As an admin, I want to manage loan applications from the admin dashboard, so that I can review and update loan statuses.

#### Acceptance Criteria

1. THE Loan_System SHALL display loan statistics in the admin dashboard
2. THE Loan_System SHALL display a loan management table with all applications
3. THE Loan_System SHALL allow admins to change loan status from the table
4. THE Loan_System SHALL display loan analytics charts (purpose distribution, status by month)
5. WHEN a status is changed, THE Loan_System SHALL update the backend and refresh the display

### Requirement 27: Data Persistence

**User Story:** As the system, I want to persist all loan applications in the database, so that data is not lost.

#### Acceptance Criteria

1. THE Loan_System SHALL create a database table named loan_applications
2. THE Loan_System SHALL store all loan application fields in the database
3. THE Loan_System SHALL use JPA/Hibernate for automatic table creation
4. THE Loan_System SHALL ensure loan ID is unique
5. THE Loan_System SHALL use appropriate data types for all fields

### Requirement 28: API Security

**User Story:** As the system, I want to secure loan API endpoints, so that only authenticated users can access them.

#### Acceptance Criteria

1. THE Loan_System SHALL require JWT authentication for all loan endpoints
2. THE Loan_System SHALL allow only ADMIN role users to access admin endpoints
3. THE Loan_System SHALL allow authenticated users to access their own loan applications
4. THE Loan_System SHALL return 401 Unauthorized for unauthenticated requests
5. THE Loan_System SHALL return 403 Forbidden for unauthorized role access

### Requirement 29: Non-Breaking Integration

**User Story:** As a developer, I want to add the loan feature without modifying existing code, so that existing functionality remains intact.

#### Acceptance Criteria

1. THE Loan_System SHALL create new entity, repository, service, and controller files
2. THE Loan_System SHALL only add routes to SecurityConfig without modifying existing routes
3. THE Loan_System SHALL only add navigation items to DashboardLayout without modifying existing items
4. THE Loan_System SHALL only add sections to AdminDashboard without modifying existing sections
5. THE Loan_System SHALL only add routes to App.tsx without modifying existing routes
6. THE Loan_System SHALL use existing ApiResponse wrapper for all responses
7. THE Loan_System SHALL use existing Notification_Service for emails
8. THE Loan_System SHALL use existing Account_Repository and Transaction_Repository for data retrieval

### Requirement 30: Transaction Pattern Analysis

**User Story:** As the credit scoring system, I want to analyze transaction patterns, so that I can assess financial behavior.

#### Acceptance Criteria

1. THE Credit_Scorer SHALL retrieve transaction history from Transaction_Repository
2. THE Credit_Scorer SHALL calculate transaction volume for the last 6 months
3. THE Credit_Scorer SHALL display transaction pattern in a chart on the frontend
4. THE Credit_Scorer SHALL use transaction data to inform repayment history scoring
