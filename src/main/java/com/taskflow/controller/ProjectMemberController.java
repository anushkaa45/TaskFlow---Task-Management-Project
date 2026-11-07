package com.taskflow.controller;

import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.service.ProjectMemberService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        UserResponseDTO response =
                projectMemberService.addMember(projectId, userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getProjectMembers(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                projectMemberService.getProjectMembers(projectId)
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        projectMemberService.removeMember(projectId, userId);

        return ResponseEntity.noContent().build();
    }
}