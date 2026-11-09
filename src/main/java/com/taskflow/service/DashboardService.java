
package com.taskflow.service;

import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.dto.response.dashboard.*;
import com.taskflow.entity.*;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskService taskService;

    public DashboardService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskService taskService) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskService = taskService;
    }

    // ================= ADMIN DASHBOARD =================

    @Transactional(readOnly = true)
    public AdminDashboardResponseDTO getAdminDashboard() {

        long totalUsers = userRepository.count();

        long totalProjects = projectRepository.count();

        long totalTasks = taskRepository.count();

        long completedTasks =
                taskRepository.countByStatus(TaskStatus.COMPLETED);

        long pendingTasks =
                taskRepository.countByStatus(TaskStatus.TODO);

        long inProgressTasks =
                taskRepository.countByStatus(TaskStatus.IN_PROGRESS);

        long highPriorityTasks =
                taskRepository.countByPriority(TaskPriority.HIGH);

        return new AdminDashboardResponseDTO(
                totalUsers,
                totalProjects,
                totalTasks,
                completedTasks,
                pendingTasks,
                inProgressTasks,
                highPriorityTasks
        );
    }


    // ================= PROJECT MANAGER DASHBOARD =================

    @Transactional(readOnly = true)
    public ManagerDashboardResponseDTO getManagerDashboard(Long managerId) {

        long totalProjects =
                projectRepository.countByCreatedBy_Id(managerId);

        long totalTasks =
                taskRepository.countByProject_CreatedBy_Id(managerId);

        long completedTasks =
                taskRepository.countByProject_CreatedBy_IdAndStatus(
                        managerId, TaskStatus.COMPLETED);

        long pendingTasks =
                taskRepository.countByProject_CreatedBy_IdAndStatus(
                        managerId, TaskStatus.TODO);

        long inProgressTasks =
                taskRepository.countByProject_CreatedBy_IdAndStatus(
                        managerId, TaskStatus.IN_PROGRESS);

        long overdueTasks =
                taskRepository.countByProject_CreatedBy_IdAndDueDateBeforeAndStatusNot(
                        managerId,
                        LocalDate.now(),
                        TaskStatus.COMPLETED);

        long highPriorityTasks =
                taskRepository.countByProject_CreatedBy_IdAndPriority(
                        managerId, TaskPriority.HIGH);

        // Project-wise statistics
        List<ProjectStatisticsDTO> projectStatistics =
                getProjectStatistics(managerId);

        // Team member performance
        List<MemberPerformanceDTO> memberPerformance =
                getMemberPerformance(managerId);

        return new ManagerDashboardResponseDTO(
                totalProjects,
                totalTasks,
                completedTasks,
                pendingTasks,
                inProgressTasks,
                overdueTasks,
                highPriorityTasks,
                projectStatistics,
                memberPerformance
        );
    }


    // ================= PROJECT-WISE STATISTICS =================

    private List<ProjectStatisticsDTO> getProjectStatistics(Long managerId) {

        List<Project> projects =
                projectRepository.findByCreatedBy_Id(managerId);

        List<ProjectStatisticsDTO> statistics = new ArrayList<>();

        for (Project project : projects) {

            List<Task> tasks =
                    taskRepository.findByProject_Id(project.getId());

            long totalTasks = tasks.size();

            long completedTasks = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                    .count();

            long pendingTasks = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.TODO)
                    .count();

            long inProgressTasks = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();

            long overdueTasks = tasks.stream()
                    .filter(task ->
                            task.getDueDate() != null
                                    && task.getDueDate().isBefore(LocalDate.now())
                                    && task.getStatus() != TaskStatus.COMPLETED)
                    .count();

            double completionPercentage = totalTasks == 0
                    ? 0.0
                    : (completedTasks * 100.0) / totalTasks;

            String projectHealth = overdueTasks > 0
                    ? "NEEDS_ATTENTION"
                    : "ON_TRACK";

            ProjectStatisticsDTO dto = new ProjectStatisticsDTO(
                    project.getId(),
                    project.getName(),
                    totalTasks,
                    completedTasks,
                    pendingTasks,
                    inProgressTasks,
                    overdueTasks,
                    completionPercentage,
                    projectHealth
            );

            statistics.add(dto);
        }

        return statistics;
    }


    // ================= TEAM MEMBER PERFORMANCE =================

    private List<MemberPerformanceDTO> getMemberPerformance(Long managerId) {

        List<Task> tasks =
                taskRepository.findByProject_CreatedBy_Id(managerId);

        Map<Long, List<Task>> tasksByMember = tasks.stream()
                .filter(task -> task.getAssignedTo() != null)
                .collect(Collectors.groupingBy(
                        task -> task.getAssignedTo().getId()
                ));

        List<MemberPerformanceDTO> performanceList = new ArrayList<>();

        for (Map.Entry<Long, List<Task>> entry : tasksByMember.entrySet()) {

            Long userId = entry.getKey();

            List<Task> memberTasks = entry.getValue();

            User user = memberTasks.get(0).getAssignedTo();

            long assignedTasks = memberTasks.size();

            long completedTasks = memberTasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                    .count();

            long pendingTasks = memberTasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.TODO)
                    .count();

            long inProgressTasks = memberTasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();

            long overdueTasks = memberTasks.stream()
                    .filter(task ->
                            task.getDueDate() != null
                                    && task.getDueDate().isBefore(LocalDate.now())
                                    && task.getStatus() != TaskStatus.COMPLETED)
                    .count();

            double completionPercentage = assignedTasks == 0
                    ? 0.0
                    : (completedTasks * 100.0) / assignedTasks;

            MemberPerformanceDTO dto = new MemberPerformanceDTO(
                    userId,
                    user.getName(),
                    assignedTasks,
                    completedTasks,
                    pendingTasks,
                    inProgressTasks,
                    overdueTasks,
                    completionPercentage
            );

            performanceList.add(dto);
        }

        return performanceList;
    }


    // ================= MEMBER DASHBOARD =================

    @Transactional(readOnly = true)
    public MemberDashboardResponseDTO getMemberDashboard(Long userId) {

        long totalAssignedTasks =
                taskRepository.countByAssignedTo_Id(userId);

        long completedTasks =
                taskRepository.countByAssignedTo_IdAndStatus(
                        userId, TaskStatus.COMPLETED);

        long pendingTasks =
                taskRepository.countByAssignedTo_IdAndStatus(
                        userId, TaskStatus.TODO);

        long inProgressTasks =
                taskRepository.countByAssignedTo_IdAndStatus(
                        userId, TaskStatus.IN_PROGRESS);

        long overdueTasks =
                taskRepository.countByAssignedTo_IdAndDueDateBeforeAndStatusNot(
                        userId,
                        LocalDate.now(),
                        TaskStatus.COMPLETED);

        double completionPercentage = totalAssignedTasks == 0
                ? 0.0
                : (completedTasks * 100.0) / totalAssignedTasks;

        // Get upcoming incomplete tasks
        List<Task> upcomingTasks =
                taskRepository
                        .findByAssignedTo_IdAndDueDateAfterAndStatusNotOrderByDueDateAsc(
                                userId,
                                LocalDate.now().minusDays(1),
                                TaskStatus.COMPLETED
                        );

        // Convert Task entities into TaskResponseDTO
        List<TaskResponseDTO> upcomingTaskDTOs = upcomingTasks.stream()
                .limit(5)
                .map(taskService::mapToResponse)
                .toList();

        return new MemberDashboardResponseDTO(
                totalAssignedTasks,
                completedTasks,
                pendingTasks,
                inProgressTasks,
                overdueTasks,
                completionPercentage,
                upcomingTaskDTOs
        );
    }
}
