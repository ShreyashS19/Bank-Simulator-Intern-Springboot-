package com.bank.simulator.service.impl;

import com.bank.simulator.auth.user.AuthProvider;
import com.bank.simulator.dto.LoginRequest;
import com.bank.simulator.dto.LoginResponse;
import com.bank.simulator.dto.SignupRequest;
import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.exception.AccountDeactivatedException;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.repository.UserRepository;
import com.bank.simulator.security.JwtUtil;
import com.bank.simulator.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.simulator.entity.OtpEntity;
import com.bank.simulator.entity.OtpPurpose;
import com.bank.simulator.repository.OtpRepository;
import com.bank.simulator.service.NotificationService;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpRepository otpRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public UserEntity signup(SignupRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("An account with this email already exists");
        }

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .active(true)
                .provider(AuthProvider.LOCAL)
                .build();

        UserEntity saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return saved;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException(
                "No account found with this email. Please sign up or use Google login.",
                HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(
                    "Incorrect password. Please try again or use 'Forgot Password'.",
                    HttpStatus.UNAUTHORIZED);
        }

        // Check account status only after password validation to avoid account enumeration clues.
        if (!user.isActive()) {
            throw new AccountDeactivatedException();
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        log.info("User logged in: {} (role: {})", user.getEmail(), user.getRole());

        LoginResponse.UserDto userDto = LoginResponse.UserDto.builder()
                .id(String.valueOf(user.getId()))
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return LoginResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Override
    public Map<String, Object> checkCustomerExists(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND));

        boolean hasCustomerRecord = customerRepository.existsByEmail(email);

        Map<String, Object> result = new HashMap<>();
        result.put("hasCustomerRecord", hasCustomerRecord);
        result.put("userId", String.valueOf(user.getId()));
        result.put("email", user.getEmail());
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", String.valueOf(u.getId()));
                    map.put("fullName", u.getFullName());
                    map.put("email", u.getEmail());
                    map.put("active", u.isActive());
                    map.put("role", u.getRole());
                    map.put("createdAt", u.getCreatedAt());
                    map.put("updatedAt", u.getUpdatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserByEmail(String email) {
        UserEntity u = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(u.getId()));
        map.put("fullName", u.getFullName());
        map.put("email", u.getEmail());
        map.put("active", u.isActive());
        map.put("role", u.getRole());
        map.put("createdAt", u.getCreatedAt());
        map.put("updatedAt", u.getUpdatedAt());
        return map;
    }

    @Override
    @Transactional
    public void updateUserStatus(String email, boolean active) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("Cannot deactivate the admin account");
        }

        user.setActive(active);
        userRepository.save(user);
        log.info("User {} status updated to active={}", email, active);
    }
    // ======================== FORGOT / RESET PASSWORD ========================

    @Override
    @Transactional
    public void generateAndSendPasswordOtp(String email) {
        // 1. Layer 1 Rate Limiting: Max 3 requests per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = otpRepository.countByEmailAndPurposeAndCreatedAtAfter(
                email, OtpPurpose.PASSWORD_RESET, oneHourAgo);
        
        if (recentRequests >= 3) {
            log.warn("Rate limit exceeded for password reset OTP: {} requests in last hour for email: {}", recentRequests, email);
            throw new BusinessException("Too many OTP requests. Please try again after 1 hour.", HttpStatus.TOO_MANY_REQUESTS);
        }

        // 2. Lookup user silently (don't reveal if email exists or not to prevent enumeration)
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return; // Silent fail (pretend it sent)
        }

        // 3. Invalidate old OTPs for this purpose
        otpRepository.invalidateExistingOtps(email, OtpPurpose.PASSWORD_RESET);

        // 4. Generate 6-digit SecureRandom OTP
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

        // 5. Save OTP Entity
        OtpEntity otpEntity = OtpEntity.builder()
                .email(email)
                .otp(otp)
                .expiryTime(expiryTime)
                .isUsed(false)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .attemptCount(0)
                .build();
        otpRepository.save(otpEntity);

        // 6. Send Async Email
        notificationService.sendPasswordResetOtpEmail(email, user.getFullName(), otp, expiryTime);
        log.info("Password reset OTP generated for email: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        // 1. Fetch the active OTP
        OtpEntity otpEntity = otpRepository.findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
                email, OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BusinessException("Invalid or expired OTP", HttpStatus.BAD_REQUEST));

        // 2. Validate Expiry
        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        // 3. Check attempt count
        if (otpEntity.getAttemptCount() >= 5) {
            throw new BusinessException("Maximum attempts reached. Please request a new OTP.", HttpStatus.TOO_MANY_REQUESTS);
        }

        // 4. Match OTP (increment attempts if wrong)
        if (!otpEntity.getOtp().equals(otp)) {
            otpEntity.setAttemptCount(otpEntity.getAttemptCount() + 1);
            otpRepository.save(otpEntity);
            throw new BusinessException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        // 5. Validate Password Strength
        if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            throw new BusinessException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.", HttpStatus.BAD_REQUEST);
        }

        // 6. Update Password
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User no longer exists", HttpStatus.NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 7. Mark OTP as used
        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        // 8. Send Success Email Async
        notificationService.sendPasswordResetSuccessEmail(email, user.getFullName(), LocalDateTime.now());
        log.info("Password successfully reset for email: {}", email);
    }
}
