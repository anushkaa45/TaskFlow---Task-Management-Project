
package com.taskflow.config;

import com.taskflow.entity.AuthProvider;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.CustomUserDetailsService;
import com.taskflow.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    public GoogleOAuth2SuccessHandler(
            UserRepository userRepository,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String googleSubject = authentication.getName();

        var user = userRepository
                .findByAuthProviderAndProviderSubject(
                        AuthProvider.GOOGLE,
                        googleSubject
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Google account was not registered."
                        )
                );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        String token = jwtService.generateToken(userDetails);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"token\":\"" + token
                        + "\",\"tokenType\":\"Bearer\""
                        + ",\"expiresIn\":" + expiration + "}"
        );
    }
}
