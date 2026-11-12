
package com.taskflow.controller;

import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.service.ProjectMemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

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

    private static final String EXAMPLE_404_MEMBERSHIP = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Project membership not found"
            }
            """;

    private static final String EXAMPLE_409 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 409,
              "error": "Conflict",
              "message": "User is already a member of this project"
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

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    // Add a member to a project
    @Operation(
            summary = "Add a project member",
            description = "Adds an existing user to a project team. Only admins and project managers can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Member added successfully"
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
                    description = "Project or user not found",
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
                    responseCode = "409",
                    description = "User is already a project member",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_409)
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
    @PostMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        UserResponseDTO response =
                projectMemberService.addMember(projectId, userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get all members of a project
    @Operation(
            summary = "Get project members",
            description = "Retrieves all members belonging to a specific project."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Members retrieved successfully"
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
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getProjectMembers(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                projectMemberService.getProjectMembers(projectId)
        );
    }

    // Remove a member from a project
    @Operation(
            summary = "Remove a project member",
            description = "Removes a user from a project team. Only admins and project managers can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Member removed successfully"
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
                    description = "Project or membership not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Project Not Found",
                                            value = EXAMPLE_404_PROJECT
                                    ),
                                    @ExampleObject(
                                            name = "Membership Not Found",
                                            value = EXAMPLE_404_MEMBERSHIP
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
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        projectMemberService.removeMember(projectId, userId);

        return ResponseEntity.noContent().build();
    }
}
