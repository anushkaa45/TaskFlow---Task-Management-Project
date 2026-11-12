
package com.taskflow.controller;

import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    // Reusable error examples
    private static final String EXAMPLE_400_VALIDATION = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Validation Failed",
              "fieldErrors": {
                "name": "Project name is required"
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

    private static final String EXAMPLE_404 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Project not found with id: 10"
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

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create a new project
    @Operation(
            summary = "Create a new project",
            description = "Creates a project. Only admins and project managers can create projects."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project details",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
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
                    description = "Referenced user not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Referenced User Not Found",
                                    value = """
                                            {
                                              "timestamp": "2026-11-12T10:30:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "User not found with id: 10"
                                            }
                                            """
                            )
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
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO dto) {

        ProjectResponseDTO response =
                projectService.createProject(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all accessible projects
    @Operation(
            summary = "Get all accessible projects",
            description = "Retrieves projects accessible to the authenticated user based on their role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully"
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {

        List<ProjectResponseDTO> projects =
                projectService.getAllProjects();

        return ResponseEntity.ok(projects);
    }

    // Get project by ID
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves details of a specific project using its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully"
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
                            examples = @ExampleObject(value = EXAMPLE_404)
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
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @PathVariable Long id) {

        ProjectResponseDTO project =
                projectService.getProjectById(id);

        return ResponseEntity.ok(project);
    }

    // Update project
    @Operation(
            summary = "Update project",
            description = "Updates project details. Only the project owner or an admin can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project details",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
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
                    description = "Project not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404)
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
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequestDTO dto) {

        ProjectResponseDTO updatedProject =
                projectService.updateProject(id, dto);

        return ResponseEntity.ok(updatedProject);
    }

    // Delete project
    @Operation(
            summary = "Delete project",
            description = "Deletes a project. Only the project owner or an admin can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Project deleted successfully"
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
                            examples = @ExampleObject(value = EXAMPLE_404)
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
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id) {

        projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }
}
