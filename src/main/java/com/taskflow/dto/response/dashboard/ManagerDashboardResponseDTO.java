
package com.taskflow.dto.response.dashboard;

import java.util.List;

public class ManagerDashboardResponseDTO {

    private long totalProjects;
    private long totalTasks;

    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long overdueTasks;
    private long highPriorityTasks;

    private List<ProjectStatisticsDTO> projectStatistics;

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
