
package com.taskflow.config;

import com.taskflow.entity.AuthProvider;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.OAuthLoginCodeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OAuthLoginCodeService codeService;

    @Value("${app.frontend.oauth-callback-url}")
    private String frontendCallbackUrl;

    public GoogleOAuth2SuccessHandler(
            UserRepository userRepository,
            OAuthLoginCodeService codeService) {

        this.userRepository = userRepository;
        this.codeService = codeService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid Google authentication"
            );
            return;
        }

        String googleSubject = oidcUser.getSubject();

        User user = userRepository
                .findByAuthProviderAndProviderSubject(
                        AuthProvider.GOOGLE,
                        googleSubject
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Google account was not registered."
                        )
                );

        String code = codeService.createCode(user);

        // Send the temporary code in the URL fragment.
        // The fragment is not sent in HTTP requests to the server.
        String redirectUrl = frontendCallbackUrl + "#code=" + code;

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.sendRedirect(redirectUrl);
    }
}
