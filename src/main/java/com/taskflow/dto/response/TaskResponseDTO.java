
package com.taskflow.dto.response;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(
        description = "Response DTO containing task details returned by TaskFlow APIs."
)
public class TaskResponseDTO {

    @Schema(description = "Unique identifier of the task", example = "6")
    private Long id;

    @Schema(description = "Title of the task", example = "Implement JWT authentication")
    private String title;

    @Schema(
            description = "Detailed description of the task",
            example = "Implement login and JWT token validation."
    )
    private String description;

    @Schema(
            description = "Current status of the task",
            example = "IN_PROGRESS",
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED"}
    )
    private TaskStatus status;

    @Schema(
            description = "Priority assigned to the task",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"}
    )
    private TaskPriority priority;

    @Schema(description = "Task due date", example = "2026-12-15")
    private LocalDate dueDate;

    @Schema(description = "ID of the project containing this task", example = "2")
    private Long projectId;

    @Schema(description = "Name of the project containing this task", example = "TaskFlow Development")
    private String projectName;

    @Schema(description = "ID of the user assigned to this task", example = "3")
    private Long assignedToUserId;

    @Schema(description = "Name of the assigned user", example = "Rahul Sharma")
    private String assignedToUserName;

    @Schema(
            description = "Date and time when the task was created",
            example = "2026-11-12T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the task was last updated",
            example = "2026-11-13T14:45:00"
    )
    private LocalDateTime updatedAt;

    public TaskResponseDTO() {
    }

    public TaskResponseDTO(
            Long id,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Long projectId,
            String projectName,
            Long assignedToUserId,
            String assignedToUserName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.projectName = projectName;
        this.assignedToUserId = assignedToUserId;
        this.assignedToUserName = assignedToUserName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public String getAssignedToUserName() {
        return assignedToUserName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
