package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.request.LoginRequestDTO;
import com.taskflow.dto.response.AuthResponseDTO;
import com.taskflow.exception.GlobalExceptionHandler;
import com.taskflow.service.AuthService;
import com.taskflow.service.JwtService;
import com.taskflow.service.CustomUserDetailsService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void login_success() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul@12345");

        AuthResponseDTO response = mock(AuthResponseDTO.class);

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1))
                .login(any(LoginRequestDTO.class));
    }

    @Test
    void login_invalidEmail() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("invalid-email");
        request.setPassword("Rahul@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequestDTO.class));
    }

    @Test
    void login_missingEmail() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("");
        request.setPassword("Rahul@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequestDTO.class));
    }

    @Test
    void login_missingPassword() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("rahul@gmail.com");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequestDTO.class));
    }

    @Test
    void login_invalidCredentials() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("rahul@gmail.com");
        request.setPassword("WrongPassword123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password."));

        verify(authService, times(1))
                .login(any(LoginRequestDTO.class));
    }

    @Test
    void login_blankPassword() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("rahul@gmail.com");
        request.setPassword("   ");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequestDTO.class));
    }
}