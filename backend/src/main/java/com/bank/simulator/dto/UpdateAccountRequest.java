package com.bank.simulator.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateAccountRequest {

    private String bankName;

    private String nameOnAccount;

    private String status;

    private BigDecimal amount;

    private String accountNumber;

    private String aadharNumber;

    private String ifscCode;
}
