package com.taskflow.controller;

import com.taskflow.dto.response.NotificationResponseDTO;
import com.taskflow.service.AuthenticatedUserService;
import com.taskflow.service.CustomUserDetailsService;
import com.taskflow.service.JwtService;
import com.taskflow.service.NotificationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(NotificationControllerTest.MethodSecurityTestConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuthenticatedUserService authenticatedUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyNotifications_success() throws Exception {

        Long userId = 1L;

        NotificationResponseDTO notification =
                mock(NotificationResponseDTO.class);

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        when(notificationService.getUserNotifications(userId))
                .thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(authenticatedUserService, times(1))
                .getCurrentUserId();

        verify(notificationService, times(1))
                .getUserNotifications(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyNotifications_emptyList() throws Exception {

        Long userId = 1L;

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        when(notificationService.getUserNotifications(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(notificationService, times(1))
                .getUserNotifications(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyUnreadNotifications_emptyList() throws Exception {

        Long userId = 1L;

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        when(notificationService.getUnreadNotifications(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/me/unread"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(notificationService, times(1))
                .getUnreadNotifications(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyUnreadNotifications_success() throws Exception {

        Long userId = 1L;

        NotificationResponseDTO notification =
                mock(NotificationResponseDTO.class);

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        when(notificationService.getUnreadNotifications(userId))
                .thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/me/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(notificationService, times(1))
                .getUnreadNotifications(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyUnreadNotificationCount_success() throws Exception {

        Long userId = 1L;

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        when(notificationService.getUnreadNotificationCount(userId))
                .thenReturn(5L);

        mockMvc.perform(get("/api/notifications/me/unread/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(notificationService, times(1))
                .getUnreadNotificationCount(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMyUnreadNotificationCount_zero() throws Exception {

        Long userId = 1L;

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        when(notificationService.getUnreadNotificationCount(userId))
                .thenReturn(0L);

        mockMvc.perform(get("/api/notifications/me/unread/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(notificationService, times(1))
                .getUnreadNotificationCount(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void markAsRead_success() throws Exception {

        Long notificationId = 1L;

        NotificationResponseDTO response =
                mock(NotificationResponseDTO.class);

        when(notificationService.markAsRead(notificationId))
                .thenReturn(response);

        mockMvc.perform(patch("/api/notifications/{notificationId}/read",
                        notificationId))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .markAsRead(notificationId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAsRead_adminSuccess() throws Exception {

        Long notificationId = 2L;

        NotificationResponseDTO response =
                mock(NotificationResponseDTO.class);

        when(notificationService.markAsRead(notificationId))
                .thenReturn(response);

        mockMvc.perform(patch("/api/notifications/{notificationId}/read",
                        notificationId))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .markAsRead(notificationId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void markAllAsRead_success() throws Exception {

        Long userId = 1L;

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        doNothing().when(notificationService).markAllAsRead(userId);

        mockMvc.perform(patch("/api/notifications/me/read-all"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAllAsRead(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void markAllAsRead_adminSuccess() throws Exception {

        Long userId = 2L;

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(userId);

        doNothing().when(notificationService).markAllAsRead(userId);

        mockMvc.perform(patch("/api/notifications/me/read-all"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAllAsRead(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteNotification_success() throws Exception {

        Long notificationId = 1L;

        doNothing().when(notificationService)
                .deleteNotification(notificationId);

        mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1))
                .deleteNotification(notificationId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotification_adminSuccess() throws Exception {

        Long notificationId = 2L;

        doNothing().when(notificationService)
                .deleteNotification(notificationId);

        mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1))
                .deleteNotification(notificationId);
    }
}