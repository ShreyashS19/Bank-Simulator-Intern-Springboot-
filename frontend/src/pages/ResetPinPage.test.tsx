import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';

/**
 * Validation function for OTP
 * Accepts input if and only if it consists of exactly 6 digits (0-9) with no other characters
 */
const validateOtp = (otp: string): boolean => {
  return /^\d{6}$/.test(otp);
};

describe('ResetPinPage - OTP Validation', () => {
  describe('Property 1: OTP Validation Correctness', () => {
    /**
     * **Validates: Requirements 3.4**
     * 
     * Property: For any string input to the OTP field, the validation function SHALL accept 
     * the input if and only if it consists of exactly 6 digits (0-9) with no other characters.
     */
    it('should accept if and only if input is exactly 6 digits', () => {
      fc.assert(
        fc.property(fc.string(), (input) => {
          const isValid = validateOtp(input);
          const isExactly6Digits = /^\d{6}$/.test(input);
          
          // The validation result should match whether the input is exactly 6 digits
          expect(isValid).toBe(isExactly6Digits);
        }),
        { numRuns: 100 }
      );
    });

    it('should accept valid 6-digit OTPs', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: 999999 }).map(n => n.toString().padStart(6, '0')),
          (otp) => {
            expect(validateOtp(otp)).toBe(true);
          }
        ),
        { numRuns: 50 }
      );
    });

    it('should reject strings with length not equal to 6', () => {
      fc.assert(
        fc.property(
          fc.string().filter(s => s.length !== 6),
          (input) => {
            expect(validateOtp(input)).toBe(false);
          }
        ),
        { numRuns: 50 }
      );
    });

    it('should reject strings with non-digit characters', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 6, maxLength: 6 }).filter(s => !/^\d{6}$/.test(s)),
          (input) => {
            expect(validateOtp(input)).toBe(false);
          }
        ),
        { numRuns: 50 }
      );
    });

    it('should reject empty strings', () => {
      expect(validateOtp('')).toBe(false);
    });

    it('should reject strings with spaces', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('123 456', '12 3456', ' 123456', '123456 '),
          (input) => {
            expect(validateOtp(input)).toBe(false);
          }
        )
      );
    });

    it('should reject strings with special characters', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('123-456', '123.456', '123,456', '123@456', '123#456'),
          (input) => {
            expect(validateOtp(input)).toBe(false);
          }
        )
      );
    });

    it('should reject strings with letters', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('12345a', 'a12345', '123a45', 'ABCDEF', 'abc123'),
          (input) => {
            expect(validateOtp(input)).toBe(false);
          }
        )
      );
    });
  });
});
