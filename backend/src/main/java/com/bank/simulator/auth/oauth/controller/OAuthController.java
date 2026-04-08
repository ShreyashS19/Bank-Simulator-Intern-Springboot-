package com.bank.simulator.auth.oauth.controller;

import com.bank.simulator.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OAuthController {

    private static final String DEFAULT_CLIENT_ID = "google-client-id-not-configured";
    private static final String DEFAULT_CLIENT_SECRET = "google-client-secret-not-configured";

    @Value("${spring.security.oauth2.client.registration.google.client-id:" + DEFAULT_CLIENT_ID + "}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:" + DEFAULT_CLIENT_SECRET + "}")
    private String googleClientSecret;

    @GetMapping("/oauth/providers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProviders() {
        boolean googleConfigured = isGoogleConfigured();
        Map<String, Object> providers = Map.of(
                "googleAuthorizationPath", "/oauth2/authorization/google",
                "googleConfigured", googleConfigured,
                "configurationMessage", googleConfigured
                        ? "Google OAuth is configured"
                        : "Google OAuth credentials are missing on backend. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET."
        );

        return ResponseEntity.ok(ApiResponse.success("OAuth providers loaded", providers));
    }

    @GetMapping("/oauth-success")
    public ResponseEntity<ApiResponse<Map<String, String>>> oauthSuccessInfo() {
        Map<String, String> details = Map.of(
                "message", "OAuth success is handled via frontend callback route",
                "callbackPath", "/oauth-success"
        );

        return ResponseEntity.ok(ApiResponse.success("OAuth callback information", details));
    }

    private boolean isGoogleConfigured() {
        return StringUtils.hasText(googleClientId)
                && StringUtils.hasText(googleClientSecret)
                && !DEFAULT_CLIENT_ID.equals(googleClientId)
                && !DEFAULT_CLIENT_SECRET.equals(googleClientSecret);
    }
}
