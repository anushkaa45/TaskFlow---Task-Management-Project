
package com.taskflow.dto.response.dashboard;

public class MemberPerformanceDTO {

    private Long userId;
    private String userName;

    private long assignedTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long overdueTasks;

    private double completionPercentage;

    public MemberPerformanceDTO() {
    }

    public MemberPerformanceDTO(
            Long userId,
            String userName,
            long assignedTasks,
            long completedTasks,
            long pendingTasks,
            long inProgressTasks,
            long overdueTasks,
            double completionPercentage) {

        this.userId = userId;
        this.userName = userName;
        this.assignedTasks = assignedTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.overdueTasks = overdueTasks;
        this.completionPercentage = completionPercentage;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public long getAssignedTasks() {
        return assignedTasks;
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
}
