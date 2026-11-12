
package com.taskflow.controller;

import com.taskflow.dto.request.CommentRequestDTO;
import com.taskflow.dto.response.CommentResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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

    // Reusable Swagger error examples

    private static final String EXAMPLE_400_VALIDATION = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Validation Failed",
              "fieldErrors": {
                "content": "Comment content is required"
              }
            }
            """;

    private static final String EXAMPLE_400_BAD_REQUEST = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Invalid request body. Check your JSON values, dates, and enum fields."
            }
            """;

    private static final String EXAMPLE_401 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 401,
              "error": "Unauthorized",
              "message": "Authentication is required. Please provide a valid JWT token."
            }
            """;

    private static final String EXAMPLE_403 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 403,
              "error": "Forbidden",
              "message": "You do not have permission to access this resource."
            }
            """;

    private static final String EXAMPLE_404_TASK = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Task not found with id: 10"
            }
            """;

    private static final String EXAMPLE_404_USER = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "User not found with id: 5"
            }
            """;

    private static final String EXAMPLE_404_COMMENT = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Comment not found with id: 10"
            }
            """;

    private static final String EXAMPLE_500 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 500,
              "error": "Internal Server Error",
              "message": "Something went wrong. Please try again later."
            }
            """;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Add a comment
    @Operation(
            summary = "Add a comment",
            description = "Adds a comment to a task using the authenticated user's identity."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid comment data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Failed",
                                            value = EXAMPLE_400_VALIDATION
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Request Body",
                                            value = EXAMPLE_400_BAD_REQUEST
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_TASK)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @Valid @RequestBody CommentRequestDTO request) {

        CommentResponseDTO response =
                commentService.addComment(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get comments by task ID
    @Operation(
            summary = "Get comments by task",
            description = "Retrieves all comments associated with a specific task."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_TASK)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByTask(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                commentService.getCommentsByTask(taskId)
        );
    }

    // Get comments by user ID
    @Operation(
            summary = "Get comments by user",
            description = "Retrieves comments written by a specific user, subject to access permissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_USER)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                commentService.getCommentsByUser(userId)
        );
    }

    // Delete a comment
    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment according to the authenticated user's permissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Comment deleted successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_COMMENT)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}
