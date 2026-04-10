package com.bank.simulator.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private String transactionId;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private String transactionType;

    private String description;

    private LocalDateTime createdDate;

    private LocalDateTime timestamp;

    private String pin;

    private String status;
}
