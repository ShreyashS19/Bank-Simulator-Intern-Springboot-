package com.bank.simulator.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {

    private String accountId;

    private String customerId;

    private String accountNumber;

    private String aadharNumber;

    private String ifscCode;

    private String phoneNumberLinked;

    private BigDecimal amount;

    private String bankName;

    private String nameOnAccount;

    private String status;

    private LocalDateTime created;

    private LocalDateTime modified;
}
