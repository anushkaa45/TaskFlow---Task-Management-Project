
package com.taskflow.service;

import com.taskflow.dto.request.LoginRequestDTO;
import com.taskflow.dto.response.AuthResponseDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        // Verify email and password
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        // Get authenticated user details
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // Return token response
        return new AuthResponseDTO(
                token,
                "Bearer",
                expiration
        );
    }
}
