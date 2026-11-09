
package com.taskflow.repository;

import com.taskflow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications of a particular user
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long userId);

    // Get only unread notifications of a particular user
    List<Notification> findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Count unread notifications of a particular user
    long countByRecipient_IdAndIsReadFalse(Long userId);
}
