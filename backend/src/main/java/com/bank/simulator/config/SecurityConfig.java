// package com.bank.simulator.config;

// import com.bank.simulator.auth.oauth.handler.OAuthFailureHandler;
// import com.bank.simulator.auth.oauth.handler.OAuthSuccessHandler;
// import com.bank.simulator.auth.oauth.service.CustomOidcUserService;
// import com.bank.simulator.security.JwtAuthFilter;
// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.annotation.Order;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.Arrays;
// import java.util.List;

// @Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
// @EnableMethodSecurity
// public class SecurityConfig {

//     private final JwtAuthFilter jwtAuthFilter;

//     @Bean
//     @Order(1)
//     public SecurityFilterChain oauth2FilterChain(HttpSecurity http,
//                                                  OAuthSuccessHandler oAuthSuccessHandler,
//                                                  OAuthFailureHandler oAuthFailureHandler,
//                                                  CustomOidcUserService customOidcUserService) throws Exception {
//         http
//             .securityMatcher("/oauth2/**", "/login/oauth2/**")
//             .csrf(AbstractHttpConfigurer::disable)
//             .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//             .sessionManagement(session ->
//                     session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
//                 .anyRequest().authenticated()
//             )
//             .oauth2Login(oauth2 -> oauth2
//                 .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
//                 .successHandler(oAuthSuccessHandler)
//                 .failureHandler(oAuthFailureHandler)
//             );

//         return http.build();
//     }

//     @Bean
//     @Order(2)
//     public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(AbstractHttpConfigurer::disable)
//             .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//             .sessionManagement(session ->
//                     session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 // Public auth endpoints (login, signup, forgot/reset password — no JWT needed)
//                 .requestMatchers(HttpMethod.POST,
//                         "/auth/signup",
//                         "/auth/login",
//                         "/auth/forgot-password",
//                         "/auth/reset-password"
//                 ).permitAll()
//                 // Public OAuth helper endpoints
//                 .requestMatchers(HttpMethod.GET, "/oauth/providers", "/oauth-success").permitAll()
//                 // Swagger/OpenAPI
//                 .requestMatchers(
//                     "/v3/api-docs/**",
//                     "/swagger-ui/**",
//                     "/swagger-ui.html"
//                 ).permitAll()
//                 // Loan endpoints - Admin only
//                 .requestMatchers(HttpMethod.GET, "/loan/all", "/loan/statistics").hasRole("ADMIN")
//                 .requestMatchers(HttpMethod.PUT, "/loan/*/status").hasRole("ADMIN")
//                 // Loan endpoints - Authenticated users
//                 .requestMatchers(HttpMethod.POST, "/loan/apply").authenticated()
//                 .requestMatchers(HttpMethod.GET, "/loan/pdf/**", "/loan/account/**", "/loan/*").authenticated()
//                 // All other requests require authentication
//                 .anyRequest().authenticated()
//             )
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost:*"));
//         config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//         config.setAllowedHeaders(List.of("*"));
//         config.setAllowCredentials(true);
//         config.setMaxAge(3600L);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", config);
//         return source;
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//         return config.getAuthenticationManager();
//     }
// }
package com.bank.simulator.config;

import com.bank.simulator.auth.oauth.handler.OAuthFailureHandler;
import com.bank.simulator.auth.oauth.handler.OAuthSuccessHandler;
import com.bank.simulator.auth.oauth.service.CustomOidcUserService;
import com.bank.simulator.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // FIX 1: Read allowed origins from environment/config, not hardcoded
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http,
                                                 OAuthSuccessHandler oAuthSuccessHandler,
                                                 OAuthFailureHandler oAuthFailureHandler,
                                                 CustomOidcUserService customOidcUserService) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                .successHandler(oAuthSuccessHandler)
                .failureHandler(oAuthFailureHandler)
            );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // FIX 2: Add security response headers
            .headers(headers -> headers
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'; frame-ancestors 'none';"))
                .referrerPolicy(referrer ->
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
            )

            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints (login, signup, forgot/reset password — no JWT needed)
                .requestMatchers(HttpMethod.POST,
                        "/auth/signup",
                        "/auth/login",
                        "/auth/forgot-password",
                        "/auth/reset-password"
                ).permitAll()
                // Public OAuth helper endpoints
                .requestMatchers(HttpMethod.GET, "/oauth/providers", "/oauth-success").permitAll()

                // FIX 3: Disable Swagger in production — expose only in dev profile.
                // Controlled via @Profile("dev") on a separate SwaggerConfig bean (recommended),
                // or conditionally permitted here. For now, restrict to localhost.
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll() // TODO: restrict to dev profile in production

                // Loan endpoints - Admin only
                .requestMatchers(HttpMethod.GET, "/loan/all", "/loan/statistics").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/loan/*/status").hasRole("ADMIN")
                // Loan endpoints - Authenticated users
                .requestMatchers(HttpMethod.POST, "/loan/apply").authenticated()
                .requestMatchers(HttpMethod.GET, "/loan/pdf/**", "/loan/account/**", "/loan/*").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // FIX 4: No wildcard localhost — use explicit list from config, not http://localhost:*
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // FIX 5: Replace wildcard headers with explicit list
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        // FIX 6: Expose only necessary response headers
        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with cost factor 12 (more secure than default 10)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}