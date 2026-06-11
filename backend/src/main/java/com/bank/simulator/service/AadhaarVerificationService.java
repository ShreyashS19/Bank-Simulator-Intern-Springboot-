package com.bank.simulator.service;

import com.bank.simulator.dto.AadhaarVerificationResponse;

public interface AadhaarVerificationService {

    /**
     * Generate OTP for Aadhaar verification via KYC provider.
     * OTP is sent to the mobile number linked to the Aadhaar.
     *
     * @param aadhaarNumber 12-digit Aadhaar number
     * @return response with clientId (reference ID) and status message
     */
    AadhaarVerificationResponse generateOtp(String aadhaarNumber);

    /**
     * Verify OTP for Aadhaar verification via KYC provider.
     *
     * @param aadhaarNumber 12-digit Aadhaar number
     * @param otp           6-digit OTP entered by the user
     * @param clientId      reference/session ID returned from generateOtp
     * @return response with verified=true and maskedAadhaar on success
     */
    AadhaarVerificationResponse verifyOtp(String aadhaarNumber, String otp, String clientId);
}
