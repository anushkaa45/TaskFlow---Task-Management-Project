package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.exception.GlobalExceptionHandler;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.service.TaskService;
import com.taskflow.service.JwtService;
import com.taskflow.service.CustomUserDetailsService;

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

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        TaskControllerTest.MethodSecurityTestConfig.class,
        GlobalExceptionHandler.class
})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasks_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);

        when(taskService.getAllTasks())
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasks_emptyList() throws Exception {

        when(taskService.getAllTasks())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTaskById_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);

        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk());

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTaskById_notFound() throws Exception {

        when(taskService.getTaskById(1L))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getTasksByProject_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);

        when(taskService.getTasksByProject(1L))
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService, times(1)).getTasksByProject(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getTasksByProject_emptyList() throws Exception {

        when(taskService.getTasksByProject(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/project/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(taskService, times(1)).getTasksByProject(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getTasksByUser_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);

        when(taskService.getTasksByUser(1L))
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(taskService, times(1)).getTasksByUser(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getTasksByUser_emptyList() throws Exception {

        when(taskService.getTasksByUser(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(taskService, times(1)).getTasksByUser(1L);
    }
}