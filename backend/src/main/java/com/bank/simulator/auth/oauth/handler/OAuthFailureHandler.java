package com.bank.simulator.auth.oauth.handler;

import com.bank.simulator.auth.AuthErrorConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuthFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:5173/oauth-success}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorParam = isAccountDeactivated(exception)
                ? AuthErrorConstants.ACCOUNT_DEACTIVATED_PARAM
                : AuthErrorConstants.OAUTH_FAILED_PARAM;
        String targetUrl = buildLoginRedirect(errorParam);

        log.warn("OAuth login failed: {} (redirect error={})", exception.getMessage(), errorParam);
        response.sendRedirect(targetUrl);
    }

    private String buildLoginRedirect(String errorCode) {
        return UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .replacePath("/login")
                .replaceQuery(null)
                .fragment(null)
                .queryParam("error", errorCode)
                .build(true)
                .toUriString();
    }

    private boolean isAccountDeactivated(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return true;
        }

        if (AuthErrorConstants.ACCOUNT_DEACTIVATED_CODE.equalsIgnoreCase(exception.getMessage())) {
            return true;
        }

        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof DisabledException) {
                return true;
            }
            if (AuthErrorConstants.ACCOUNT_DEACTIVATED_CODE.equalsIgnoreCase(cause.getMessage())) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }
}
