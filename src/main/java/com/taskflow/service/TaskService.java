
package com.taskflow.service;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;

import com.taskflow.entity.*;
import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuthenticatedUserService authenticatedUserService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AuthenticatedUserService authenticatedUserService) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // Convert Task entity into TaskResponseDTO
    TaskResponseDTO mapToResponse(Task task) {

        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignedTo().getId(),
                task.getAssignedTo().getName(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    // Get current user's role
    private String getCurrentRole() {
        return authenticatedUserService.getCurrentUserRole();
    }

    // Get current user's ID
    private Long getCurrentUserId() {
        return authenticatedUserService.getCurrentUserId();
    }

    // Check whether the current user can manage a project's tasks
    private void checkProjectManagementAccess(Project project) {

        String role = getCurrentRole();

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("PROJECT_MANAGER".equals(role)
                && project.getCreatedBy().getId().equals(getCurrentUserId())) {
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to manage tasks in this project");
    }

    // Check whether the current user can view a project's tasks
    private void checkProjectViewAccess(Project project) {

        String role = getCurrentRole();

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("PROJECT_MANAGER".equals(role)
                && project.getCreatedBy().getId().equals(getCurrentUserId())) {
            return;
        }

        if ("MEMBER".equals(role)
                && projectMemberRepository.existsByProject_IdAndUser_Id(
                project.getId(), getCurrentUserId())) {
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to view tasks in this project");
    }

    // Create a new task
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + request.getProjectId()));

        checkProjectManagementAccess(project);

        User user = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.getAssignedToUserId()));

        // The assigned user must belong to the project
        if (!projectMemberRepository.existsByProject_IdAndUser_Id(
                project.getId(), user.getId())) {
            throw new IllegalArgumentException(
                    "Assigned user must be a member of the project");
        }

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignedTo(user);

        Task savedTask = taskRepository.save(task);

        notificationService.createNotification(
                savedTask.getAssignedTo().getId(),
                "You have been assigned a new task: " + savedTask.getTitle(),
                NotificationType.TASK_ASSIGNED
        );

        return mapToResponse(savedTask);
    }

    // Get all tasks (ADMIN only)
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {

        if (!"ADMIN".equals(getCurrentRole())) {
            throw new AccessDeniedException(
                    "Only administrators can view all tasks");
        }

        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get task by ID
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id));

        checkProjectViewAccess(task.getProject());

        return mapToResponse(task);
    }

    // Get tasks by project ID
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + projectId));

        checkProjectViewAccess(project);

        return taskRepository.findByProject_Id(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get tasks assigned to a user
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUser(Long userId) {

        String role = getCurrentRole();
        Long currentUserId = getCurrentUserId();

        if ("MEMBER".equals(role) && !currentUserId.equals(userId)) {
            throw new AccessDeniedException(
                    "You can only view your own assigned tasks");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId);
        }

        List<Task> tasks = taskRepository.findByAssignedTo_Id(userId);

        if ("PROJECT_MANAGER".equals(role)) {
            tasks = tasks.stream()
                    .filter(task -> task.getProject().getCreatedBy()
                            .getId().equals(currentUserId))
                    .toList();
        }

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update a task
    @Transactional
    public TaskResponseDTO updateTask(
            Long id, TaskUpdateRequestDTO request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id));

        String role = getCurrentRole();

        if ("MEMBER".equals(role)) {

            if (!task.getAssignedTo().getId().equals(getCurrentUserId())) {
                throw new AccessDeniedException(
                        "You can only update tasks assigned to you");
            }

            // Members can change only task status
            task.setStatus(request.getStatus());

        } else {

            checkProjectManagementAccess(task.getProject());

            TaskStatus previousStatus = task.getStatus();

            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setStatus(request.getStatus());
            task.setPriority(request.getPriority());
            task.setDueDate(request.getDueDate());

            if (previousStatus != task.getStatus()) {

                notificationService.createNotification(
                        task.getAssignedTo().getId(),
                        "Your task " + task.getTitle()
                                + " status has changed to "
                                + task.getStatus(),
                        NotificationType.TASK_STATUS_CHANGED
                );
            }
        }

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    // Delete a task
    @Transactional
    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id));

        checkProjectManagementAccess(task.getProject());

        taskRepository.delete(task);
    }

    // Create pagination request
    private Pageable createPageable(int page, int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException(
                    "Page must be 0 or greater and size must be greater than 0");
        }

        return PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );
    }

    // Get all tasks with pagination (ADMIN only)
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getAllTasksWithPagination(
            int page, int size) {

        if (!"ADMIN".equals(getCurrentRole())) {
            throw new AccessDeniedException(
                    "Only administrators can view all tasks");
        }

        return taskRepository.findAll(createPageable(page, size))
                .map(this::mapToResponse);
    }

    // Get tasks by status
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByStatus(
            TaskStatus status, int page, int size) {

        String role = getCurrentRole();
        Pageable pageable = createPageable(page, size);

        if ("ADMIN".equals(role)) {
            return taskRepository.findByStatus(status, pageable)
                    .map(this::mapToResponse);
        }

        if ("PROJECT_MANAGER".equals(role)) {
            return taskRepository
                    .findByProject_CreatedBy_IdAndStatus(
                            getCurrentUserId(), status, pageable)
                    .map(this::mapToResponse);
        }

        throw new AccessDeniedException(
                "You are not allowed to search all tasks");
    }

    // Get tasks by priority
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByPriority(
            TaskPriority priority, int page, int size) {

        String role = getCurrentRole();
        Pageable pageable = createPageable(page, size);

        if ("ADMIN".equals(role)) {
            return taskRepository.findByPriority(priority, pageable)
                    .map(this::mapToResponse);
        }

        if ("PROJECT_MANAGER".equals(role)) {
            return taskRepository
                    .findByProject_CreatedBy_IdAndPriority(
                            getCurrentUserId(), priority, pageable)
                    .map(this::mapToResponse);
        }

        throw new AccessDeniedException(
                "You are not allowed to search all tasks");
    }

    // Search tasks by title
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> searchTasksByTitle(
            String title, int page, int size) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Search title cannot be empty");
        }

        String role = getCurrentRole();
        Pageable pageable = createPageable(page, size);

        if ("ADMIN".equals(role)) {
            return taskRepository
                    .findByTitleContainingIgnoreCase(title.trim(), pageable)
                    .map(this::mapToResponse);
        }

        if ("PROJECT_MANAGER".equals(role)) {
            return taskRepository
                    .findByProject_CreatedBy_IdAndTitleContainingIgnoreCase(
                            getCurrentUserId(), title.trim(), pageable)
                    .map(this::mapToResponse);
        }

        throw new AccessDeniedException(
                "You are not allowed to search all tasks");
    }

    // Search and filter tasks
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> searchAndFilterTasks(
            String title,
            TaskStatus status,
            TaskPriority priority,
            int page,
            int size) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Search title cannot be empty");
        }

        String role = getCurrentRole();
        Pageable pageable = createPageable(page, size);

        if ("ADMIN".equals(role)) {
            return taskRepository
                    .findByTitleContainingIgnoreCaseAndStatusAndPriority(
                            title.trim(), status, priority, pageable)
                    .map(this::mapToResponse);
        }

        if ("PROJECT_MANAGER".equals(role)) {
            return taskRepository
                    .findByProject_CreatedBy_IdAndTitleContainingIgnoreCaseAndStatusAndPriority(
                            getCurrentUserId(),
                            title.trim(),
                            status,
                            priority,
                            pageable)
                    .map(this::mapToResponse);
        }

        throw new AccessDeniedException(
                "You are not allowed to search all tasks");
    }
}
