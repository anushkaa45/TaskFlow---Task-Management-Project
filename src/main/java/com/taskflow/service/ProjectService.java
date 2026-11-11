
package com.taskflow.service;

import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;

import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectMember;
import com.taskflow.entity.User;

import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            AuthenticatedUserService authenticatedUserService) {

        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    private ProjectResponseDTO mapToResponse(Project project) {

        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    // Check whether the current user can manage a project
    private void checkProjectManagementAccess(Project project) {

        String role = authenticatedUserService.getCurrentUserRole();

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("PROJECT_MANAGER".equals(role)
                && project.getCreatedBy().getId()
                .equals(authenticatedUserService.getCurrentUserId())) {
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to manage this project");
    }

    // Check whether the current user can view a project
    private void checkProjectViewAccess(Project project) {

        String role = authenticatedUserService.getCurrentUserRole();

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("PROJECT_MANAGER".equals(role)
                && project.getCreatedBy().getId()
                .equals(authenticatedUserService.getCurrentUserId())) {
            return;
        }

        if ("MEMBER".equals(role)
                && projectMemberRepository.existsByProject_IdAndUser_Id(
                project.getId(),
                authenticatedUserService.getCurrentUserId())) {
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to view this project");
    }

    // Create a new project
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {

        String role = authenticatedUserService.getCurrentUserRole();

        if (!"ADMIN".equals(role) && !"PROJECT_MANAGER".equals(role)) {
            throw new AccessDeniedException(
                    "Only administrators and project managers can create projects");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be earlier than start date");
        }

        User creator = userRepository.findById(
                authenticatedUserService.getCurrentUserId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Authenticated user not found"));

        Project project = new Project();

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setStatus(dto.getStatus());
        project.setCreatedBy(creator);

        Project savedProject = projectRepository.save(project);

        return mapToResponse(savedProject);
    }

    // Get projects according to the current user's role
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getAllProjects() {

        String role = authenticatedUserService.getCurrentUserRole();
        Long currentUserId = authenticatedUserService.getCurrentUserId();

        if ("ADMIN".equals(role)) {
            return projectRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        if ("PROJECT_MANAGER".equals(role)) {
            return projectRepository.findByCreatedBy_Id(currentUserId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        if ("MEMBER".equals(role)) {
            return projectRepository.findAll()
                    .stream()
                    .filter(project ->
                            projectMemberRepository
                                    .existsByProject_IdAndUser_Id(
                                            project.getId(), currentUserId))
                    .map(this::mapToResponse)
                    .toList();
        }

        throw new AccessDeniedException(
                "You are not allowed to view projects");
    }

    // Get project by ID
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id));

        checkProjectViewAccess(project);

        return mapToResponse(project);
    }

    // Update project
    @Transactional
    public ProjectResponseDTO updateProject(
            Long id, ProjectUpdateRequestDTO dto) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id));

        checkProjectManagementAccess(project);

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be earlier than start date");
        }

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setStatus(dto.getStatus());

        Project updatedProject = projectRepository.save(project);

        return mapToResponse(updatedProject);
    }

    // Delete project
    @Transactional
    public void deleteProject(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id));

        checkProjectManagementAccess(project);

        projectRepository.delete(project);
    }
}
