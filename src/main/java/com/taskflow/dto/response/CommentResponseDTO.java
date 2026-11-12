
package com.taskflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Response DTO containing comment details returned by TaskFlow APIs."
)
public class CommentResponseDTO {

    @Schema(description = "Unique identifier of the comment", example = "1")
    private Long id;

    @Schema(
            description = "Content of the comment",
            example = "The authentication API implementation is completed."
    )
    private String content;

    @Schema(description = "ID of the task associated with this comment", example = "6")
    private Long taskId;

    @Schema(description = "ID of the user who posted the comment", example = "3")
    private Long userId;

    @Schema(description = "Name of the user who posted the comment", example = "Rahul Sharma")
    private String userName;

    @Schema(
            description = "Date and time when the comment was created",
            example = "2026-11-12T10:30:00"
    )
    private LocalDateTime createdAt;

    public CommentResponseDTO() {}

    public CommentResponseDTO(
            Long id,
            String content,
            Long taskId,
            Long userId,
            String userName,
            LocalDateTime createdAt) {

        this.id = id;
        this.content = content;
        this.taskId = taskId;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
