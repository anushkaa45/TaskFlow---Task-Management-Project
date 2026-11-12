
package com.taskflow.dto.response.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Response DTO containing overall system statistics for the TaskFlow admin dashboard."
)
public class AdminDashboardResponseDTO {

    @Schema(description = "Total number of registered users", example = "25")
    private long totalUsers;

    @Schema(description = "Total number of projects in the system", example = "12")
    private long totalProjects;

    @Schema(description = "Total number of tasks across all projects", example = "150")
    private long totalTasks;

    @Schema(description = "Number of tasks marked as completed", example = "60")
    private long completedTasks;

    @Schema(description = "Number of tasks waiting to be started", example = "50")
    private long pendingTasks;

    @Schema(description = "Number of tasks currently in progress", example = "40")
    private long inProgressTasks;

    @Schema(description = "Total number of high-priority tasks", example = "20")
    private long highPriorityTasks;

    public AdminDashboardResponseDTO() {
    }

    public AdminDashboardResponseDTO(
            long totalUsers,
            long totalProjects,
            long totalTasks,
            long completedTasks,
            long pendingTasks,
            long inProgressTasks,
            long highPriorityTasks) {

        this.totalUsers = totalUsers;
        this.totalProjects = totalProjects;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.highPriorityTasks = highPriorityTasks;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalProjects() {
        return totalProjects;
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

    public long getHighPriorityTasks() {
        return highPriorityTasks;
    }
}
