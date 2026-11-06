package com.taskflow.service;

import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.exception.ResourceAlreadyExistsException;
import com.taskflow.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be earlier than start date"
            );
        }

        User creator = userRepository.findById(dto.getCreatedByUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Creator not found with id: "
                                        + dto.getCreatedByUserId()
                        ));

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

    public List<ProjectResponseDTO> getAllProjects() {

        return projectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProjectResponseDTO getProjectById(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id
                        ));

        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponseDTO updateProject(
            Long id, ProjectUpdateRequestDTO dto) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id
                        ));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be earlier than start date"
            );
        }

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setStatus(dto.getStatus());

        Project updatedProject = projectRepository.save(project);

        return mapToResponse(updatedProject);
    }

    public void deleteProject(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id
                        ));

        projectRepository.delete(project);
    }
}