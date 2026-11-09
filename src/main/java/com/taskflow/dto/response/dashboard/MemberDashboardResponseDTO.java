
package com.taskflow.dto.response.dashboard;

import com.taskflow.dto.response.TaskResponseDTO;
import java.util.List;

public class MemberDashboardResponseDTO {

    private long totalAssignedTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long overdueTasks;

    private double completionPercentage;

    private List<TaskResponseDTO> upcomingTasks;

    public MemberDashboardResponseDTO() {
    }

    public MemberDashboardResponseDTO(
            long totalAssignedTasks,
            long completedTasks,
            long pendingTasks,
            long inProgressTasks,
            long overdueTasks,
            double completionPercentage,
            List<TaskResponseDTO> upcomingTasks) {

        this.totalAssignedTasks = totalAssignedTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.overdueTasks = overdueTasks;
        this.completionPercentage = completionPercentage;
        this.upcomingTasks = upcomingTasks;
    }

    public long getTotalAssignedTasks() {
        return totalAssignedTasks;
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

    public List<TaskResponseDTO> getUpcomingTasks() {
        return upcomingTasks;
    }
}
