
package com.taskflow.controller;

import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;
import com.taskflow.service.CommentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Add a comment
    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @Valid @RequestBody CommentRequestDTO request) {

        CommentResponseDTO response =
                commentService.addComment(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get comments by task ID
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByTask(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                commentService.getCommentsByTask(taskId));
    }

    // Get comments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                commentService.getCommentsByUser(userId));
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}
