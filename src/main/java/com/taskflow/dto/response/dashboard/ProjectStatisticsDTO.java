
package com.taskflow.dto.response.dashboard;

public class ProjectStatisticsDTO {

    private Long projectId;
    private String projectName;

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long overdueTasks;

    private double completionPercentage;

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
