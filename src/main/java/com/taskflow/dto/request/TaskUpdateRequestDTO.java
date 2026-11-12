
package com.taskflow.dto.request;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(
        description = "Request DTO used to update an existing task."
)
public class TaskUpdateRequestDTO {

    @Schema(
            description = "Updated title of the task",
            example = "Implement JWT authentication",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @Schema(
            description = "Updated description of the task",
            example = "Complete JWT login, token generation and validation.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Task description is required")
    private String description;

    @Schema(
            description = "Updated status of the task",
            example = "IN_PROGRESS",
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Task status is required")
    private TaskStatus status;

    @Schema(
            description = "Updated priority of the task",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Task priority is required")
    private TaskPriority priority;

    @Schema(
            description = "Updated due date in YYYY-MM-DD format. Cannot be in the past.",
            example = "2026-12-20",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    public TaskUpdateRequestDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
