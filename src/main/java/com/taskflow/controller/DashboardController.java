
package com.taskflow.controller;

import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.dto.response.dashboard.AdminDashboardResponseDTO;
import com.taskflow.dto.response.dashboard.ManagerDashboardResponseDTO;
import com.taskflow.dto.response.dashboard.MemberDashboardResponseDTO;
import com.taskflow.service.AuthenticatedUserService;
import com.taskflow.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthenticatedUserService authenticatedUserService;

    // Reusable Swagger error examples

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

    private static final String EXAMPLE_500 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 500,
              "error": "Internal Server Error",
              "message": "Something went wrong. Please try again later."
            }
            """;

    // Constructor Injection
    public DashboardController(
            DashboardService dashboardService,
            AuthenticatedUserService authenticatedUserService) {

        this.dashboardService = dashboardService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // Admin Dashboard
    @Operation(
            summary = "Get admin dashboard",
            description = "Retrieves overall system statistics, including users, projects, tasks and task status counts."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin dashboard retrieved successfully"
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
                    description = "Admin access required",
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
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponseDTO> getAdminDashboard() {

        return ResponseEntity.ok(
                dashboardService.getAdminDashboard()
        );
    }

    // Project Manager Dashboard
    @Operation(
            summary = "Get project manager dashboard",
            description = "Retrieves statistics for the authenticated project manager's projects, tasks and team performance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Manager dashboard retrieved successfully"
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
                    description = "Project manager access required",
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
    @GetMapping("/manager")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ManagerDashboardResponseDTO> getManagerDashboard() {

        Long managerId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                dashboardService.getManagerDashboard(managerId)
        );
    }

    // Member Dashboard
    @Operation(
            summary = "Get member dashboard",
            description = "Retrieves the authenticated member's assigned tasks, completion statistics and upcoming deadlines."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Member dashboard retrieved successfully"
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
                    description = "Member access required",
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
    @GetMapping("/member")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<MemberDashboardResponseDTO> getMemberDashboard() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                dashboardService.getMemberDashboard(userId)
        );
    }

    // TEMPORARY: Test 500 Internal Server Error
    @GetMapping("/test-error")
    public void testError() {
        throw new RuntimeException("Testing 500 Internal Server Error");
    }
}
