package com.bank.simulator.auth.oauth.service;

import com.bank.simulator.auth.AuthErrorConstants;
import com.bank.simulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = normalizeEmail(oidcUser.getEmail());

        // Block deactivated accounts before OAuth success handler can issue a JWT.
        if (StringUtils.hasText(email)) {
            userRepository.findByEmail(email)
                    .filter(existingUser -> !existingUser.isActive())
                    .ifPresent(existingUser -> {
                        throw new DisabledException(AuthErrorConstants.ACCOUNT_DEACTIVATED_CODE);
                    });
        }

        return oidcUser;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}
