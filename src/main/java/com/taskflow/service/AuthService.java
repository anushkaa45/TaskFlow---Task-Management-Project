
package com.taskflow.service;

import com.taskflow.dto.request.LoginRequestDTO;
import com.taskflow.dto.response.AuthResponseDTO;
import com.taskflow.entity.AuthProvider;
import com.taskflow.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        // Only LOCAL accounts can use email/password login
        boolean isLocalAccount = userRepository
                .findByEmail(request.getEmail())
                .map(user -> user.getAuthProvider() == AuthProvider.LOCAL)
                .orElse(false);

        if (!isLocalAccount) {
            throw new BadCredentialsException("Invalid email or password");
        }

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

        // Generate TaskFlow JWT token
        String token = jwtService.generateToken(userDetails);

        // Return token response
        return new AuthResponseDTO(
                token,
                "Bearer",
                expiration
        );
    }
}
