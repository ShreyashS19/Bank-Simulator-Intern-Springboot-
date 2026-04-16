package com.bank.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Single reusable OTP table used by BOTH Forgot Password and Reset PIN flows.
 *
 * OTP Rules:
 *  - Expires 10 minutes after generation (expiryTime)
 *  - Single-use: isUsed is set to true immediately after successful validation
 *  - Max 5 wrong attempts per record (attemptCount); blocked after 5
 *  - Max 3 OTP send requests per email per purpose per hour (enforced in service)
 *  - One active OTP per email+purpose (old one invalidated on new request)
 *  - Generated using SecureRandom (cryptographically secure)
 */
@Entity
@Table(name = "otps", indexes = {
    @Index(name = "idx_otp_email_purpose", columnList = "email, purpose")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    /** 6-digit numeric OTP — stored as plain string (not hashed, short-lived) */
    @Column(nullable = false, length = 6)
    private String otp;

    /** Set to LocalDateTime.now().plusMinutes(10) at generation time */
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    /** Marked true immediately after the OTP is successfully used */
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    /** Distinguishes PASSWORD_RESET vs PIN_RESET context */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpPurpose purpose;

    /**
     * Incremented on each failed OTP match attempt.
     * OTP is permanently blocked (and must be re-requested) once this reaches 5.
     */
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    /**
     * Auto-set at creation time. Used for Layer-1 per-email rate limiting:
     * count records within the last 60 minutes.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
