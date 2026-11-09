package com.taskflow.service;

import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectMember;
import com.taskflow.entity.User;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.exception.ResourceAlreadyExistsException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.entity.NotificationType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ProjectMemberService(
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository, NotificationService notificationService) {

        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // Add a user to a project

    @Transactional
    public UserResponseDTO addMember(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + projectId));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId));

        if (projectMemberRepository
                .existsByProject_IdAndUser_Id(projectId, userId)) {

            throw new ResourceAlreadyExistsException(
                    "User is already a member of this project");
        }

        ProjectMember projectMember = new ProjectMember();

        projectMember.setProject(project);
        projectMember.setUser(user);

        projectMemberRepository.save(projectMember);

        notificationService.createNotification(
                userId,
                "You have been added to the project: " + project.getName(),
                NotificationType.PROJECT_MEMBER_ADDED
        );

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }


    // Get all members of a project
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getProjectMembers(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + projectId));

        List<ProjectMember> members =
                projectMemberRepository.findByProject_Id(projectId);

        return members.stream()
                .map(member -> {
                    User user = member.getUser();

                    return new UserResponseDTO(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole().name(),
                            user.getCreatedAt()
                    );
                })
                .toList();
    }


    @Transactional
    public void removeMember(Long projectId, Long userId) {

        ProjectMember membership =
                projectMemberRepository
                        .findByProject_IdAndUser_Id(projectId, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Membership not found"));

        projectMemberRepository.delete(membership);
    }
}