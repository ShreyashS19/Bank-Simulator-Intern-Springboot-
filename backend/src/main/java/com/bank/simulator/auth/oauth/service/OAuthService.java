package com.bank.simulator.auth.oauth.service;

import com.bank.simulator.auth.oauth.dto.OAuthUserInfo;
import com.bank.simulator.auth.user.AuthProvider;
import com.bank.simulator.dto.LoginResponse;
import com.bank.simulator.entity.UserEntity;
import com.bank.simulator.exception.BusinessException;
import com.bank.simulator.repository.UserRepository;
import com.bank.simulator.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse handleGoogleLogin(OAuth2User oAuth2User) {
        OAuthUserInfo userInfo = mapGoogleUser(oAuth2User);
        UserEntity user = userRepository.findByEmail(userInfo.email())
                .map(this::validateActiveUser)
                .orElseGet(() -> createGoogleUser(userInfo));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return LoginResponse.builder()
                .token(token)
                .user(toUserDto(user))
                .build();
    }

    private UserEntity validateActiveUser(UserEntity user) {
        if (!user.isActive()) {
            throw new BusinessException("Your account has been deactivated. Please contact support.", HttpStatus.FORBIDDEN);
        }

        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.LOCAL);
            return userRepository.save(user);
        }

        return user;
    }

    private UserEntity createGoogleUser(OAuthUserInfo userInfo) {
        UserEntity user = UserEntity.builder()
                .fullName(userInfo.fullName())
                .email(userInfo.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role("USER")
                .active(true)
                .provider(AuthProvider.GOOGLE)
                .build();

        try {
            UserEntity savedUser = userRepository.save(user);
            log.info("New Google user created: {}", savedUser.getEmail());
            return savedUser;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Google user creation raced for email {}. Reusing existing user.", userInfo.email());
            return userRepository.findByEmail(userInfo.email())
                    .map(this::validateActiveUser)
                    .orElseThrow(() -> new BusinessException("Unable to process Google login", HttpStatus.CONFLICT));
        }
    }

    private LoginResponse.UserDto toUserDto(UserEntity user) {
        return LoginResponse.UserDto.builder()
                .id(String.valueOf(user.getId()))
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private OAuthUserInfo mapGoogleUser(OAuth2User oAuth2User) {
        String email = toLowerTrimmed(oAuth2User.getAttribute("email"));
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("Google account email is not available", HttpStatus.BAD_REQUEST);
        }

        String fullName = resolveFullName(oAuth2User, email);
        return new OAuthUserInfo(email, fullName);
    }

    private String resolveFullName(OAuth2User oAuth2User, String email) {
        String name = trimmed(oAuth2User.getAttribute("name"));
        if (StringUtils.hasText(name)) {
            return name;
        }

        String givenName = trimmed(oAuth2User.getAttribute("given_name"));
        String familyName = trimmed(oAuth2User.getAttribute("family_name"));
        String combined = (givenName + " " + familyName).trim();
        if (StringUtils.hasText(combined)) {
            return combined;
        }

        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : "Google User";
    }

    private String toLowerTrimmed(Object value) {
        String text = trimmed(value);
        return StringUtils.hasText(text) ? text.toLowerCase() : text;
    }

    private String trimmed(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
