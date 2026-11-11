
package com.taskflow.controller;

import com.taskflow.dto.response.NotificationResponseDTO;
import com.taskflow.service.AuthenticatedUserService;
import com.taskflow.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserService authenticatedUserService;

    public NotificationController(
            NotificationService notificationService,
            AuthenticatedUserService authenticatedUserService) {

        this.notificationService = notificationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // Get logged-in user's notifications
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    // Get logged-in user's unread notifications
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getMyUnreadNotifications() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    // Get logged-in user's unread notification count
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/unread/count")
    public ResponseEntity<Long> getMyUnreadNotificationCount() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationCount(userId)
        );
    }

    // Mark one notification as read
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId)
        );
    }

    // Mark all logged-in user's notifications as read
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead() {

        Long userId = authenticatedUserService.getCurrentUserId();

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    // Delete notification
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}
