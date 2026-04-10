# Testing Setup for Loan Dashboard

## Required Dependencies

To run the tests for the Loan Dashboard component, you need to install the following dependencies:

```bash
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom @vitest/ui
```

## Running Tests

Once the dependencies are installed, you can run the tests using:

```bash
# Run all tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run tests with UI
npm run test:ui
```

## Add Test Scripts to package.json

Add the following scripts to your `package.json`:

```json
{
  "scripts": {
    "test": "vitest run",
    "test:watch": "vitest",
    "test:ui": "vitest --ui"
  }
}
```

## Test Files

The following test files have been created:

- `frontend/src/pages/LoanDashboard.test.tsx` - Tests for the LoanDashboard component

## Test Coverage

The tests cover:

1. **Component Rendering**: Tests that the component renders with loan data
2. **Charts**: Tests that credit score gauge and factor scores bar chart render correctly
3. **Improvement Tips**: Tests that improvement tips only show for REJECTED or UNDER_REVIEW status
4. **Loan History Table**: Tests that all loans are displayed in the table
5. **Error Handling**: Tests error states
6. **Loading State**: Tests loading state
7. **Empty State**: Tests when no loans are found

## Manual Testing

If you prefer to test manually without installing test dependencies:

1. Start the backend server
2. Start the frontend development server: `npm run dev`
3. Navigate to `/loans` to view the loan dashboard
4. Test the following scenarios:
   - View loan dashboard with existing loans
   - Check that credit score gauge displays correctly
   - Verify factor scores bar chart shows all 11 factors
   - Confirm improvement tips only show for rejected/under review loans
   - Verify loan history table displays all loans with correct data
   - Test with no loans (should show "No loan applications found")
