package com.bank.simulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateLoanStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|APPROVED|REJECTED|UNDER_REVIEW", message = "Status must be one of: PENDING, APPROVED, REJECTED, UNDER_REVIEW")
    private String status;
}
