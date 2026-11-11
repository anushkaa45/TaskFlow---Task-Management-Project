
package com.taskflow.controller;

import com.taskflow.dto.response.dashboard.AdminDashboardResponseDTO;
import com.taskflow.dto.response.dashboard.ManagerDashboardResponseDTO;
import com.taskflow.dto.response.dashboard.MemberDashboardResponseDTO;
import com.taskflow.service.AuthenticatedUserService;
import com.taskflow.service.DashboardService;

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

    // Constructor Injection
    public DashboardController(
            DashboardService dashboardService,
            AuthenticatedUserService authenticatedUserService) {

        this.dashboardService = dashboardService;
        this.authenticatedUserService = authenticatedUserService;
    }

    // Admin Dashboard
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponseDTO> getAdminDashboard() {

        return ResponseEntity.ok(
                dashboardService.getAdminDashboard()
        );
    }

    // Project Manager Dashboard
    @GetMapping("/manager")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ManagerDashboardResponseDTO> getManagerDashboard() {

        Long managerId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                dashboardService.getManagerDashboard(managerId)
        );
    }

    // Member Dashboard
    @GetMapping("/member")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<MemberDashboardResponseDTO> getMemberDashboard() {

        Long userId = authenticatedUserService.getCurrentUserId();

        return ResponseEntity.ok(
                dashboardService.getMemberDashboard(userId)
        );
    }
}
