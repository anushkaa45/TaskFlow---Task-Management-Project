
package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.request.UserUpdateRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;

import com.taskflow.exception.GlobalExceptionHandler;

import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.service.UserService;
import com.taskflow.service.JwtService;
import com.taskflow.service.CustomUserDetailsService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;

@WebMvcTest(
        value = UserController.class,
        excludeAutoConfiguration = OAuth2ClientWebSecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalExceptionHandler.class,
        UserControllerTest.MethodSecurityTestConfig.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // Sample response
    private UserResponseDTO getSampleUser() {
        return new UserResponseDTO(
                1L,
                "Rahul Sharma",
                "rahul@gmail.com",
                "MEMBER",
                null
        );
    }

    // Test 1: Successful registration
    @Test
    void registerUser_success() throws Exception {

        UserRequestDTO request = new UserRequestDTO();
        request.setName("Rahul Sharma");
        request.setEmail("rahul@gmail.com");
        request.setPassword("password123");

        when(userService.registerUser(any(UserRequestDTO.class)))
                .thenReturn(getSampleUser());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul Sharma"))
                .andExpect(jsonPath("$.email").value("rahul@gmail.com"));

        verify(userService, times(1))
                .registerUser(any(UserRequestDTO.class));
    }

    // Test 2: Registration with invalid email
    @Test
    void registerUser_invalidEmail() throws Exception {

        UserRequestDTO request = new UserRequestDTO();
        request.setName("Rahul Sharma");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void getAllUsers_admin_success() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(java.util.List.of(getSampleUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rahul Sharma"))
                .andExpect(jsonPath("$[0].email").value("rahul@gmail.com"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void getAllUsers_emptyList() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void getUserById_success() throws Exception {

        when(userService.getUserById(1L))
                .thenReturn(getSampleUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul Sharma"))
                .andExpect(jsonPath("$.email").value("rahul@gmail.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void getUserById_notFound() throws Exception {

        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99L);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Rahul");
        request.setEmail("updated@gmail.com");

        UserResponseDTO response = new UserResponseDTO(
                1L,
                "Updated Rahul",
                "updated@gmail.com",
                "MEMBER",
                null
        );

        when(userService.updateUser(
                eq(1L),
                any(UserUpdateRequestDTO.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Rahul"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));

        verify(userService, times(1))
                .updateUser(eq(1L), any(UserUpdateRequestDTO.class));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void updateUser_invalidEmail() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Rahul");
        request.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {

        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "MEMBER")
    void deleteUser_memberAccessDenied() throws Exception {

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void getUserByEmail_success() throws Exception {

        when(userService.getUserByEmail("rahul@gmail.com"))
                .thenReturn(getSampleUser());

        mockMvc.perform(get("/api/users/email/rahul@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul Sharma"))
                .andExpect(jsonPath("$.email").value("rahul@gmail.com"));

        verify(userService, times(1))
                .getUserByEmail("rahul@gmail.com");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void getUserByEmail_notFound() throws Exception {

        when(userService.getUserByEmail("missing@gmail.com"))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/email/missing@gmail.com"))
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .getUserByEmail("missing@gmail.com");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "MEMBER")
    void getAllUsers_memberAccessDenied() throws Exception {

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "MEMBER")
    void getUserById_memberAccessDenied() throws Exception {

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "MEMBER")
    void updateUser_memberAccessDenied() throws Exception {

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Rahul");
        request.setEmail("updated@gmail.com");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "MEMBER")
    void getUserByEmail_memberAccessDenied() throws Exception {

        mockMvc.perform(get("/api/users/email/rahul@gmail.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void registerUser_invalidName() throws Exception {

        UserRequestDTO request = new UserRequestDTO();
        request.setName("");
        request.setEmail("rahul@gmail.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void registerUser_shortPassword() throws Exception {

        UserRequestDTO request = new UserRequestDTO();
        request.setName("Rahul Sharma");
        request.setEmail("rahul@gmail.com");
        request.setPassword("123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
