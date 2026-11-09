
package com.taskflow.service;

import com.taskflow.dto.response.NotificationResponseDTO;
import com.taskflow.entity.Notification;
import com.taskflow.entity.NotificationType;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.NotificationRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Convert Entity into Response DTO
    private NotificationResponseDTO mapToResponse(Notification notification) {

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                notification.getType().name(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getRecipient().getId()
        );
    }

    // Create a notification
    @Transactional
    public NotificationResponseDTO createNotification(
            Long userId,
            String message,
            NotificationType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId));

        Notification notification = new Notification();

        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipient(user);

        Notification savedNotification =
                notificationRepository.save(notification);

        return mapToResponse(savedNotification);
    }

    // Get all notifications of a user
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with ID: " + userId);
        }

        return notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get unread notifications
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnreadNotifications(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with ID: " + userId);
        }

        return notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get unread notification count
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with ID: " + userId);
        }

        return notificationRepository
                .countByRecipient_IdAndIsReadFalse(userId);
    }

    // Mark one notification as read
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with ID: " + notificationId));

        notification.setRead(true);

        Notification updatedNotification =
                notificationRepository.save(notification);

        return mapToResponse(updatedNotification);
    }

    // Mark all notifications as read
    @Transactional
    public void markAllAsRead(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with ID: " + userId);
        }

        List<Notification> notifications =
                notificationRepository
                        .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(userId);

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    // Delete notification
    @Transactional
    public void deleteNotification(Long notificationId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with ID: " + notificationId));

        notificationRepository.delete(notification);
    }
}
