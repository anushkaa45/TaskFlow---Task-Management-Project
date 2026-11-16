
package com.taskflow.service;

import com.taskflow.dto.response.NotificationResponseDTO;
import com.taskflow.entity.Notification;
import com.taskflow.entity.NotificationType;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.NotificationRepository;
import com.taskflow.repository.UserRepository;

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
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_success() {

        User user = mock(User.class);
        Notification notification = mock(Notification.class);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        when(notification.getId()).thenReturn(1L);
        when(notification.getMessage()).thenReturn("New comment added");
        when(notification.getType()).thenReturn(NotificationType.NEW_COMMENT);
        when(notification.isRead()).thenReturn(false);
        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        when(notification.getCreatedAt())
                .thenReturn(java.time.LocalDateTime.now());

        NotificationResponseDTO response =
                notificationService.createNotification(
                        10L,
                        "New comment added",
                        NotificationType.NEW_COMMENT
                );

        assertNotNull(response);

        verify(userRepository).findById(10L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_userNotFound() {

        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.createNotification(
                        10L,
                        "New comment added",
                        NotificationType.NEW_COMMENT
                )
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void getUserNotifications_success() {

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification));

        when(notification.getId()).thenReturn(1L);
        when(notification.getMessage()).thenReturn("Task assigned");
        when(notification.getType()).thenReturn(NotificationType.NEW_COMMENT);
        when(notification.isRead()).thenReturn(false);
        when(notification.getCreatedAt())
                .thenReturn(java.time.LocalDateTime.now());
        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        List<NotificationResponseDTO> result =
                notificationService.getUserNotifications(10L);

        assertEquals(1, result.size());

        verify(notificationRepository)
                .findByRecipient_IdOrderByCreatedAtDesc(10L);
    }

    @Test
    void getUserNotifications_accessDenied() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.getUserNotifications(20L)
        );

        verify(notificationRepository, never())
                .findByRecipient_IdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getUnreadNotifications_success() {

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification));

        when(notification.getId()).thenReturn(2L);
        when(notification.getMessage()).thenReturn("Task assigned");
        when(notification.getType()).thenReturn(NotificationType.NEW_COMMENT);
        when(notification.isRead()).thenReturn(false);
        when(notification.getCreatedAt())
                .thenReturn(java.time.LocalDateTime.now());
        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        List<NotificationResponseDTO> result =
                notificationService.getUnreadNotifications(10L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());

        verify(notificationRepository)
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(10L);
    }

    @Test
    void getUnreadNotifications_emptyList() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<NotificationResponseDTO> result =
                notificationService.getUnreadNotifications(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUnreadNotificationCount_success() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .countByRecipient_IdAndIsReadFalse(10L))
                .thenReturn(5L);

        long count = notificationService.getUnreadNotificationCount(10L);

        assertEquals(5L, count);

        verify(notificationRepository)
                .countByRecipient_IdAndIsReadFalse(10L);
    }

    @Test
    void getUnreadNotificationCount_zero() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .countByRecipient_IdAndIsReadFalse(10L))
                .thenReturn(0L);

        long count = notificationService.getUnreadNotificationCount(10L);

        assertEquals(0L, count);
    }

    @Test
    void markAsRead_success() {

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        when(notification.getId()).thenReturn(1L);
        when(notification.getMessage()).thenReturn("Task assigned");
        when(notification.getType()).thenReturn(NotificationType.NEW_COMMENT);
        when(notification.isRead()).thenReturn(true);
        when(notification.getCreatedAt())
                .thenReturn(java.time.LocalDateTime.now());

        NotificationResponseDTO response =
                notificationService.markAsRead(1L);

        assertNotNull(response);
        assertTrue(response.isRead());

        verify(notification).setRead(true);
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_notFound() {

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(1L)
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void markAsRead_accessDenied() {

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(20L);

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.markAsRead(1L)
        );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void markAllAsRead_success() {

        Notification notification1 = mock(Notification.class);
        Notification notification2 = mock(Notification.class);

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification1, notification2));

        notificationService.markAllAsRead(10L);

        verify(notification1).setRead(true);
        verify(notification2).setRead(true);

        verify(notificationRepository).saveAll(
                List.of(notification1, notification2)
        );
    }

    @Test
    void markAllAsRead_accessDenied() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.markAllAsRead(20L)
        );

        verify(notificationRepository, never())
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(anyLong());

        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void deleteNotification_success() {

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        notificationService.deleteNotification(1L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_notFound() {

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.deleteNotification(1L)
        );

        verify(notificationRepository, never())
                .delete(any(Notification.class));
    }

    @Test
    void deleteNotification_accessDenied() {

        Notification notification = mock(Notification.class);
        User user = mock(User.class);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notification.getRecipient()).thenReturn(user);
        when(user.getId()).thenReturn(20L);

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.deleteNotification(1L)
        );

        verify(notificationRepository, never())
                .delete(any(Notification.class));
    }

    @Test
    void getUserNotifications_emptyList() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<NotificationResponseDTO> result =
                notificationService.getUserNotifications(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUnreadNotifications_accessDenied() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.getUnreadNotifications(20L)
        );

        verify(notificationRepository, never())
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getUnreadNotificationCount_accessDenied() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.getUnreadNotificationCount(20L)
        );

        verify(notificationRepository, never())
                .countByRecipient_IdAndIsReadFalse(anyLong());
    }

    @Test
    void markAllAsRead_emptyList() {

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of());

        notificationService.markAllAsRead(10L);

        verify(notificationRepository).saveAll(List.of());
    }
}

