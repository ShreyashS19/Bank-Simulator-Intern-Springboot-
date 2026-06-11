package com.bank.simulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AadhaarOtpVerifyRequest {

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar number must be exactly 12 digits")
    private String aadhaarNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits")
    private String otp;

    @NotBlank(message = "Client ID (reference ID) is required")
    private String clientId;
}
