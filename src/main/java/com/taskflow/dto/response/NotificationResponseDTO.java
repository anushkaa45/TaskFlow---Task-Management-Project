
package com.taskflow.dto.response;

import java.time.LocalDateTime;

public class NotificationResponseDTO {

    private Long id;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;

    private Long recipientId;

    public NotificationResponseDTO() {
    }

    public NotificationResponseDTO(
            Long id,
            String message,
            String type,
            boolean isRead,
            LocalDateTime createdAt,
            Long recipientId) {

        this.id = id;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.recipientId = recipientId;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getRecipientId() {
        return recipientId;
    }
}
