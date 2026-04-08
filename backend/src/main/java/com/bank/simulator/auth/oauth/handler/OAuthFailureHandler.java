package com.bank.simulator.auth.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuthFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:5173/oauth-success}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.warn("OAuth login failed: {}", exception.getMessage());

        String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .fragment("error=" + encode("oauth_authentication_failed"))
                .build(true)
                .toUriString();

        response.sendRedirect(targetUrl);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
