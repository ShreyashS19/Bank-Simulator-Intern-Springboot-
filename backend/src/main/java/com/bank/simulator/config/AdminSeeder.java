package com.bank.simulator.config;

import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole("ADMIN")) {
            UserEntity admin = UserEntity.builder()
                    .fullName("System Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("=== ADMIN USER SEEDED ===");
            log.info("Email: {}", adminEmail);
            log.info("Password: Admin@123456 (as configured)");
            log.info("========================");
        } else {
            log.info("Admin user already exists. Skipping seed.");
        }
    }
}
