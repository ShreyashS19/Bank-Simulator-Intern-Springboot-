package com.bank.simulator.service;

import java.math.BigDecimal;

public interface NotificationService {

    void sendTransactionNotificationsAsync(
            String senderEmail,
            String senderName,
            String senderBankName,
            String receiverEmail,
            String receiverName,
            String receiverBankName,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String transactionId);

    void sendTransactionNotificationToSender(
            String senderEmail,
            String senderName,
            String senderBankName,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String transactionId);

    void sendTransactionNotificationToReceiver(
            String receiverEmail,
            String receiverName,
            String receiverBankName,
            String receiverAccountNumber,
            String senderAccountNumber,
            BigDecimal amount,
            String transactionId);

    boolean sendNotification(String toEmail, String subject, String body);
}
