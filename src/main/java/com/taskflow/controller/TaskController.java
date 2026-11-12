
package com.taskflow.controller;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Reusable Swagger error examples

    private static final String EXAMPLE_400_VALIDATION = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Validation Failed",
              "fieldErrors": {
                "title": "Title is required"
              }
            }
            """;

    private static final String EXAMPLE_400_BAD_REQUEST = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Invalid request body. Check your JSON values, dates, and enum fields."
            }
            """;

    private static final String EXAMPLE_400_PAGINATION = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Page must be non-negative and size must be between 1 and 100"
            }
            """;

    private static final String EXAMPLE_400_INVALID_PARAMETER = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Invalid value for parameter: status"
            }
            """;

    private static final String EXAMPLE_401 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 401,
              "error": "Unauthorized",
              "message": "Authentication is required. Please provide a valid JWT token."
            }
            """;

    private static final String EXAMPLE_403 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 403,
              "error": "Forbidden",
              "message": "You do not have permission to access this resource."
            }
            """;

    private static final String EXAMPLE_404_TASK = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Task not found with id: 10"
            }
            """;

    private static final String EXAMPLE_404_PROJECT = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Project not found with id: 10"
            }
            """;

    private static final String EXAMPLE_404_USER = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "User not found with id: 5"
            }
            """;

    private static final String EXAMPLE_500 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 500,
              "error": "Internal Server Error",
              "message": "Something went wrong. Please try again later."
            }
            """;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create a new task
    @Operation(
            summary = "Create a new task",
            description = "Creates a task inside a project and assigns it to a project member."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Failed",
                                            value = EXAMPLE_400_VALIDATION
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Request Body",
                                            value = EXAMPLE_400_BAD_REQUEST
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Project or assigned user not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Project Not Found",
                                            value = EXAMPLE_404_PROJECT
                                    ),
                                    @ExampleObject(
                                            name = "User Not Found",
                                            value = EXAMPLE_404_USER
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO request) {

        TaskResponseDTO response = taskService.createTask(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get all tasks
    @Operation(
            summary = "Get all tasks",
            description = "Retrieves all tasks available to the authenticated admin or project manager."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // Get task by ID
    @Operation(
            summary = "Get task by ID",
            description = "Retrieves details of a specific task using its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_TASK)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Get tasks by project ID
    @Operation(
            summary = "Get tasks by project",
            description = "Retrieves all tasks belonging to a specific project."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project tasks retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_PROJECT)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                taskService.getTasksByProject(projectId)
        );
    }

    // Get tasks assigned to a user
    @Operation(
            summary = "Get tasks assigned to a user",
            description = "Retrieves tasks assigned to the specified user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User tasks retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_USER)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                taskService.getTasksByUser(userId)
        );
    }

    // Update task
    @Operation(
            summary = "Update task",
            description = "Updates task details. Members can update their assigned tasks, while admins and project managers have broader management access."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task details",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Failed",
                                            value = EXAMPLE_400_VALIDATION
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Request Body",
                                            value = EXAMPLE_400_BAD_REQUEST
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_TASK)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequestDTO request) {

        return ResponseEntity.ok(
                taskService.updateTask(id, request)
        );
    }

    // Delete task
    @Operation(
            summary = "Delete task",
            description = "Deletes a task. Only admins and project managers can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404_TASK)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }

    // Get tasks with pagination
    @Operation(
            summary = "Get tasks with pagination",
            description = "Retrieves tasks in pages. Default page is 0 and default page size is 5. Maximum page size is 100."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Invalid Pagination",
                                    value = EXAMPLE_400_PAGINATION
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @GetMapping("/page")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        validatePagination(page, size);

        return ResponseEntity.ok(
                taskService.getAllTasksWithPagination(page, size)
        );
    }

    // Filter tasks by status
    @Operation(
            summary = "Filter tasks by status",
            description = "Retrieves tasks matching the specified status with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks filtered successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status or pagination parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Pagination",
                                            value = EXAMPLE_400_PAGINATION
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Status",
                                            value = EXAMPLE_400_INVALID_PARAMETER
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @GetMapping("/filter/status")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByStatus(
            @RequestParam TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        validatePagination(page, size);

        return ResponseEntity.ok(
                taskService.getTasksByStatus(status, page, size)
        );
    }

    // Filter tasks by priority
    @Operation(
            summary = "Filter tasks by priority",
            description = "Retrieves tasks matching the specified priority with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks filtered successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid priority or pagination parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Pagination",
                                            value = EXAMPLE_400_PAGINATION
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Priority",
                                            value = """
                                                    {
                                                      "timestamp": "2026-11-12T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Invalid value for parameter: priority"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @GetMapping("/filter/priority")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByPriority(
            @RequestParam TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        validatePagination(page, size);

        return ResponseEntity.ok(
                taskService.getTasksByPriority(priority, page, size)
        );
    }

    // Search tasks by title
    @Operation(
            summary = "Search tasks by title",
            description = "Searches tasks using a title keyword and returns paginated results."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search or pagination parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Invalid Pagination",
                                    value = EXAMPLE_400_PAGINATION
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        validatePagination(page, size);

        return ResponseEntity.ok(
                taskService.searchTasksByTitle(title, page, size)
        );
    }

    // Search and filter tasks together
    @Operation(
            summary = "Search and filter tasks",
            description = "Searches tasks by title and filters them by status and priority with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search and filter results retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search, filter or pagination parameters",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Pagination",
                                            value = EXAMPLE_400_PAGINATION
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Status",
                                            value = EXAMPLE_400_INVALID_PARAMETER
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Priority",
                                            value = """
                                                    {
                                                      "timestamp": "2026-11-12T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Invalid value for parameter: priority"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @GetMapping("/search/filter")
    public ResponseEntity<Page<TaskResponseDTO>> searchAndFilterTasks(
            @RequestParam String title,
            @RequestParam TaskStatus status,
            @RequestParam TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        validatePagination(page, size);

        return ResponseEntity.ok(
                taskService.searchAndFilterTasks(
                        title, status, priority, page, size
                )
        );
    }

    // Common pagination validation
    private void validatePagination(int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
    }
}
