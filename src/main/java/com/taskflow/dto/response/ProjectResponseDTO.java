package com.taskflow.dto.response;

import com.taskflow.entity.ProjectStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponseDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponseDTO() {
    }

    public ProjectResponseDTO(
            Long id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}