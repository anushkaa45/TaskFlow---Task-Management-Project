
package com.taskflow.service;

import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;

import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.entity.NotificationType;

import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.ProjectMemberRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final AuthenticatedUserService authenticatedUserService;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            NotificationService notificationService,
            AuthenticatedUserService authenticatedUserService) {

        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // Convert Comment entity into response DTO
    private CommentResponseDTO mapToResponse(Comment comment) {

        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getCreatedAt()
        );
    }

    // Check whether logged-in user can access the task's project
    private void checkProjectAccess(Task task) {

        String role = authenticatedUserService.getCurrentUserRole();
        Long currentUserId = authenticatedUserService.getCurrentUserId();

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("PROJECT_MANAGER".equals(role)
                && task.getProject().getCreatedBy().getId().equals(currentUserId)) {
            return;
        }

        if ("MEMBER".equals(role)
                && projectMemberRepository.existsByProject_IdAndUser_Id(
                task.getProject().getId(), currentUserId)) {
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to access comments in this project");
    }

    // Add a comment
    @Transactional
    public CommentResponseDTO addComment(CommentRequestDTO request) {

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + request.getTaskId()));

        // Verify project access
        checkProjectAccess(task);

        // Always identify the author using the authenticated user
        User user = userRepository.findById(
                        authenticatedUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"));

        Comment comment = new Comment();

        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        // Notify the assigned user, if different from the commenter
        if (task.getAssignedTo() != null) {

            Long assignedUserId = task.getAssignedTo().getId();

            if (!assignedUserId.equals(user.getId())) {

                notificationService.createNotification(
                        assignedUserId,
                        "A new comment was added to your task: " + task.getTitle(),
                        NotificationType.NEW_COMMENT
                );
            }
        }

        return mapToResponse(savedComment);
    }

    // Get comments for a particular task
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId));

        checkProjectAccess(task);

        return commentRepository.findByTask_Id(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get comments written by a particular user
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByUser(Long userId) {

        String role = authenticatedUserService.getCurrentUserRole();
        Long currentUserId = authenticatedUserService.getCurrentUserId();

        // Users can view their own comments.
        // Admin can view comments of any user.
        if (!"ADMIN".equals(role) && !currentUserId.equals(userId)) {
            throw new AccessDeniedException(
                    "You can only view your own comments");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        return commentRepository.findByUser_Id(userId)
                .stream()
                .filter(comment -> {
                    try {
                        checkProjectAccess(comment.getTask());
                        return true;
                    } catch (AccessDeniedException ex) {
                        return false;
                    }
                })
                .map(this::mapToResponse)
                .toList();
    }

    // Delete a comment
    @Transactional
    public void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found with id: " + commentId));

        String role = authenticatedUserService.getCurrentUserRole();
        Long currentUserId = authenticatedUserService.getCurrentUserId();

        // Admin can delete any comment
        if ("ADMIN".equals(role)) {
            commentRepository.delete(comment);
            return;
        }

        // A user can delete their own comment
        if (comment.getUser().getId().equals(currentUserId)) {
            checkProjectAccess(comment.getTask());
            commentRepository.delete(comment);
            return;
        }

        // Project Manager can delete comments in their own projects
        if ("PROJECT_MANAGER".equals(role)
                && comment.getTask().getProject()
                .getCreatedBy().getId().equals(currentUserId)) {

            commentRepository.delete(comment);
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to delete this comment");
    }
}
