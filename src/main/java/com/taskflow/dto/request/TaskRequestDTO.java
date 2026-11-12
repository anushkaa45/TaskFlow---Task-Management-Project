
package com.taskflow.dto.request;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(
        description = "Request DTO used to create a new task in a project."
)
public class TaskRequestDTO {

    @Schema(
            description = "Title of the task",
            example = "Implement user authentication",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @Schema(
            description = "Detailed description of the task",
            example = "Implement JWT-based login and registration APIs.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Task description is required")
    private String description;

    @Schema(
            description = "Initial status of the task",
            example = "TODO",
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED"}
    )
    private TaskStatus status;

    @Schema(
            description = "Priority level of the task",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"}
    )
    private TaskPriority priority;

    @Schema(
            description = "Task due date in YYYY-MM-DD format. Cannot be in the past.",
            example = "2026-12-15",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @Schema(
            description = "ID of the project to which this task belongs",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @Schema(
            description = "ID of the user to whom this task is assigned",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Assigned user ID is required")
    private Long assignedToUserId;

    public TaskRequestDTO() {
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}
