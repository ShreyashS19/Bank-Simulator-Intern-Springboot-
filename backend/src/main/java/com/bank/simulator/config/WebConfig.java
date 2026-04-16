package com.bank.simulator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC configuration — separated from SecurityConfig to avoid Spring context conflicts.
 * Registers the IP-based rate limit interceptor ONLY on OTP endpoints.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/auth/forgot-password",
                        "/auth/reset-password",
                        "/account/forgot-pin",
                        "/account/reset-pin"
                );
    }
}
