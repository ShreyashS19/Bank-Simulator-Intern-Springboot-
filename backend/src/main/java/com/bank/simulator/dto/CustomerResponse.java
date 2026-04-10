package com.bank.simulator.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CustomerResponse {

    private String customerId;

    private String name;

    private String phoneNumber;

    private String email;

    private String address;

    private String aadharNumber;

    private LocalDate dob;

    private String status;
}
