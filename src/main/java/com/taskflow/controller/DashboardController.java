
package com.taskflow.controller;

import com.taskflow.dto.response.dashboard.AdminDashboardResponseDTO;
import com.taskflow.dto.response.dashboard.ManagerDashboardResponseDTO;
import com.taskflow.dto.response.dashboard.MemberDashboardResponseDTO;
import com.taskflow.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Admin Dashboard
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardResponseDTO> getAdminDashboard() {

        AdminDashboardResponseDTO response =
                dashboardService.getAdminDashboard();

        return ResponseEntity.ok(response);
    }

    // Project Manager Dashboard
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ManagerDashboardResponseDTO> getManagerDashboard(
            @PathVariable Long managerId) {

        ManagerDashboardResponseDTO response =
                dashboardService.getManagerDashboard(managerId);

        return ResponseEntity.ok(response);
    }

    // Member Dashboard
    @GetMapping("/member/{userId}")
    public ResponseEntity<MemberDashboardResponseDTO> getMemberDashboard(
            @PathVariable Long userId) {

        MemberDashboardResponseDTO response =
                dashboardService.getMemberDashboard(userId);

        return ResponseEntity.ok(response);
    }
}
