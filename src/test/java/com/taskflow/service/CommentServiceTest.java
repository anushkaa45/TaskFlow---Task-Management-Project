
package com.taskflow.service;

import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;
import com.taskflow.entity.*;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void addComment_taskNotFound() {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setTaskId(1L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.addComment(request)
        );

        verify(taskRepository, times(1)).findById(1L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentsByTask_taskNotFound() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.getCommentsByTask(1L)
        );

        verify(taskRepository, times(1)).findById(1L);
        verify(commentRepository, never()).findByTask_Id(anyLong());
    }

    @Test
    void addComment_adminSuccess() {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Authentication implementation completed");
        request.setTaskId(1L);

        Task task = mock(Task.class);
        User user = mock(User.class);
        Comment savedComment = mock(Comment.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(10L);
        when(user.getName()).thenReturn("Test User");

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        when(savedComment.getId()).thenReturn(100L);
        when(savedComment.getContent())
                .thenReturn("Authentication implementation completed");
        when(savedComment.getTask()).thenReturn(task);
        when(savedComment.getUser()).thenReturn(user);
        when(savedComment.getCreatedAt()).thenReturn(
                java.time.LocalDateTime.now()
        );
        when(task.getId()).thenReturn(1L);

        CommentResponseDTO response = commentService.addComment(request);

        assertNotNull(response);

        verify(commentRepository, times(1))
                .save(any(Comment.class));

        verify(notificationService, never())
                .createNotification(anyLong(), anyString(), any());
    }

    @Test
    void addComment_memberSuccess() {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Task update completed");
        request.setTaskId(1L);

        Task task = mock(Task.class);
        User user = mock(User.class);
        Comment savedComment = mock(Comment.class);
        Project project = mock(Project.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(5L);

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(5L, 10L))
                .thenReturn(true);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(10L);
        when(user.getName()).thenReturn("Test User");

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        when(savedComment.getId()).thenReturn(101L);
        when(savedComment.getContent()).thenReturn("Task update completed");
        when(savedComment.getTask()).thenReturn(task);
        when(savedComment.getUser()).thenReturn(user);
        when(savedComment.getCreatedAt()).thenReturn(
                java.time.LocalDateTime.now()
        );
        when(task.getId()).thenReturn(1L);

        CommentResponseDTO response = commentService.addComment(request);

        assertNotNull(response);

        verify(projectMemberRepository, times(1))
                .existsByProject_IdAndUser_Id(5L, 10L);

        verify(commentRepository, times(1))
                .save(any(Comment.class));
    }

    @Test
    void addComment_memberAccessDenied() {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Test comment");
        request.setTaskId(1L);

        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(5L);

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(5L, 10L))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> commentService.addComment(request)
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_projectManagerAccessDenied() {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Test comment");
        request.setTaskId(1L);

        Task task = mock(Task.class);
        Project project = mock(Project.class);
        User creator = mock(User.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(task.getProject()).thenReturn(project);
        when(project.getCreatedBy()).thenReturn(creator);
        when(creator.getId()).thenReturn(20L);

        assertThrows(
                AccessDeniedException.class,
                () -> commentService.addComment(request)
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getCommentsByTask_success() {

        Task task = mock(Task.class);
        Project project = mock(Project.class);
        User creator = mock(User.class);
        Comment comment = mock(Comment.class);
        User author = mock(User.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(commentRepository.findByTask_Id(1L))
                .thenReturn(List.of(comment));

        when(comment.getId()).thenReturn(100L);
        when(comment.getContent()).thenReturn("Test comment");
        when(comment.getTask()).thenReturn(task);
        when(comment.getUser()).thenReturn(author);
        when(comment.getCreatedAt()).thenReturn(
                java.time.LocalDateTime.now()
        );

        when(task.getId()).thenReturn(1L);
        when(author.getId()).thenReturn(10L);
        when(author.getName()).thenReturn("Test User");

        List<CommentResponseDTO> result =
                commentService.getCommentsByTask(1L);

        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).getContent());

        verify(commentRepository).findByTask_Id(1L);
    }

    @Test
    void getCommentsByTask_memberAccessDenied() {

        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(5L);

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(5L, 10L))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> commentService.getCommentsByTask(1L)
        );

        verify(commentRepository, never())
                .findByTask_Id(anyLong());
    }

    @Test
    void getCommentsByUser_adminSuccess() {

        User user = mock(User.class);
        Comment comment = mock(Comment.class);
        Task task = mock(Task.class);
        User author = mock(User.class);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");
        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(userRepository.findById(20L))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByUser_Id(20L))
                .thenReturn(List.of(comment));

        when(comment.getTask()).thenReturn(task);
        when(comment.getId()).thenReturn(100L);
        when(comment.getContent()).thenReturn("Admin test comment");
        when(comment.getUser()).thenReturn(author);
        when(comment.getCreatedAt()).thenReturn(
                java.time.LocalDateTime.now()
        );

        when(task.getId()).thenReturn(1L);
        when(author.getId()).thenReturn(20L);
        when(author.getName()).thenReturn("Test User");

        List<CommentResponseDTO> result =
                commentService.getCommentsByUser(20L);

        assertEquals(1, result.size());
        assertEquals("Admin test comment", result.get(0).getContent());

        verify(commentRepository).findByUser_Id(20L);
    }

    @Test
    void getCommentsByUser_memberOwnComments() {

        User user = mock(User.class);
        Comment comment = mock(Comment.class);
        Task task = mock(Task.class);
        Project project = mock(Project.class);
        User author = mock(User.class);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");
        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByUser_Id(10L))
                .thenReturn(List.of(comment));

        when(comment.getTask()).thenReturn(task);
        when(comment.getId()).thenReturn(101L);
        when(comment.getContent()).thenReturn("My comment");
        when(comment.getUser()).thenReturn(author);
        when(comment.getCreatedAt()).thenReturn(
                java.time.LocalDateTime.now()
        );

        when(task.getId()).thenReturn(1L);
        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(5L);

        when(author.getId()).thenReturn(10L);
        when(author.getName()).thenReturn("Member");

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(5L, 10L))
                .thenReturn(true);

        List<CommentResponseDTO> result =
                commentService.getCommentsByUser(10L);

        assertEquals(1, result.size());
        assertEquals("My comment", result.get(0).getContent());
    }

    @Test
    void getCommentsByUser_accessDenied() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        assertThrows(
                AccessDeniedException.class,
                () -> commentService.getCommentsByUser(20L)
        );

        verify(userRepository, never()).findById(anyLong());
        verify(commentRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void getCommentsByUser_userNotFound() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(userRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.getCommentsByUser(20L)
        );

        verify(commentRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void deleteComment_adminSuccess() {

        Comment comment = mock(Comment.class);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        commentService.deleteComment(1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ownerSuccess() {

        Comment comment = mock(Comment.class);
        User user = mock(User.class);
        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(comment.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        when(comment.getTask()).thenReturn(task);
        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(5L);

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(5L, 10L))
                .thenReturn(true);

        commentService.deleteComment(1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_projectManagerSuccess() {

        Comment comment = mock(Comment.class);
        User commentOwner = mock(User.class);
        User creator = mock(User.class);
        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(comment.getUser()).thenReturn(commentOwner);
        when(commentOwner.getId()).thenReturn(20L);

        when(comment.getTask()).thenReturn(task);
        when(task.getProject()).thenReturn(project);
        when(project.getCreatedBy()).thenReturn(creator);
        when(creator.getId()).thenReturn(10L);

        commentService.deleteComment(1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_notFound() {

        when(commentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.deleteComment(1L)
        );

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteComment_accessDenied() {

        Comment comment = mock(Comment.class);
        User owner = mock(User.class);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(comment.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(20L);

        assertThrows(
                AccessDeniedException.class,
                () -> commentService.deleteComment(1L)
        );

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteComment_projectManagerAccessDenied() {

        Comment comment = mock(Comment.class);
        User owner = mock(User.class);
        User creator = mock(User.class);
        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(comment.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(20L);

        when(comment.getTask()).thenReturn(task);
        when(task.getProject()).thenReturn(project);
        when(project.getCreatedBy()).thenReturn(creator);
        when(creator.getId()).thenReturn(30L);

        assertThrows(
                AccessDeniedException.class,
                () -> commentService.deleteComment(1L)
        );

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void getCommentsByUser_filtersInaccessibleProjectComments() {

        User user = mock(User.class);
        Comment comment = mock(Comment.class);
        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(commentRepository.findByUser_Id(10L))
                .thenReturn(List.of(comment));

        when(comment.getTask()).thenReturn(task);
        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(5L);

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(5L, 10L))
                .thenReturn(false);

        List<CommentResponseDTO> result =
                commentService.getCommentsByUser(10L);

        assertTrue(result.isEmpty());

        verify(commentRepository).findByUser_Id(10L);
    }

    @Test
    void addComment_assignedUserNotification() {

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("New update");
        request.setTaskId(1L);

        Task task = mock(Task.class);
        Project project = mock(Project.class);
        User commenter = mock(User.class);
        User assignedUser = mock(User.class);
        Comment savedComment = mock(Comment.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(commenter));

        when(commenter.getId()).thenReturn(10L);
        when(commenter.getName()).thenReturn("Commenter");

        when(task.getAssignedTo()).thenReturn(assignedUser);
        when(assignedUser.getId()).thenReturn(20L);
        when(task.getTitle()).thenReturn("JWT Authentication");

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        when(savedComment.getId()).thenReturn(100L);
        when(savedComment.getContent()).thenReturn("New update");
        when(savedComment.getTask()).thenReturn(task);
        when(savedComment.getUser()).thenReturn(commenter);
        when(savedComment.getCreatedAt())
                .thenReturn(java.time.LocalDateTime.now());
        when(task.getId()).thenReturn(1L);

        commentService.addComment(request);

        verify(notificationService).createNotification(
                eq(20L),
                eq("A new comment was added to your task: JWT Authentication"),
                eq(NotificationType.NEW_COMMENT)
        );
    }
}
