
package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.service.CustomUserDetailsService;
import com.taskflow.service.JwtService;
import com.taskflow.service.ProjectService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProjectControllerTest.MethodSecurityTestConfig.class)
class ProjectControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllProjects_success() throws Exception {

        ProjectResponseDTO project = mock(ProjectResponseDTO.class);

        when(projectService.getAllProjects())
                .thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllProjects_emptyList() throws Exception {

        when(projectService.getAllProjects())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getProjectById_success() throws Exception {

        Long projectId = 1L;

        ProjectResponseDTO project = mock(ProjectResponseDTO.class);

        when(projectService.getProjectById(projectId))
                .thenReturn(project);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getProjectById(projectId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getProjectById_notFound() throws Exception {

        Long projectId = 10L;

        when(projectService.getProjectById(projectId))
                .thenThrow(new ResourceNotFoundException(
                        "Project not found with id: " + projectId));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProjectById(projectId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProject_success() throws Exception {

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("TaskFlow Project");
        request.setDescription("Project management application");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.PLANNING);

        ProjectResponseDTO response = mock(ProjectResponseDTO.class);

        when(projectService.createProject(any(ProjectRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(projectService, times(1))
                .createProject(any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProject_invalidName() throws Exception {

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("");
        request.setDescription("Project management application");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.PLANNING);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(projectService, never())
                .createProject(any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProject_success() throws Exception {

        Long projectId = 1L;

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("Updated TaskFlow Project");
        request.setDescription("Updated project description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.IN_PROGRESS);

        ProjectResponseDTO response = mock(ProjectResponseDTO.class);

        when(projectService.updateProject(
                eq(projectId),
                any(ProjectUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(projectService, times(1))
                .updateProject(eq(projectId), any(ProjectUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProject_invalidName() throws Exception {

        Long projectId = 1L;

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("");
        request.setDescription("Updated project description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(projectService, never())
                .updateProject(eq(projectId), any(ProjectUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProject_success() throws Exception {
        Long projectId = 1L;

        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteProject_memberAccessDenied() throws Exception {

        Long projectId = 1L;

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isForbidden());

        verify(projectService, never()).deleteProject(projectId);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void createProject_projectManagerSuccess() throws Exception {

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("New Project");
        request.setDescription("Project description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.PLANNING);

        ProjectResponseDTO response = mock(ProjectResponseDTO.class);

        when(projectService.createProject(any(ProjectRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(projectService, times(1))
                .createProject(any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createProject_memberAccessDenied() throws Exception {

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("New Project");
        request.setDescription("Project description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.PLANNING);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(projectService, never())
                .createProject(any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void updateProject_projectManagerSuccess() throws Exception {

        Long projectId = 1L;

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("Updated Project");
        request.setDescription("Updated description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.IN_PROGRESS);

        ProjectResponseDTO response = mock(ProjectResponseDTO.class);

        when(projectService.updateProject(
                eq(projectId), any(ProjectUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(projectService, times(1))
                .updateProject(eq(projectId), any(ProjectUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateProject_memberAccessDenied() throws Exception {

        Long projectId = 1L;

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("Updated Project");
        request.setDescription("Updated description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(projectService, never())
                .updateProject(eq(projectId), any(ProjectUpdateRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void deleteProject_projectManagerSuccess() throws Exception {

        Long projectId = 1L;

        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProject_adminSuccess() throws Exception {

        Long projectId = 2L;

        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void getAllProjects_projectManagerSuccess() throws Exception {

        ProjectResponseDTO project = mock(ProjectResponseDTO.class);

        when(projectService.getAllProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProjectById_adminSuccess() throws Exception {

        Long projectId = 1L;

        ProjectResponseDTO project = mock(ProjectResponseDTO.class);

        when(projectService.getProjectById(projectId)).thenReturn(project);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getProjectById(projectId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getProjectById_memberSuccess() throws Exception {

        Long projectId = 1L;

        ProjectResponseDTO project = mock(ProjectResponseDTO.class);

        when(projectService.getProjectById(projectId)).thenReturn(project);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk());

        verify(projectService, times(1)).getProjectById(projectId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllProjects_adminSuccess() throws Exception {

        ProjectResponseDTO project = mock(ProjectResponseDTO.class);

        when(projectService.getAllProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(projectService, times(1)).getAllProjects();
    }
}
