
package com.taskflow.service;

import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;

import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;

import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
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

    // Add a comment
    @Transactional
    public CommentResponseDTO addComment(CommentRequestDTO request) {

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + request.getTaskId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        Comment comment = new Comment();

        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        return mapToResponse(savedComment);
    }

    // Get comments for a particular task
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByTask(Long taskId) {

        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId));

        return commentRepository.findByTask_Id(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get comments written by a particular user
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByUser(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        return commentRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Delete a comment
    @Transactional
    public void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found with id: " + commentId));

        commentRepository.delete(comment);
    }
}
