
package com.taskflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Response DTO containing notification details returned by TaskFlow APIs."
)
public class NotificationResponseDTO {

    @Schema(description = "Unique identifier of the notification", example = "1")
    private Long id;

    @Schema(
            description = "Notification message displayed to the recipient",
            example = "A new task has been assigned to you."
    )
    private String message;

    @Schema(
            description = "Type of notification",
            example = "TASK_ASSIGNED",
            allowableValues = {
                    "TASK_ASSIGNED",
                    "TASK_STATUS_CHANGED",
                    "NEW_COMMENT",
                    "PROJECT_MEMBER_ADDED"
            }
    )
    private String type;

    @Schema(description = "Indicates whether the notification has been read", example = "false")
    private boolean isRead;

    @Schema(
            description = "Date and time when the notification was created",
            example = "2026-11-12T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(description = "ID of the user receiving the notification", example = "3")
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
