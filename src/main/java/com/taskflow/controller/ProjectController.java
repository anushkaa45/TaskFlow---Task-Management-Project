
package com.taskflow.controller;

import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;
import com.taskflow.service.ProjectService;

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

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ADMIN and PROJECT_MANAGER can create projects
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

    // All authenticated roles can view projects
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {

        List<ProjectResponseDTO> projects =
                projectService.getAllProjects();

        return ResponseEntity.ok(projects);
    }

    // All authenticated roles can view a project by ID
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @PathVariable Long id) {

        ProjectResponseDTO project =
                projectService.getProjectById(id);

        return ResponseEntity.ok(project);
    }

    // ADMIN and PROJECT_MANAGER can update projects
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequestDTO dto) {

        ProjectResponseDTO updatedProject =
                projectService.updateProject(id, dto);

        return ResponseEntity.ok(updatedProject);
    }

    // ADMIN and PROJECT_MANAGER can delete projects
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id) {

        projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }
}
