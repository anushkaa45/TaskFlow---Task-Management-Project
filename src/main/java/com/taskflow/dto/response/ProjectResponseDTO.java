
package com.taskflow.dto.response;

import com.taskflow.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(
        description = "Response DTO containing project details returned by TaskFlow APIs."
)
public class ProjectResponseDTO {

    @Schema(description = "Unique identifier of the project", example = "1")
    private Long id;

    @Schema(description = "Name of the project", example = "TaskFlow Development")
    private String name;

    @Schema(
            description = "Description of the project",
            example = "A project and task management system built using Spring Boot."
    )
    private String description;

    @Schema(description = "Project start date", example = "2026-11-12")
    private LocalDate startDate;

    @Schema(description = "Project end date", example = "2027-02-12")
    private LocalDate endDate;

    @Schema(
            description = "Current status of the project",
            example = "IN_PROGRESS",
            allowableValues = {"PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD"}
    )
    private ProjectStatus status;

    @Schema(
            description = "Date and time when the project was created",
            example = "2026-11-12T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the project was last updated",
            example = "2026-11-13T14:45:00"
    )
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
