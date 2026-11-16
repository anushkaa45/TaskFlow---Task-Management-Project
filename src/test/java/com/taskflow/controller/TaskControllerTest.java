package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateTask_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);

        when(taskService.updateTask(
                eq(1L),
                any(TaskUpdateRequestDTO.class)
        )).thenReturn(task);

        String requestBody = """
            {
                "title": "Implement JWT authentication",
                "description": "Complete JWT login and token validation",
                "status": "IN_PROGRESS",
                "priority": "HIGH",
                "dueDate": "2027-12-20"
            }
            """;

        mockMvc.perform(put("/api/tasks/1")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(taskService, times(1))
                .updateTask(eq(1L), any(TaskUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateTask_invalidTitle() throws Exception {

        String requestBody = """
            {
                "title": "",
                "description": "Complete JWT login and token validation",
                "status": "IN_PROGRESS",
                "priority": "HIGH",
                "dueDate": "2027-12-20"
            }
            """;

        mockMvc.perform(put("/api/tasks/1")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .updateTask(eq(1L), any(TaskUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTask_adminSuccess() throws Exception {

        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteTask_memberAccessDenied() throws Exception {

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isForbidden());

        verify(taskService, never()).deleteTask(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasksWithPagination_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);

        Page<TaskResponseDTO> taskPage =
                new PageImpl<>(List.of(task));

        when(taskService.getAllTasksWithPagination(0, 5))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(taskService, times(1))
                .getAllTasksWithPagination(0, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasksWithPagination_invalidPage() throws Exception {

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getAllTasksWithPagination(anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTasksByStatus_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);
        Page<TaskResponseDTO> taskPage =
                new PageImpl<>(List.of(task));

        when(taskService.getTasksByStatus(TaskStatus.IN_PROGRESS, 0, 5))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/filter/status")
                        .param("status", "IN_PROGRESS")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(taskService, times(1))
                .getTasksByStatus(TaskStatus.IN_PROGRESS, 0, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTasksByStatus_invalidPage() throws Exception {

        mockMvc.perform(get("/api/tasks/filter/status")
                        .param("status", "IN_PROGRESS")
                        .param("page", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getTasksByStatus(any(TaskStatus.class), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTasksByPriority_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);
        Page<TaskResponseDTO> taskPage =
                new PageImpl<>(List.of(task));

        when(taskService.getTasksByPriority(TaskPriority.HIGH, 0, 5))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/filter/priority")
                        .param("priority", "HIGH")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(taskService, times(1))
                .getTasksByPriority(TaskPriority.HIGH, 0, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTasksByPriority_invalidPage() throws Exception {

        mockMvc.perform(get("/api/tasks/filter/priority")
                        .param("priority", "HIGH")
                        .param("page", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getTasksByPriority(any(TaskPriority.class), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchTasksByTitle_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);
        Page<TaskResponseDTO> taskPage =
                new PageImpl<>(List.of(task));

        when(taskService.searchTasksByTitle("JWT", 0, 5))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/search")
                        .param("title", "JWT")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(taskService, times(1))
                .searchTasksByTitle("JWT", 0, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchTasksByTitle_invalidPage() throws Exception {

        mockMvc.perform(get("/api/tasks/search")
                        .param("title", "JWT")
                        .param("page", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .searchTasksByTitle(anyString(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAndFilterTasks_success() throws Exception {

        TaskResponseDTO task = mock(TaskResponseDTO.class);
        Page<TaskResponseDTO> taskPage =
                new PageImpl<>(List.of(task));

        when(taskService.searchAndFilterTasks(
                "JWT", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 0, 5))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/search/filter")
                        .param("title", "JWT")
                        .param("status", "IN_PROGRESS")
                        .param("priority", "HIGH")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(taskService, times(1))
                .searchAndFilterTasks(
                        "JWT", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 0, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAndFilterTasks_invalidPage() throws Exception {

        mockMvc.perform(get("/api/tasks/search/filter")
                        .param("title", "JWT")
                        .param("status", "IN_PROGRESS")
                        .param("priority", "HIGH")
                        .param("page", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .searchAndFilterTasks(
                        anyString(),
                        any(TaskStatus.class),
                        any(TaskPriority.class),
                        anyInt(),
                        anyInt());
    }
}