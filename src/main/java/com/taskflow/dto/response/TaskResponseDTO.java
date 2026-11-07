package com.taskflow.dto.response;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;

    private Long projectId;
    private String projectName;

    private Long assignedToUserId;
    private String assignedToUserName;

    private LocalDateTime createdAt;
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