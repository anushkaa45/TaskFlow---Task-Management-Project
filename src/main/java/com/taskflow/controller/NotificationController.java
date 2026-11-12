
package com.taskflow.controller;

import com.taskflow.dto.response.NotificationResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.service.AuthenticatedUserService;
import com.taskflow.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserService authenticatedUserService;

    // Reusable Swagger error examples

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

    private static final String EXAMPLE_404 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Notification not found with id: 10"
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

    public NotificationController(
            NotificationService notificationService,
            AuthenticatedUserService authenticatedUserService) {

        this.notificationService = notificationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // Get logged-in user's notifications
    @Operation(
            summary = "Get my notifications",
            description = "Retrieves all notifications belonging to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully"
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    // Get logged-in user's unread notifications
    @Operation(
            summary = "Get unread notifications",
            description = "Retrieves all unread notifications belonging to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread notifications retrieved successfully"
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getMyUnreadNotifications() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    // Get logged-in user's unread notification count
    @Operation(
            summary = "Get unread notification count",
            description = "Returns the number of unread notifications for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread count retrieved successfully"
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/unread/count")
    public ResponseEntity<Long> getMyUnreadNotificationCount() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationCount(userId)
        );
    }

    // Mark one notification as read
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a specific notification as read if it belongs to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification marked as read"
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
                    description = "Notification not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404)
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
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId)
        );
    }

    // Mark all logged-in user's notifications as read
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications belonging to the authenticated user as read."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Notifications marked as read"
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
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead() {

        Long userId = authenticatedUserService.getCurrentUserId();

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    // Delete notification
    @Operation(
            summary = "Delete notification",
            description = "Deletes a specific notification belonging to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Notification deleted successfully"
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
                    description = "Notification not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404)
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
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}
