package com.bank.simulator.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCustomerRequest {

    private String name;

    private String phoneNumber;

    private String email;

    private String address;

    @Pattern(regexp = "\\d{6}", message = "Customer PIN must be exactly 6 digits")
    private String customerPin;

    private String aadharNumber;

    private LocalDate dob;

    private String status;
}
