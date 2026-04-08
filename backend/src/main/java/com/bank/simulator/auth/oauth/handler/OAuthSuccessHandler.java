package com.bank.simulator.auth.oauth.handler;

import com.bank.simulator.auth.oauth.service.OAuthService;
import com.bank.simulator.dto.LoginResponse;
import com.bank.simulator.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthService oAuthService;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:5173/oauth-success}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = extractOAuth2User(authentication);
        if (oAuth2User == null) {
            response.sendRedirect(buildErrorRedirect("oauth_principal_not_supported"));
            return;
        }

        try {
            LoginResponse loginResponse = oAuthService.handleGoogleLogin(oAuth2User);
            String userJson = objectMapper.writeValueAsString(loginResponse.getUser());
            String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                    .fragment("token=" + encode(loginResponse.getToken()) + "&user=" + encode(userJson))
                    .build(true)
                    .toUriString();

            clearAuthenticationAttributes(request);
            response.sendRedirect(targetUrl);
        } catch (BusinessException ex) {
            String errorCode = ex.getStatus() == HttpStatus.FORBIDDEN ? "account_deactivated" : "oauth_processing_failed";
            log.warn("OAuth login blocked: {}", ex.getMessage());
            response.sendRedirect(buildErrorRedirect(errorCode));
        } catch (Exception ex) {
            log.error("Unexpected OAuth success handling error", ex);
            response.sendRedirect(buildErrorRedirect("oauth_processing_failed"));
        }
    }

    private OAuth2User extractOAuth2User(Authentication authentication) {
        if (authentication == null) {
            log.error("OAuth success handler invoked with null authentication");
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User;
        }

        String principalType = principal == null ? "null" : principal.getClass().getName();
        log.error("OAuth success handler invoked with unsupported principal: {}", principalType);
        return null;
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

    private String buildErrorRedirect(String errorCode) {
        return UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .fragment("error=" + encode(errorCode))
                .build(true)
                .toUriString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
