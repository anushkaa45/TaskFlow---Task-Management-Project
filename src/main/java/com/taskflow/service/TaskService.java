package com.taskflow.service;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;

import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;

import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // Convert Task entity into TaskResponseDTO
    private TaskResponseDTO mapToResponse(Task task) {

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

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

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

        taskRepository.delete(task);
    }
}