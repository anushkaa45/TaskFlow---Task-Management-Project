
package com.taskflow.dto.response.dashboard;

public class AdminDashboardResponseDTO {

    private long totalUsers;
    private long totalProjects;
    private long totalTasks;

    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;

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
