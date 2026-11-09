package com.taskflow.service;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;

import com.taskflow.entity.*;

import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.taskflow.entity.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskflow.entity.NotificationType;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,NotificationService notificationService) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

    // Create a new task
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + request.getProjectId()));

        User user = userRepository.findById(request.getAssignedToUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.getAssignedToUserId()));

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

    // Get all tasks
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {

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

        return mapToResponse(task);
    }

    // Get tasks by project ID
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByProject(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + projectId));

        return taskRepository.findByProject_Id(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get tasks assigned to a user
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUser(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId));

        return taskRepository.findByAssignedTo_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // Update a task
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateRequestDTO request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id));

        TaskStatus previousStatus = task.getStatus();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);

        if (previousStatus != updatedTask.getStatus()) {

            notificationService.createNotification(
                    updatedTask.getAssignedTo().getId(),
                    "Your task " + updatedTask.getTitle()
                            + " status has changed to "
                            + updatedTask.getStatus(),
                    NotificationType.TASK_STATUS_CHANGED
            );
        }

        return mapToResponse(updatedTask);
    }

    // Delete a task
    @Transactional
    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id));

        taskRepository.delete(task);
    }


    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getAllTasksWithPagination(
            int page,
            int size) {

        // Create pagination request
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        // Fetch paginated tasks
        Page<Task> tasks = taskRepository.findAll(pageable);

        // Convert Task entities into DTOs
        return tasks.map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByStatus(
            TaskStatus status,
            int page,
            int size) {

        // Create pagination request
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        // Find tasks by status
        Page<Task> tasks =
                taskRepository.findByStatus(status, pageable);

        // Convert entities into response DTOs
        return tasks.map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByPriority(
            TaskPriority priority,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        Page<Task> tasks =
                taskRepository.findByPriority(priority, pageable);

        return tasks.map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> searchTasksByTitle(
            String title,
            int page,
            int size) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Search title cannot be empty");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        Page<Task> tasks =
                taskRepository.findByTitleContainingIgnoreCase(
                        title.trim(), pageable);

        return tasks.map(this::mapToResponse);
    }


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

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        Page<Task> tasks =
                taskRepository
                        .findByTitleContainingIgnoreCaseAndStatusAndPriority(
                                title.trim(),
                                status,
                                priority,
                                pageable
                        );

        return tasks.map(this::mapToResponse);
    }

}