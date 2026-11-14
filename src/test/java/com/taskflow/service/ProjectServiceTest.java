
package com.taskflow.service;

import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;
import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void memberCannotCreateProject() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.createProject(null)
        );

        verifyNoInteractions(
                projectRepository,
                userRepository
        );
    }


    @Test
    void projectManagerCannotCreateProjectWhenUserDoesNotExist() {

        // Arrange
        ProjectRequestDTO request = new ProjectRequestDTO();

        request.setName("TaskFlow");
        request.setDescription("Task management project");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(10));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(userRepository.findById(5L))
                .thenReturn(java.util.Optional.empty());

        // Act and Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.createProject(request)
        );

        verify(userRepository).findById(5L);
        verifyNoInteractions(projectRepository);
    }

    private ProjectRequestDTO createProjectRequest() {
        ProjectRequestDTO request = new ProjectRequestDTO();

        request.setName("TaskFlow");
        request.setDescription("Task management system");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.PLANNING);

        return request;
    }

    @Test
    void adminCanCreateProjectSuccessfully() {

        // Arrange
        ProjectRequestDTO request = createProjectRequest();

        User creator = new User();
        creator.setId(1L);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(creator));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project project = invocation.getArgument(0);
                    project.setId(101L);
                    return project;
                });

        // Act
        ProjectResponseDTO response =
                projectService.createProject(request);

        // Assert
        assertNotNull(response);
        assertEquals("TaskFlow", response.getName());

        verify(userRepository).findById(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void projectManagerCanCreateProjectSuccessfully() {

        // Arrange
        ProjectRequestDTO request = createProjectRequest();

        User creator = new User();
        creator.setId(5L);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(creator));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project project = invocation.getArgument(0);
                    project.setId(102L);
                    return project;
                });

        // Act
        ProjectResponseDTO response =
                projectService.createProject(request);

        // Assert
        assertNotNull(response);
        assertEquals("TaskFlow", response.getName());

        verify(userRepository).findById(5L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void adminCanGetAllProjects() {

        User creator = new User();
        creator.setId(1L);

        Project project = new Project();
        project.setId(101L);
        project.setName("TaskFlow");
        project.setDescription("Task management system");
        project.setCreatedBy(creator);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(1L);

        when(projectRepository.findAll())
                .thenReturn(List.of(project));

        List<ProjectResponseDTO> response =
                projectService.getAllProjects();

        assertEquals(1, response.size());
        assertEquals("TaskFlow", response.get(0).getName());

        verify(projectRepository).findAll();
    }

    @Test
    void projectManagerCanGetOwnProjects() {

        User creator = new User();
        creator.setId(5L);

        Project project = new Project();
        project.setId(101L);
        project.setName("TaskFlow");
        project.setCreatedBy(creator);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(projectRepository.findByCreatedBy_Id(5L))
                .thenReturn(List.of(project));

        List<ProjectResponseDTO> response =
                projectService.getAllProjects();

        assertEquals(1, response.size());
        assertEquals("TaskFlow", response.get(0).getName());

        verify(projectRepository).findByCreatedBy_Id(5L);
    }

    @Test
    void memberCanGetProjectsTheyBelongTo() {

        User creator = new User();
        creator.setId(1L);

        Project project = new Project();
        project.setId(101L);
        project.setName("TaskFlow");
        project.setCreatedBy(creator);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(projectRepository.findAll())
                .thenReturn(List.of(project));

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(101L, 10L))
                .thenReturn(true);

        List<ProjectResponseDTO> response =
                projectService.getAllProjects();

        assertEquals(1, response.size());
        assertEquals("TaskFlow", response.get(0).getName());

        verify(projectMemberRepository)
                .existsByProject_IdAndUser_Id(101L, 10L);
    }

    @Test
    void getProjectByIdThrowsExceptionWhenProjectDoesNotExist() {

        when(projectRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.getProjectById(999L)
        );

        verify(projectRepository).findById(999L);
    }

    @Test
    void memberCannotViewProjectTheyDoNotBelongTo() {

        User creator = new User();
        creator.setId(1L);

        Project project = new Project();
        project.setId(101L);
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(projectMemberRepository
                .existsByProject_IdAndUser_Id(101L, 10L))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.getProjectById(101L)
        );
    }

    @Test
    void projectManagerCanViewOwnProject() {

        User creator = new User();
        creator.setId(5L);

        Project project = new Project();
        project.setId(101L);
        project.setName("TaskFlow");
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        ProjectResponseDTO response =
                projectService.getProjectById(101L);

        assertNotNull(response);
        assertEquals("TaskFlow", response.getName());
    }

    @Test
    void memberCannotUpdateProject() {

        User creator = new User();
        creator.setId(1L);

        Project project = new Project();
        project.setId(101L);
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProject(101L, request)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateProjectThrowsExceptionWhenEndDateIsBeforeStartDate() {

        User creator = new User();
        creator.setId(5L);

        Project project = new Project();
        project.setId(101L);
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("Updated TaskFlow");
        request.setDescription("Updated description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().minusDays(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.updateProject(101L, request)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProjectThrowsExceptionWhenProjectDoesNotExist() {

        when(projectRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.deleteProject(999L)
        );

        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void adminCanDeleteProjectSuccessfully() {

        Project project = new Project();
        project.setId(101L);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        projectService.deleteProject(101L);

        verify(projectRepository).delete(project);
    }

    @Test
    void adminCanViewProjectById() {

        Project project = new Project();
        project.setId(101L);
        project.setName("TaskFlow");

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        ProjectResponseDTO response =
                projectService.getProjectById(101L);

        assertNotNull(response);
        assertEquals("TaskFlow", response.getName());
    }

    @Test
    void projectManagerCannotUpdateAnotherManagersProject() {

        User creator = new User();
        creator.setId(20L);

        Project project = new Project();
        project.setId(101L);
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProject(
                        101L, new ProjectUpdateRequestDTO())
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void memberCannotDeleteProject() {

        Project project = new Project();
        project.setId(101L);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteProject(101L)
        );

        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void projectManagerCanDeleteOwnProject() {

        User creator = new User();
        creator.setId(5L);

        Project project = new Project();
        project.setId(101L);
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        projectService.deleteProject(101L);

        verify(projectRepository).delete(project);
    }

    @Test
    void createProjectThrowsExceptionWhenEndDateIsBeforeStartDate() {

        ProjectRequestDTO request = createProjectRequest();

        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().minusDays(1));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.createProject(request)
        );

        verifyNoInteractions(userRepository, projectRepository);
    }

    @Test
    void adminCanUpdateProjectSuccessfully() {

        Project project = new Project();
        project.setId(101L);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("Updated TaskFlow");
        request.setDescription("Updated project description");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.IN_PROGRESS);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponseDTO response =
                projectService.updateProject(101L, request);

        assertNotNull(response);
        assertEquals("Updated TaskFlow", response.getName());
        assertEquals(ProjectStatus.IN_PROGRESS, response.getStatus());

        verify(projectRepository).save(project);
    }

    @Test
    void projectManagerCanUpdateOwnProjectSuccessfully() {

        User creator = new User();
        creator.setId(5L);

        Project project = new Project();
        project.setId(101L);
        project.setCreatedBy(creator);

        when(projectRepository.findById(101L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        ProjectUpdateRequestDTO request = new ProjectUpdateRequestDTO();
        request.setName("Manager Updated TaskFlow");
        request.setDescription("Updated by project manager");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setStatus(ProjectStatus.IN_PROGRESS);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponseDTO response =
                projectService.updateProject(101L, request);

        assertNotNull(response);
        assertEquals("Manager Updated TaskFlow", response.getName());

        verify(projectRepository).save(project);
    }
}
