
package com.taskflow.service;

import com.taskflow.entity.AuthProvider;
import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import com.taskflow.repository.UserRepository;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOidcUserService
        extends OidcUserService {

    private final UserRepository userRepository;

    public GoogleOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {

        // Validate the Google OIDC identity token.
        OidcUser oidcUser = super.loadUser(userRequest);

        String googleSubject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        // Require a verified email address.
        if (!Boolean.TRUE.equals(
                oidcUser.getEmailVerified())) {
            throw oauthException(
                    "Google email address is not verified."
            );
        }

        // Required identity attributes.
        if (googleSubject == null || googleSubject.isBlank()
                || email == null || email.isBlank()
                || name == null || name.isBlank()) {
            throw oauthException(
                    "Required Google account information is missing."
            );
        }

        // Find an existing Google account using Google's stable subject.
        User user = userRepository
                .findByAuthProviderAndProviderSubject(
                        AuthProvider.GOOGLE,
                        googleSubject
                )
                .orElse(null);

        if (user != null) {

            // Prevent silently changing the account's email.
            if (!user.getEmail().equalsIgnoreCase(email)) {
                throw oauthException(
                        "Google account email does not match the linked account."
                );
            }

            return oidcUser;
        }

        // Never automatically link an existing account by email.
        if (userRepository.existsByEmail(email)) {
            throw oauthException(
                    "An account with this email already exists. "
                            + "Sign in using your existing login method."
            );
        }

        // Register a new Google account.
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(null);
        newUser.setRole(UserRole.MEMBER);
        newUser.setAuthProvider(AuthProvider.GOOGLE);
        newUser.setProviderSubject(googleSubject);

        userRepository.save(newUser);

        return oidcUser;
    }

    private OAuth2AuthenticationException oauthException(
            String message) {

        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info"),
                message
        );
    }
}
