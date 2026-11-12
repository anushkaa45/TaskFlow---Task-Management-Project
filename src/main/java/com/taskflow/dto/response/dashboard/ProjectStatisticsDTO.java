
package com.taskflow.dto.response.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Response DTO containing task statistics and health information for an individual project."
)
public class ProjectStatisticsDTO {

    @Schema(description = "Unique identifier of the project", example = "2")
    private Long projectId;

    @Schema(description = "Name of the project", example = "TaskFlow Development")
    private String projectName;

    @Schema(description = "Total number of tasks in the project", example = "20")
    private long totalTasks;

    @Schema(description = "Number of completed tasks", example = "8")
    private long completedTasks;

    @Schema(description = "Number of pending tasks", example = "5")
    private long pendingTasks;

    @Schema(description = "Number of tasks currently in progress", example = "4")
    private long inProgressTasks;

    @Schema(description = "Number of overdue tasks", example = "3")
    private long overdueTasks;

    @Schema(
            description = "Percentage of project tasks that have been completed",
            example = "40.0"
    )
    private double completionPercentage;

    @Schema(
            description = "Overall health indicator of the project",
            example = "ON_TRACK"
    )
    private String projectHealth;

    public ProjectStatisticsDTO() {
    }

    public ProjectStatisticsDTO(
            Long projectId,
            String projectName,
            long totalTasks,
            long completedTasks,
            long pendingTasks,
            long inProgressTasks,
            long overdueTasks,
            double completionPercentage,
            String projectHealth) {

        this.projectId = projectId;
        this.projectName = projectName;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.overdueTasks = overdueTasks;
        this.completionPercentage = completionPercentage;
        this.projectHealth = projectHealth;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public String getProjectHealth() {
        return projectHealth;
    }
}
