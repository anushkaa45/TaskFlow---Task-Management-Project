
package com.taskflow.controller;

import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;
import com.taskflow.service.CommentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // All authenticated roles can add comments
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @Valid @RequestBody CommentRequestDTO request) {

        CommentResponseDTO response =
                commentService.addComment(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // All authenticated roles can view comments by task ID
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByTask(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                commentService.getCommentsByTask(taskId));
    }

    // All authenticated roles can view comments by user ID
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                commentService.getCommentsByUser(userId));
    }

    // Only ADMIN and PROJECT_MANAGER can delete comments
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}
