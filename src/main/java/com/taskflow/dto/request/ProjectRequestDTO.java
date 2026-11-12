
package com.taskflow.dto.request;

import com.taskflow.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(
        description = "Request DTO used to create a new project in TaskFlow."
)
public class ProjectRequestDTO {

    @Schema(
            description = "Name of the project",
            example = "TaskFlow Development",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100,
            message = "Project name must be between 3 and 100 characters")
    private String name;

    @Schema(
            description = "Detailed description of the project",
            example = "A project and task management system built using Spring Boot.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(
            description = "Project start date in YYYY-MM-DD format",
            example = "2026-11-12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Schema(
            description = "Project end date in YYYY-MM-DD format",
            example = "2027-02-12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Schema(
            description = "Initial status of the project",
            example = "PLANNING",
            allowableValues = {"PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD"}
    )
    private ProjectStatus status;

    public ProjectRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
