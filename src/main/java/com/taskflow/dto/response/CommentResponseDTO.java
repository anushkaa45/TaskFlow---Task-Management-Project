
package com.taskflow.dto.response;

import java.time.LocalDateTime;

public class CommentResponseDTO {

    private Long id;
    private String content;

    private Long taskId;

    private Long userId;
    private String userName;

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
