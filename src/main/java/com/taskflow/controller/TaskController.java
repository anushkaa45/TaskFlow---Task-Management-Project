
package com.taskflow.controller;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create a new task
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO request) {

        TaskResponseDTO response = taskService.createTask(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get all tasks
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // Get task by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Get tasks by project ID
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                taskService.getTasksByProject(projectId));
    }

    // Get tasks assigned to a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                taskService.getTasksByUser(userId));
    }

    // Update task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequestDTO request) {

        return ResponseEntity.ok(
                taskService.updateTask(id, request));
    }

    // Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }
}
