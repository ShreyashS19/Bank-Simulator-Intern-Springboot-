package com.bank.simulator.service.impl;

import com.bank.simulator.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String springMailUsername;

    @Override
    @Async
    public void sendTransactionNotificationsAsync(
            String senderEmail,
            String senderName,
            String senderBankName,
            String receiverEmail,
            String receiverName,
            String receiverBankName,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String transactionId) {

        log.info("Processing transaction emails for TXN: {}", transactionId);

        try {
            sendTransactionNotificationToSender(
                    senderEmail,
                    senderName,
                    senderBankName,
                    senderAccountNumber,
                    receiverAccountNumber,
                    amount,
                    transactionId
            );
        } catch (Exception e) {
            log.error("Sender debit email failed for TXN {}: {}", transactionId, e.getMessage(), e);
        }

        try {
            sendTransactionNotificationToReceiver(
                    receiverEmail,
                    receiverName,
                    receiverBankName,
                    receiverAccountNumber,
                    senderAccountNumber,
                    amount,
                    transactionId
            );
        } catch (Exception e) {
            log.error("Receiver credit email failed for TXN {}: {}", transactionId, e.getMessage(), e);
        }
    }

    @Override
    public void sendTransactionNotificationToSender(
            String senderEmail,
            String senderName,
            String senderBankName,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String transactionId) {

        if (!emailEnabled) {
            log.info("Email disabled. Skipping sender notification.");
            return;
        }

        String subject = "Transaction Alert — Amount Debited";
        String body = buildSenderEmailTemplate(
                senderName, senderBankName, senderAccountNumber, receiverAccountNumber, amount, transactionId);

        sendNotification(senderEmail, subject, body);
    }

    @Override
    public void sendTransactionNotificationToReceiver(
            String receiverEmail,
            String receiverName,
            String receiverBankName,
            String receiverAccountNumber,
            String senderAccountNumber,
            BigDecimal amount,
            String transactionId) {

        if (!emailEnabled) {
            log.info("Email disabled. Skipping receiver notification.");
            return;
        }

        String subject = "Transaction Alert — Amount Credited";
        String body = buildReceiverEmailTemplate(
                receiverName, receiverBankName, receiverAccountNumber, senderAccountNumber, amount, transactionId);

        sendNotification(receiverEmail, subject, body);
    }

    @Override
    public boolean sendNotification(String toEmail, String subject, String body) {
        String recipient = toEmail != null ? toEmail.trim() : null;
        if (recipient == null || recipient.isBlank()) {
            log.warn("Skipping email — recipient address is null or blank");
            return false;
        }
        if (subject == null || subject.isBlank()) {
            log.warn("Skipping email to {} — subject is null or blank", recipient);
            return false;
        }
        if (body == null || body.isBlank()) {
            log.warn("Skipping email to {} — body is null or blank", recipient);
            return false;
        }

        String senderAddress = resolveFromAddress();
        if (senderAddress == null || senderAddress.isBlank()) {
            log.error("Skipping email to {} — no valid sender address configured", recipient);
            return false;
        }

        try {
            log.info("Sending email to: {} | subject: {}", recipient, subject);
            System.out.println("Sending email to: " + recipient + " | subject: " + subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom(senderAddress);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML=true

            mailSender.send(message);
            log.info("Email sent successfully to: {}", recipient);
            return true;
        } catch (Exception e) {
            log.error("Email failed for {} | Error: {} | Cause: {}",
                    recipient, e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "none",
                    e);
            return false;
        }
    }

    private String resolveFromAddress() {
        if (fromEmail != null && !fromEmail.isBlank()) {
            return fromEmail.trim();
        }
        if (springMailUsername != null && !springMailUsername.isBlank()) {
            return springMailUsername.trim();
        }
        return null;
    }

    // ======================== EMAIL TEMPLATES ========================

    private String buildSenderEmailTemplate(
            String senderName,
            String bankName,
            String senderAccount,
            String receiverAccount,
            BigDecimal amount,
            String transactionId) {

        String transactionDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
                    .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }
                    .email-header { background-color: #1976D2; color: white; padding: 20px; text-align: center; }
                    .email-header h1 { margin: 0; font-size: 24px; }
                    .email-body { padding: 30px; color: #333333; line-height: 1.8; }
                    .greeting { font-size: 16px; margin-bottom: 20px; }
                    .message { font-size: 15px; margin-bottom: 25px; }
                    .details-section { background-color: #f9f9f9; border-left: 4px solid #1976D2; padding: 20px; margin: 20px 0; }
                    .details-title { font-weight: bold; font-size: 16px; margin-bottom: 15px; color: #1976D2; }
                    .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e0e0e0; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { font-weight: bold; color: #666666; }
                    .detail-value { color: #333333; text-align: right; }
                    .amount-highlight { color: #d32f2f; font-weight: bold; font-size: 18px; }
                    .closing { margin-top: 30px; font-size: 15px; }
                    .signature { margin-top: 20px; font-weight: bold; }
                    .email-footer { background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999999; border-top: 1px solid #e0e0e0; }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-header"><h1>%s</h1></div>
                    <div class="email-body">
                        <div class="greeting">Hi %s,</div>
                        <div class="message">Your account has been debited with <span class="amount-highlight">₹%s</span> for a transaction to account %s.</div>
                        <div class="details-section">
                            <div class="details-title">Transaction Details:</div>
                            <div class="detail-row"><span class="detail-label">Transaction ID:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Bank:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Sender Account:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Receiver Account:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Amount:</span><span class="detail-value amount-highlight">₹%s</span></div>
                            <div class="detail-row"><span class="detail-label">Date:</span><span class="detail-value">%s</span></div>
                        </div>
                        <div class="closing">Thank you for banking with %s.</div>
                        <div class="signature">Best regards,<br>%s Team</div>
                    </div>
                    <div class="email-footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>&copy; 2025 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                bankName, senderName, amount.toString(), receiverAccount,
                transactionId, bankName, senderAccount, receiverAccount,
                amount.toString(), transactionDate, bankName, bankName, bankName
        );
    }

    private String buildReceiverEmailTemplate(
            String receiverName,
            String bankName,
            String receiverAccount,
            String senderAccount,
            BigDecimal amount,
            String transactionId) {

        String transactionDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
                    .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }
                    .email-header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .email-header h1 { margin: 0; font-size: 24px; }
                    .email-body { padding: 30px; color: #333333; line-height: 1.8; }
                    .greeting { font-size: 16px; margin-bottom: 20px; }
                    .message { font-size: 15px; margin-bottom: 25px; }
                    .details-section { background-color: #f9f9f9; border-left: 4px solid #4CAF50; padding: 20px; margin: 20px 0; }
                    .details-title { font-weight: bold; font-size: 16px; margin-bottom: 15px; color: #4CAF50; }
                    .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e0e0e0; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { font-weight: bold; color: #666666; }
                    .detail-value { color: #333333; text-align: right; }
                    .amount-highlight { color: #4CAF50; font-weight: bold; font-size: 18px; }
                    .closing { margin-top: 30px; font-size: 15px; }
                    .signature { margin-top: 20px; font-weight: bold; }
                    .email-footer { background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999999; border-top: 1px solid #e0e0e0; }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-header"><h1>%s</h1></div>
                    <div class="email-body">
                        <div class="greeting">Hi %s,</div>
                        <div class="message">Your account has been credited with <span class="amount-highlight">₹%s</span> from account %s.</div>
                        <div class="details-section">
                            <div class="details-title">Transaction Details:</div>
                            <div class="detail-row"><span class="detail-label">Transaction ID:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Bank:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Sender Account:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Receiver Account:</span><span class="detail-value">%s</span></div>
                            <div class="detail-row"><span class="detail-label">Amount:</span><span class="detail-value amount-highlight">₹%s</span></div>
                            <div class="detail-row"><span class="detail-label">Date:</span><span class="detail-value">%s</span></div>
                        </div>
                        <div class="closing">Thank you for banking with %s.</div>
                        <div class="signature">Best regards,<br>%s Team</div>
                    </div>
                    <div class="email-footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>&copy; 2025 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                bankName, receiverName, amount.toString(), senderAccount,
                transactionId, bankName, senderAccount, receiverAccount,
                amount.toString(), transactionDate, bankName, bankName, bankName
        );
    }
}
