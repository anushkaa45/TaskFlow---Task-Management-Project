
package com.taskflow.dto.response.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = "Response DTO containing project, task and team performance statistics for the Project Manager dashboard."
)
public class ManagerDashboardResponseDTO {

    @Schema(description = "Total number of projects managed by the current manager", example = "5")
    private long totalProjects;

    @Schema(description = "Total number of tasks across the manager's projects", example = "75")
    private long totalTasks;

    @Schema(description = "Number of completed tasks", example = "30")
    private long completedTasks;

    @Schema(description = "Number of pending tasks", example = "20")
    private long pendingTasks;

    @Schema(description = "Number of tasks currently in progress", example = "15")
    private long inProgressTasks;

    @Schema(description = "Number of overdue tasks", example = "7")
    private long overdueTasks;

    @Schema(description = "Number of high-priority tasks", example = "12")
    private long highPriorityTasks;

    @Schema(
            description = "Statistics for each project managed by the current manager",
            implementation = ProjectStatisticsDTO.class
    )
    private List<ProjectStatisticsDTO> projectStatistics;

    @Schema(
            description = "Performance statistics for members working on the manager's projects",
            implementation = MemberPerformanceDTO.class
    )
    private List<MemberPerformanceDTO> memberPerformance;

    public ManagerDashboardResponseDTO() {
    }

    public ManagerDashboardResponseDTO(
            long totalProjects,
            long totalTasks,
            long completedTasks,
            long pendingTasks,
            long inProgressTasks,
            long overdueTasks,
            long highPriorityTasks,
            List<ProjectStatisticsDTO> projectStatistics,
            List<MemberPerformanceDTO> memberPerformance) {

        this.totalProjects = totalProjects;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.overdueTasks = overdueTasks;
        this.highPriorityTasks = highPriorityTasks;
        this.projectStatistics = projectStatistics;
        this.memberPerformance = memberPerformance;
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

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public long getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public List<ProjectStatisticsDTO> getProjectStatistics() {
        return projectStatistics;
    }

    public List<MemberPerformanceDTO> getMemberPerformance() {
        return memberPerformance;
    }
}
