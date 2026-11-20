package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;
import com.taskflow.service.CommentService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;

@WebMvcTest(
        value = CommentController.class,
        excludeAutoConfiguration = OAuth2ClientWebSecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(CommentControllerTest.MethodSecurityTestConfig.class)
class CommentControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

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
    void addComment_success() throws Exception {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Task implementation completed");
        request.setTaskId(1L);

        CommentResponseDTO response = mock(CommentResponseDTO.class);

        when(commentService.addComment(any(CommentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(commentService, times(1))
                .addComment(any(CommentRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void addComment_emptyContent() throws Exception {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("");
        request.setTaskId(1L);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .addComment(any(CommentRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void addComment_missingTaskId() throws Exception {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Task implementation completed");
        request.setTaskId(null);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .addComment(any(CommentRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void addComment_contentTooLong() throws Exception {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("A".repeat(2001));
        request.setTaskId(1L);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .addComment(any(CommentRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getCommentsByTask_success() throws Exception {

        Long taskId = 1L;

        CommentResponseDTO comment = mock(CommentResponseDTO.class);

        when(commentService.getCommentsByTask(taskId))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(commentService, times(1)).getCommentsByTask(taskId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getCommentsByTask_emptyList() throws Exception {

        Long taskId = 1L;

        when(commentService.getCommentsByTask(taskId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/comments/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(commentService, times(1)).getCommentsByTask(taskId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getCommentsByUser_success() throws Exception {

        Long userId = 1L;

        CommentResponseDTO comment = mock(CommentResponseDTO.class);

        when(commentService.getCommentsByUser(userId))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(commentService, times(1)).getCommentsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getCommentsByUser_emptyList() throws Exception {

        Long userId = 1L;

        when(commentService.getCommentsByUser(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/comments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(commentService, times(1)).getCommentsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteComment_success() throws Exception {

        Long commentId = 1L;

        doNothing().when(commentService).deleteComment(commentId);

        mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteComment_adminSuccess() throws Exception {

        Long commentId = 2L;

        doNothing().when(commentService).deleteComment(commentId);

        mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addComment_adminSuccess() throws Exception {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Admin comment");
        request.setTaskId(1L);

        CommentResponseDTO response = mock(CommentResponseDTO.class);

        when(commentService.addComment(any(CommentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(commentService, times(1))
                .addComment(any(CommentRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void addComment_projectManagerSuccess() throws Exception {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Project manager comment");
        request.setTaskId(1L);

        CommentResponseDTO response = mock(CommentResponseDTO.class);

        when(commentService.addComment(any(CommentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(commentService, times(1))
                .addComment(any(CommentRequestDTO.class));
    }
}