
package com.taskflow.repository;

import com.taskflow.entity.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to create a user
    private User createUser() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("notification" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.MEMBER);

        return userRepository.saveAndFlush(user);
    }

    // Helper method to create a notification
    private Notification createNotification(User user) {

        Notification notification = new Notification();

        notification.setMessage("This is a test notification");
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setRecipient(user);

        return notificationRepository.saveAndFlush(notification);
    }

    // Test 1: Save notification successfully
    @Test
    void saveNotification_success() {

        User user = createUser();

        Notification notification = createNotification(user);

        assertNotNull(notification.getId());
        assertEquals(
                "This is a test notification",
                notification.getMessage()
        );
        assertEquals(NotificationType.TASK_ASSIGNED, notification.getType());
        assertFalse(notification.isRead());
        assertNotNull(notification.getCreatedAt());
    }

    // Test 2: Find notification by ID
    @Test
    void findById_success() {

        User user = createUser();

        Notification savedNotification = createNotification(user);

        Optional<Notification> result =
                notificationRepository.findById(savedNotification.getId());

        assertTrue(result.isPresent());
        assertEquals(
                "This is a test notification",
                result.get().getMessage()
        );
    }

    // Test 3: Find all notifications
    @Test
    void findAll_success() {

        User user = createUser();

        createNotification(user);
        createNotification(user);

        List<Notification> notifications =
                notificationRepository.findAll();

        assertEquals(2, notifications.size());
    }

    // Test 4: Delete notification successfully
    @Test
    void deleteNotification_success() {

        User user = createUser();

        Notification savedNotification = createNotification(user);

        Long notificationId = savedNotification.getId();

        notificationRepository.deleteById(notificationId);
        notificationRepository.flush();

        Optional<Notification> result =
                notificationRepository.findById(notificationId);

        assertFalse(result.isPresent());
    }

    // Test 5: Find notifications by recipient ID
    // Ordered by createdAt descending
    @Test
    void findByRecipient_IdOrderByCreatedAtDesc_success() {

        User user = createUser();

        Notification first = createNotification(user);
        Notification second = createNotification(user);

        List<Notification> notifications =
                notificationRepository
                        .findByRecipient_IdOrderByCreatedAtDesc(user.getId());

        assertEquals(2, notifications.size());

        assertTrue(
                !notifications.get(0).getCreatedAt()
                        .isBefore(notifications.get(1).getCreatedAt())
        );
    }

    // Test 6: Find unread notifications by recipient ID
    @Test
    void findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc_success() {

        User user = createUser();

        Notification unread = createNotification(user);

        Notification read = createNotification(user);
        read.setRead(true);

        notificationRepository.saveAndFlush(read);

        List<Notification> notifications =
                notificationRepository
                        .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(
                                user.getId()
                        );

        assertEquals(1, notifications.size());
        assertFalse(notifications.get(0).isRead());
    }

    // Test 7: Count unread notifications by recipient ID
    @Test
    void countByRecipient_IdAndIsReadFalse_success() {

        User user = createUser();

        createNotification(user);
        createNotification(user);

        Notification read = createNotification(user);
        read.setRead(true);

        notificationRepository.saveAndFlush(read);

        long count =
                notificationRepository
                        .countByRecipient_IdAndIsReadFalse(user.getId());

        assertEquals(2, count);
    }
}
