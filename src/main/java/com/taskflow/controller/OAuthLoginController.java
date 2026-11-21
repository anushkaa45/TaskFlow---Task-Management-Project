package com.taskflow.controller;

import com.taskflow.dto.request.OAuthCodeExchangeRequest;
import com.taskflow.dto.response.AuthResponseDTO;
import com.taskflow.entity.User;
import com.taskflow.service.CustomUserDetailsService;
import com.taskflow.service.JwtService;
import com.taskflow.service.OAuthLoginCodeService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthLoginController {

    private final OAuthLoginCodeService codeService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    public OAuthLoginController(
            OAuthLoginCodeService codeService,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService) {

        this.codeService = codeService;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/exchange")
    public ResponseEntity<AuthResponseDTO> exchangeCode(
            @Valid @RequestBody OAuthCodeExchangeRequest request) {

        try {
            User user = codeService.consumeCode(request.getCode());

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getEmail());

            String token = jwtService.generateToken(userDetails);

            AuthResponseDTO response =
                    new AuthResponseDTO(token, "Bearer", expiration);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid, expired, or already used login code"
            );
        }
    }
}