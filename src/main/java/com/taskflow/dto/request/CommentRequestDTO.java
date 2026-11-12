
package com.taskflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Request DTO used to add a comment to an existing task."
)
public class CommentRequestDTO {

    @Schema(
            description = "Content of the comment. Maximum 2000 characters.",
            example = "The authentication API implementation is completed.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String content;

    @Schema(
            description = "ID of the task to which the comment belongs",
            example = "6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Task ID is required")
    private Long taskId;

    public CommentRequestDTO() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
