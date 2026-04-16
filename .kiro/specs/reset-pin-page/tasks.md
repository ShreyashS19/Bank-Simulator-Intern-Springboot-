# Implementation Plan: Reset PIN Page Feature

## Overview

This implementation creates a dedicated Reset PIN page at `/reset-pin` route with navigation menu integration. The feature leverages existing backend PIN reset infrastructure while providing an improved user experience through a standalone page instead of the current modal-based approach.

## Tasks

- [-] 1. Create ResetPinPage component with basic structure
  - Create `frontend/src/pages/ResetPinPage.tsx` with TypeScript interfaces
  - Implement two-step form workflow (Request OTP → Enter OTP & New PIN)
  - Add proper TypeScript types for component state and props
  - Integrate with DashboardLayout for consistent navigation
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 1.1 Write property test for OTP validation
  - **Property 1: OTP Validation Correctness**
  - **Validates: Requirements 3.4**

- [ ] 1.2 Write property test for PIN validation  
  - **Property 2: PIN Validation Correctness**
  - **Validates: Requirements 3.5**

- [ ] 2. Implement form validation and user input handling
  - [~] 2.1 Create validation functions for OTP and PIN inputs
    - Implement `validateOtp()` function (exactly 6 digits)
    - Implement `validatePin()` function (exactly 6 digits)
    - Add input sanitization to prevent non-numeric characters
    - _Requirements: 3.4, 3.5_
  
  - [~] 2.2 Implement form state management
    - Add React state for step management (1 | 2)
    - Add state for OTP and PIN input values
    - Add loading and error state management
    - _Requirements: 3.1, 3.2, 4.5_
  
  - [~] 2.3 Write unit tests for validation functions
    - Test OTP validation edge cases (empty, too short, too long, non-numeric)
    - Test PIN validation edge cases (empty, too short, too long, non-numeric)
    - _Requirements: 3.4, 3.5_

- [ ] 3. Integrate with existing accountService API methods
  - [~] 3.1 Implement Step 1: Request OTP functionality
    - Call `accountService.forgotPin()` with user email from session
    - Handle success response and transition to Step 2
    - Display appropriate loading states during API call
    - _Requirements: 3.2, 5.1, 5.3_
  
  - [~] 3.2 Implement Step 2: Reset PIN functionality
    - Call `accountService.resetPin()` with email, OTP, and new PIN
    - Handle success response and redirect to accounts page
    - Display success toast notification
    - _Requirements: 3.6, 3.7_
  
  - [~] 3.3 Write integration tests for API calls
    - Mock accountService methods and test success scenarios
    - Test error handling for failed API calls
    - Test loading state management during API calls
    - _Requirements: 3.2, 3.6, 4.1, 4.2_

- [~] 4. Checkpoint - Ensure core functionality works
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement comprehensive error handling
  - [~] 5.1 Add session validation and error handling
    - Check for user email in localStorage on component mount
    - Display "User email not found in session" error when missing
    - Redirect to login page when user is not authenticated
    - _Requirements: 5.2, 5.4_
  
  - [~] 5.2 Add API error handling with user-friendly messages
    - Handle forgot-pin API errors with appropriate messages
    - Handle reset-pin API errors with appropriate messages
    - Display validation errors for invalid OTP/PIN formats
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [~] 5.3 Write unit tests for error handling
    - Test session validation error scenarios
    - Test API error handling and message display
    - Test validation error display
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 5.2_

- [ ] 6. Add routing integration and navigation menu
  - [~] 6.1 Add /reset-pin route to App.tsx
    - Import ResetPinPage component
    - Add protected route with ProtectedRoute guard
    - Ensure route is accessible to authenticated users
    - _Requirements: 1.1, 5.4_
  
  - [~] 6.2 Add Reset PIN menu item to DashboardLayout
    - Import KeyRound icon from lucide-react
    - Add menu item to allNavItems array with appropriate configuration
    - Ensure menu item is visible to all authenticated users
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [~] 6.3 Write integration tests for routing
    - Test navigation to /reset-pin route
    - Test menu item click navigation
    - Test authentication guard functionality
    - _Requirements: 1.1, 2.2, 5.4_

- [ ] 7. Implement responsive design and accessibility
  - [~] 7.1 Add responsive design for mobile, tablet, and desktop
    - Use existing design system components (Card, Button, Input)
    - Ensure proper spacing and layout on all screen sizes
    - Test touch interactions on mobile devices
    - _Requirements: 6.1, 6.2_
  
  - [~] 7.2 Implement accessibility features
    - Add proper ARIA labels and roles for form elements
    - Implement keyboard navigation support
    - Add focus management between form steps
    - Ensure proper color contrast and visual indicators
    - _Requirements: 6.3, 6.4, 6.5_
  
  - [~] 7.3 Write accessibility tests
    - Test keyboard navigation functionality
    - Test ARIA label presence and correctness
    - Test focus management between steps
    - _Requirements: 6.3, 6.4, 6.5_

- [ ] 8. Final integration and testing
  - [~] 8.1 Wire all components together
    - Ensure seamless flow between Step 1 and Step 2
    - Test complete PIN reset workflow end-to-end
    - Verify success redirect to accounts page
    - _Requirements: 3.7_
  
  - [~] 8.2 Write end-to-end integration tests
    - Test complete user workflow from navigation to success
    - Test error scenarios and recovery paths
    - Test session integration and authentication
    - _Requirements: All requirements_

- [~] 9. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design
- Unit tests validate specific examples and edge cases
- The implementation leverages existing `accountService.forgotPin()` and `accountService.resetPin()` methods
- All new code follows existing TypeScript and React patterns in the codebase