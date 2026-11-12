
package com.taskflow.dto.response.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Response DTO containing task assignment and performance statistics for an individual team member."
)
public class MemberPerformanceDTO {

    @Schema(description = "Unique identifier of the team member", example = "3")
    private Long userId;

    @Schema(description = "Name of the team member", example = "Rahul Sharma")
    private String userName;

    @Schema(description = "Total number of tasks assigned to the member", example = "15")
    private long assignedTasks;

    @Schema(description = "Number of tasks completed by the member", example = "8")
    private long completedTasks;

    @Schema(description = "Number of pending tasks assigned to the member", example = "3")
    private long pendingTasks;

    @Schema(description = "Number of tasks currently in progress", example = "2")
    private long inProgressTasks;

    @Schema(description = "Number of overdue tasks assigned to the member", example = "2")
    private long overdueTasks;

    @Schema(
            description = "Percentage of assigned tasks completed by the member",
            example = "53.33"
    )
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
