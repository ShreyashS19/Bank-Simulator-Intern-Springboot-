package com.bank.simulator.controller;

import com.bank.simulator.dto.ApiResponse;
import com.bank.simulator.dto.LoginRequest;
import com.bank.simulator.dto.LoginResponse;
import com.bank.simulator.dto.SignupRequest;
import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * POST /api/auth/signup
     * Register a new user account.
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@Valid @RequestBody SignupRequest request) {
        UserEntity user = userService.signup(request);

        Map<String, Object> userData = Map.of(
                "id", String.valueOf(user.getId()),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "active", user.isActive(),
                "role", user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userData));
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    /**
     * GET /api/auth/check-customer?email=...
     * Check if a customer record exists for the given user email.
     */
    @GetMapping("/check-customer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkCustomerExists(@RequestParam String email) {
        Map<String, Object> result = userService.checkCustomerExists(email);
        return ResponseEntity.ok(ApiResponse.success("Customer check completed", result));
    }

    /**
     * GET /api/auth/users/all
     * Get all registered users (admin use).
     */
    @GetMapping("/users/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * PUT /api/auth/user/status?email=...&active=true/false
     * Enable or disable a user account.
     */
    @PutMapping("/user/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @RequestParam String email,
            @RequestParam boolean active) {
        userService.updateUserStatus(email, active);
        String message = active ? "User account activated successfully" : "User account deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * GET /api/auth/user?email=...
     * Get user details by email.
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserByEmail(@RequestParam String email) {
        Map<String, Object> user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
}
