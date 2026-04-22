package com.bank.simulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateLoanStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "PENDING|PENDING_BANK_REVIEW|APPROVED|REJECTED|UNDER_REVIEW|ACTIVE|CLOSED",
            message = "Status must be one of: PENDING, PENDING_BANK_REVIEW, APPROVED, REJECTED, UNDER_REVIEW, ACTIVE, CLOSED"
    )
    private String status;
}
