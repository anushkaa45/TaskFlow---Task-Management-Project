
package com.taskflow.controller;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
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


    @GetMapping("/page")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        return ResponseEntity.ok(
                taskService.getAllTasksWithPagination(page, size));
    }


    @GetMapping("/filter/status")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByStatus(
            @RequestParam TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        return ResponseEntity.ok(
                taskService.getTasksByStatus(status, page, size));
    }


    @GetMapping("/filter/priority")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByPriority(
            @RequestParam TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        return ResponseEntity.ok(
                taskService.getTasksByPriority(priority, page, size));
    }


    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        return ResponseEntity.ok(
                taskService.searchTasksByTitle(title, page, size));
    }


    @GetMapping("/search/filter")
    public ResponseEntity<Page<TaskResponseDTO>> searchAndFilterTasks(

            @RequestParam String title,
            @RequestParam TaskStatus status,
            @RequestParam TaskPriority priority,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100");
        }

        return ResponseEntity.ok(
                taskService.searchAndFilterTasks(
                        title, status, priority, page, size
                )
        );
    }

}
