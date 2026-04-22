package com.bank.simulator.service.impl;

import com.bank.simulator.dto.LoanEligibilityResultDto;
import com.bank.simulator.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

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

    @Override
    @Async
    public void sendEligibilityResultEmail(LoanEligibilityResultDto result) {
        if (result == null || result.getCustomerEmail() == null || result.getCustomerEmail().isBlank()) {
            log.warn("Skipping eligibility result email - invalid recipient payload");
            return;
        }
        if (!emailEnabled) {
            log.info("Email disabled. Skipping eligibility result email for ref {}", result.getReferenceNumber());
            return;
        }

        String subject = "Loan Eligibility Result - Ref #" + result.getReferenceNumber() + " | Bank Simulator";
        String body = buildEligibilityResultTemplate(result);
        sendNotification(result.getCustomerEmail(), subject, body);
    }

    private String buildEligibilityResultTemplate(LoanEligibilityResultDto result) {
        boolean eligible = "ELIGIBLE".equalsIgnoreCase(result.getEligibilityStatus());
        String badgeColor = eligible ? "#2e7d32" : "#c62828";
        String badgeText = eligible ? "ELIGIBLE" : "NOT ELIGIBLE";
        String logoDataUri = loadBankLogoDataUri();

        StringBuilder documentsHtml = new StringBuilder();
        List<String> documents = result.getRequiredDocuments() == null ? List.of() : result.getRequiredDocuments();
        for (int i = 0; i < documents.size(); i++) {
            documentsHtml.append("<li style=\"margin:6px 0;\">")
                    .append(escapeHtml(documents.get(i)))
                    .append("</li>");
        }

        StringBuilder notesHtml = new StringBuilder();
        List<String> notes = result.getSpecialNotes() == null ? List.of() : result.getSpecialNotes();
        for (String note : notes) {
            notesHtml.append("<li style=\"margin:6px 0;\">")
                    .append(escapeHtml(note))
                    .append("</li>");
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset=\"UTF-8\">
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
            </head>
            <body style=\"margin:0;padding:20px;background:#f4f6f9;font-family:Arial,sans-serif;color:#1f2937;\">
                <table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:700px;margin:0 auto;background:#ffffff;border-radius:10px;overflow:hidden;border:1px solid #e5e7eb;\">
                    <tr>
                        <td style=\"padding:20px;border-bottom:1px solid #e5e7eb;\">
                            <table width=\"100%%\"><tr>
                                <td style=\"width:72px;\"><img src=\"%s\" alt=\"Bank Simulator\" width=\"56\" height=\"56\" style=\"display:block;border-radius:8px;object-fit:contain;\"/></td>
                                <td style=\"vertical-align:middle;\">
                                    <h2 style=\"margin:0;font-size:22px;color:#0f172a;\">Bank Simulator</h2>
                                    <p style=\"margin:4px 0 0;color:#475569;font-size:14px;\">Loan Eligibility Advisory</p>
                                </td>
                            </tr></table>
                        </td>
                    </tr>
                    <tr>
                        <td style=\"padding:24px;\">
                            <div style=\"display:inline-block;background:%s;color:#ffffff;padding:10px 16px;border-radius:999px;font-weight:700;font-size:13px;letter-spacing:.5px;\">%s</div>
                            <p style=\"margin:18px 0 8px;font-size:15px;\"><strong>Reference Number:</strong> %s</p>
                            <p style=\"margin:0 0 8px;font-size:15px;\"><strong>Customer:</strong> %s</p>
                            <p style=\"margin:0 0 8px;font-size:15px;\"><strong>Eligibility Score:</strong> %s / 100</p>
                            <p style=\"margin:0 0 20px;font-size:15px;line-height:1.6;\">%s</p>

                            <h3 style=\"margin:18px 0 8px;font-size:16px;color:#0f172a;\">Documents Required</h3>
                            <ol style=\"margin:0;padding-left:20px;line-height:1.6;\">%s</ol>

                            <h3 style=\"margin:18px 0 8px;font-size:16px;color:#0f172a;\">Important Notes</h3>
                            <ul style=\"margin:0;padding-left:20px;line-height:1.6;\">%s</ul>

                            <p style=\"margin:22px 0 0;font-size:13px;color:#475569;\">This letter is valid for 30 days. Final approval is at bank discretion.</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                logoDataUri,
                badgeColor,
                badgeText,
                escapeHtml(result.getReferenceNumber()),
                escapeHtml(result.getCustomerName()),
                result.getEligibilityScore() != null ? result.getEligibilityScore().toPlainString() : "0",
                escapeHtml(result.getEligibilityMessage()),
                documentsHtml,
                notesHtml
        );
    }

    private String loadBankLogoDataUri() {
        try {
            ClassPathResource logo = new ClassPathResource("static/bank-logo.png");
            if (logo.exists()) {
                byte[] bytes = logo.getInputStream().readAllBytes();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException ex) {
            log.warn("Unable to load bank logo for eligibility email: {}", ex.getMessage());
        }
        return "https://dummyimage.com/56x56/f59e0b/ffffff.png&text=BS";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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

    // ======================== OTP EMAIL METHODS ========================

    @Override
    @Async
    public void sendPasswordResetOtpEmail(String toEmail, String userName, String otp, java.time.LocalDateTime expiryTime) {
        if (!emailEnabled) { log.info("Email disabled. Skipping password reset OTP email."); return; }
        String subject = "Your Password Reset OTP - Bank Simulator";
        String body = buildOtpEmailTemplate(userName, otp, expiryTime,
                "Password Reset Request",
                "We received a request to reset the password for your account associated with this email address.",
                "");
        sendNotification(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendPinResetOtpEmail(String toEmail, String userName, String otp, java.time.LocalDateTime expiryTime) {
        if (!emailEnabled) { log.info("Email disabled. Skipping PIN reset OTP email."); return; }
        String subject = "Your PIN Reset OTP - Bank Simulator";
        String body = buildOtpEmailTemplate(userName, otp, expiryTime,
                "Account PIN Reset Request",
                "We received a request to reset the PIN for your bank account.",
                "<p style=\"color:#555555;font-size:13px;margin-top:12px;\">Your PIN protects your transactions. Never share it with anyone, including bank employees.</p>");
        sendNotification(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendPasswordResetSuccessEmail(String toEmail, String userName, java.time.LocalDateTime resetTime) {
        if (!emailEnabled) { log.info("Email disabled. Skipping password reset success email."); return; }
        String subject = "Your Password Has Been Reset - Bank Simulator";
        String body = buildSuccessEmailTemplate(userName, resetTime,
                "Password Reset Successful",
                "Your password has been successfully reset",
                "If you did NOT make this change, your account may be compromised. Please contact our support team immediately.");
        sendNotification(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendPinResetSuccessEmail(String toEmail, String userName, java.time.LocalDateTime resetTime) {
        if (!emailEnabled) { log.info("Email disabled. Skipping PIN reset success email."); return; }
        String subject = "Your Account PIN Has Been Reset - Bank Simulator";
        String body = buildSuccessEmailTemplate(userName, resetTime,
                "PIN Reset Successful",
                "Your account PIN has been successfully reset",
                "If you did NOT request this PIN change, contact us immediately as your account security may be at risk.");
        sendNotification(toEmail, subject, body);
    }

    // ─── OTP HTML Template Builder ───────────────────────────────────────────

    private String buildOtpEmailTemplate(String userName, String otp, java.time.LocalDateTime expiryTime,
                                          String headerTitle, String bodyText, String extraWarning) {
        String expiryStr = formatDateTime(expiryTime);
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head>"
             + "<body style=\"margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;\">"
             + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f6f9;padding:40px 0;\"><tr><td align=\"center\">"
             + "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff;border-radius:8px;overflow:hidden;max-width:600px;width:100%;\">"
             + "<tr><td style=\"background-color:#1a73e8;padding:24px;text-align:center;\">"
             + "<p style=\"margin:0;font-size:24px;font-weight:bold;color:#ffffff;\">Bank Simulator</p>"
             + "<p style=\"margin:6px 0 0;font-size:14px;color:#d0e4ff;\">" + headerTitle + "</p>"
             + "</td></tr>"
             + "<tr><td style=\"padding:32px 40px;color:#333333;\">"
             + "<p style=\"font-size:16px;margin:0 0 16px;\">Dear " + userName + ",</p>"
             + "<p style=\"font-size:15px;margin:0 0 24px;line-height:1.6;\">" + bodyText + "</p>"
             + "<p style=\"font-size:15px;margin:0 0 12px;font-weight:bold;\">Your One-Time Password (OTP) is:</p>"
             + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\">"
             + "<div style=\"display:inline-block;background-color:#f0f4ff;border:2px solid #1a73e8;border-radius:8px;padding:16px 32px;margin:8px 0 24px;\">"
             + "<span style=\"font-size:36px;font-weight:700;letter-spacing:12px;color:#1a73e8;\">" + otp + "</span>"
             + "</div></td></tr></table>"
             + "<p style=\"color:#e53935;font-size:14px;margin:0 0 8px;\">This OTP is valid for <strong>10 minutes</strong> only.</p>"
             + "<p style=\"color:#e53935;font-size:13px;margin:0 0 8px;\">Expires at: <strong>" + expiryStr + "</strong></p>"
             + "<p style=\"color:#555555;font-size:13px;margin:0 0 8px;\">Do not share this OTP with anyone.</p>"
             + extraWarning
             + "<p style=\"font-size:14px;margin:24px 0 0;color:#555555;\">If you did not request this, please ignore this email or contact support@banksimulator.com immediately.</p>"
             + "</td></tr>"
             + "<tr><td style=\"background-color:#f4f6f9;padding:16px;text-align:center;border-top:1px solid #eeeeee;\">"
             + "<p style=\"margin:0;font-size:12px;color:#999999;\">&copy; 2025 Bank Simulator. All rights reserved.</p>"
             + "<p style=\"margin:4px 0 0;font-size:12px;color:#999999;\">This is an automated message. Please do not reply. | Support: support@banksimulator.com</p>"
             + "</td></tr></table></td></tr></table></body></html>";
    }

    // ─── Success Email Template Builder ─────────────────────────────────────

    private String buildSuccessEmailTemplate(String userName, java.time.LocalDateTime resetTime,
                                              String headerTitle, String actionText, String warningText) {
        String resetStr = formatDateTime(resetTime);
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head>"
             + "<body style=\"margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;\">"
             + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f6f9;padding:40px 0;\"><tr><td align=\"center\">"
             + "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff;border-radius:8px;overflow:hidden;max-width:600px;width:100%;\">"
             + "<tr><td style=\"background-color:#2e7d32;padding:24px;text-align:center;\">"
             + "<p style=\"margin:0;font-size:24px;font-weight:bold;color:#ffffff;\">Bank Simulator</p>"
             + "<p style=\"margin:6px 0 0;font-size:14px;color:#c8e6c9;\">" + headerTitle + "</p>"
             + "</td></tr>"
             + "<tr><td style=\"padding:32px 40px;color:#333333;\">"
             + "<p style=\"font-size:16px;margin:0 0 16px;\">Dear " + userName + ",</p>"
             + "<p style=\"font-size:15px;margin:0 0 8px;\"><strong>" + actionText + "</strong> on <strong>" + resetStr + "</strong>.</p>"
             + "<p style=\"font-size:15px;margin:0 0 24px;\">If you made this change, no further action is required.</p>"
             + "<div style=\"background-color:#fff8e1;border-left:4px solid #f9a825;padding:16px;border-radius:4px;color:#5d4037;margin-bottom:24px;\">"
             + "<p style=\"margin:0 0 8px;font-weight:bold;\">" + warningText + "</p>"
             + "<p style=\"margin:0;font-size:14px;\">Email: support@banksimulator.com | Phone: 1800-XXX-XXXX</p>"
             + "</div>"
             + "<p style=\"font-size:14px;font-weight:bold;margin:0 0 8px;\">For your security:</p>"
             + "<ul style=\"padding-left:20px;line-height:1.8;font-size:14px;color:#555555;\">"
             + "<li>Never share your credentials with anyone</li>"
             + "<li>Use a unique password/PIN for banking</li>"
             + "<li>Enable alerts for all transactions</li>"
             + "</ul></td></tr>"
             + "<tr><td style=\"background-color:#f4f6f9;padding:16px;text-align:center;border-top:1px solid #eeeeee;\">"
             + "<p style=\"margin:0;font-size:12px;color:#999999;\">&copy; 2025 Bank Simulator. All rights reserved.</p>"
             + "</td></tr></table></td></tr></table></body></html>";
    }

    // ─── DateTime Formatter (IST) ─────────────────────────────────────────────

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        java.time.ZonedDateTime ist = dateTime.atZone(java.time.ZoneId.of("Asia/Kolkata"));
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' h:mm a 'IST'",
                        java.util.Locale.ENGLISH);
        return ist.format(formatter);
    }
}
