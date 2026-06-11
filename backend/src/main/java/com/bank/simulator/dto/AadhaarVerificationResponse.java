package com.bank.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AadhaarVerificationResponse {

    private boolean verified;
    private String maskedAadhaar;
    private String message;
    private String clientId;
}
