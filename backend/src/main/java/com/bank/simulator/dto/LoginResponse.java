package com.bank.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private UserDto user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private String id;
        private String fullName;
        private String email;
        private String role;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
