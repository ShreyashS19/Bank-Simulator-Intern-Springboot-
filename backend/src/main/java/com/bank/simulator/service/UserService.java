package com.bank.simulator.service;

import com.bank.simulator.dto.LoginRequest;
import com.bank.simulator.dto.LoginResponse;
import com.bank.simulator.dto.SignupRequest;
import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.CustomerRepository;
import com.bank.simulator.repository.UserRepository;
import com.bank.simulator.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
                .build();

        UserEntity saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return saved;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new BusinessException("Your account has been deactivated. Please contact support.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
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
}
