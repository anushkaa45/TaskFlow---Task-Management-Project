
package com.taskflow.dto.response.dashboard;

import com.taskflow.dto.response.TaskResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = "Response DTO containing personal task statistics and upcoming tasks for the Member dashboard."
)
public class MemberDashboardResponseDTO {

    @Schema(description = "Total number of tasks assigned to the member", example = "15")
    private long totalAssignedTasks;

    @Schema(description = "Number of completed tasks", example = "8")
    private long completedTasks;

    @Schema(description = "Number of pending tasks", example = "3")
    private long pendingTasks;

    @Schema(description = "Number of tasks currently in progress", example = "2")
    private long inProgressTasks;

    @Schema(description = "Number of overdue tasks", example = "2")
    private long overdueTasks;

    @Schema(
            description = "Percentage of assigned tasks completed by the member",
            example = "53.33"
    )
    private double completionPercentage;

    @Schema(
            description = "List of upcoming tasks assigned to the member, ordered by due date",
            implementation = TaskResponseDTO.class
    )
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
