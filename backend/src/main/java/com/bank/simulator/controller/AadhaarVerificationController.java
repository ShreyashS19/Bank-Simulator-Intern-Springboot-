package com.bank.simulator.controller;

import com.bank.simulator.dto.AadhaarOtpRequest;
import com.bank.simulator.dto.AadhaarOtpVerifyRequest;
import com.bank.simulator.dto.AadhaarVerificationResponse;
import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.service.AadhaarVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aadhaar")
@RequiredArgsConstructor
@Slf4j
public class AadhaarVerificationController {

    private final AadhaarVerificationService aadhaarVerificationService;

    /**
     * POST /api/aadhaar/generate-otp
     * Generate OTP for Aadhaar verification via KYC provider.
     * OTP is sent to the user's Aadhaar-linked mobile number.
     */
    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponse<AadhaarVerificationResponse>> generateOtp(
            @Valid @RequestBody AadhaarOtpRequest request) {
        log.info("Aadhaar OTP generation requested");
        AadhaarVerificationResponse response = aadhaarVerificationService.generateOtp(request.getAadhaarNumber());
        return ResponseEntity.ok(ApiResponse.success("OTP sent to Aadhaar-linked mobile number", response));
    }

    /**
     * POST /api/aadhaar/verify-otp
     * Verify OTP for Aadhaar verification via KYC provider.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AadhaarVerificationResponse>> verifyOtp(
            @Valid @RequestBody AadhaarOtpVerifyRequest request) {
        log.info("Aadhaar OTP verification requested");
        AadhaarVerificationResponse response = aadhaarVerificationService.verifyOtp(
                request.getAadhaarNumber(),
                request.getOtp(),
                request.getClientId()
        );
        return ResponseEntity.ok(ApiResponse.success("Aadhaar verified successfully", response));
    }
}
