package com.bank.simulator.service;

import com.bank.simulator.dto.LoanEligibilityResultDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

        void sendEligibilityResultEmail(LoanEligibilityResultDto result);

    // ─── OTP & Reset Emails ───────────────────────────────────────────────────

    /**
     * Send the 6-digit OTP email for password reset.
     * Called asynchronously — does not block the API response.
     */
    void sendPasswordResetOtpEmail(String toEmail, String userName, String otp, LocalDateTime expiryTime);

    /**
     * Send the 6-digit OTP email for PIN reset.
     * Called asynchronously — does not block the API response.
     */
    void sendPinResetOtpEmail(String toEmail, String userName, String otp, LocalDateTime expiryTime);

    /**
     * Send a confirmation email after a successful password reset.
     * Called asynchronously — does not block the API response.
     */
    void sendPasswordResetSuccessEmail(String toEmail, String userName, LocalDateTime resetTime);

    /**
     * Send a confirmation email after a successful PIN reset.
     * Called asynchronously — does not block the API response.
     */
    void sendPinResetSuccessEmail(String toEmail, String userName, LocalDateTime resetTime);
}
