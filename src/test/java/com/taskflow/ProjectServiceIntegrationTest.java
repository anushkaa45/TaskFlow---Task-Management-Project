package com.taskflow;

import com.taskflow.dto.request.ProjectRequestDTO;
import com.taskflow.dto.request.ProjectUpdateRequestDTO;
import com.taskflow.dto.response.ProjectResponseDTO;

import com.taskflow.entity.*;
import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.UserRepository;

import com.taskflow.service.ProjectService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "jwt.secret=TaskFlowTestSecretKeyForJwtAuthentication123456789",
        "spring.datasource.url=jdbc:h2:mem:taskflow_project_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Transactional
class ProjectServiceIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private User manager;
    private User member;
    private User admin;

    @BeforeEach
    void setUp() {

        manager = new User();
        manager.setName("Test Manager");
        manager.setEmail("manager-project@test.com");
        manager.setPassword("password123");
        manager.setRole(UserRole.PROJECT_MANAGER);
        manager = userRepository.save(manager);

        member = new User();
        member.setName("Test Member");
        member.setEmail("member-project@test.com");
        member.setPassword("password123");
        member.setRole(UserRole.MEMBER);
        member = userRepository.save(member);

        admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-project@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(manager);
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, List.of()
                )
        );
    }

    private ProjectRequestDTO createProjectRequest() {

        ProjectRequestDTO dto = new ProjectRequestDTO();

        dto.setName("Integration Test Project");
        dto.setDescription("Testing ProjectService integration");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(30));
        dto.setStatus(ProjectStatus.PLANNING);

        return dto;
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_ShouldSaveProject_WhenManagerCreatesProject() {

        ProjectRequestDTO dto = createProjectRequest();

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());

        Project savedProject = projectRepository.findById(result.getId())
                .orElseThrow();

        assertEquals(manager.getId(), savedProject.getCreatedBy().getId());
    }

    @Test
    void createProject_ShouldAllowAdminToCreateProject() {

        authenticate(admin);

        ProjectRequestDTO dto = createProjectRequest();
        dto.setName("Admin Created Project");

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals("Admin Created Project", result.getName());

        Project savedProject = projectRepository.findById(result.getId())
                .orElseThrow();

        assertEquals(admin.getId(), savedProject.getCreatedBy().getId());
    }

    @Test
    void createProject_ShouldThrowAccessDenied_WhenMemberCreatesProject() {

        authenticate(member);

        ProjectRequestDTO dto = createProjectRequest();

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.createProject(dto)
        );

        assertEquals(0, projectRepository.count());
    }

    @Test
    void createProject_ShouldThrowException_WhenEndDateIsBeforeStartDate() {

        ProjectRequestDTO dto = createProjectRequest();

        dto.setStartDate(LocalDate.now().plusDays(10));
        dto.setEndDate(LocalDate.now().plusDays(5));

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.createProject(dto)
        );

        assertEquals(0, projectRepository.count());
    }

    // Test 5: Admin can retrieve all projects
    @Test
    void getAllProjects_ShouldReturnAllProjects_WhenAdminRequests() {

        Project project1 = createProject(manager, "Project One");
        Project project2 = createProject(manager, "Project Two");

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager-project@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project3 = createProject(anotherManager, "Project Three");

        authenticate(admin);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(3, result.size());
    }


    // Test 6: Project manager can retrieve only their own projects
    @Test
    void getAllProjects_ShouldReturnOnlyManagersOwnProjects() {

        Project project1 = createProject(manager, "Manager Project One");
        Project project2 = createProject(manager, "Manager Project Two");

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager6@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        createProject(anotherManager, "Other Manager Project");

        authenticate(manager);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(2, result.size());

        assertTrue(result.stream()
                .allMatch(p -> p.getName().startsWith("Manager Project")));
    }


    // Test 7: Member can retrieve only projects they belong to
    @Test
    void getAllProjects_ShouldReturnOnlyProjectsWhereMemberIsAdded() {

        Project project1 = createProject(manager, "Member Project");

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager7@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        createProject(anotherManager, "Unrelated Project");

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project1);
        projectMember.setUser(member);
        projectMemberRepository.save(projectMember);

        authenticate(member);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(1, result.size());
        assertEquals(project1.getName(), result.get(0).getName());
    }


    // Test 8: Manager receives empty list when no projects exist
    @Test
    void getAllProjects_ShouldReturnEmptyList_WhenManagerHasNoProjects() {

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // Helper method: Create and save a project
    private Project createProject(User creator, String name) {

        Project project = new Project();

        project.setName(name);
        project.setDescription("Integration testing project");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(30));
        project.setStatus(ProjectStatus.PLANNING);
        project.setCreatedBy(creator);

        return projectRepository.save(project);
    }

    // Test 9: Project manager can retrieve their own project
    @Test
    void getProjectById_ShouldReturnProject_WhenManagerOwnsProject() {

        Project project = createProject(manager, "Manager Own Project");

        authenticate(manager);

        ProjectResponseDTO result =
                projectService.getProjectById(project.getId());

        assertNotNull(result);
        assertEquals(project.getId(), result.getId());
        assertEquals("Manager Own Project", result.getName());
    }


    // Test 10: Member can retrieve a project they belong to
    @Test
    void getProjectById_ShouldAllowMember_WhenMemberBelongsToProject() {

        Project project = createProject(manager, "Member Accessible Project");

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(member);
        projectMemberRepository.save(projectMember);

        authenticate(member);

        ProjectResponseDTO result =
                projectService.getProjectById(project.getId());

        assertNotNull(result);
        assertEquals(project.getId(), result.getId());
    }


    // Test 11: Member cannot retrieve a project they do not belong to
    @Test
    void getProjectById_ShouldThrowAccessDenied_WhenMemberIsNotInProject() {

        Project project = createProject(manager, "Restricted Project");

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.getProjectById(project.getId())
        );
    }


    // Test 12: Exception when project does not exist
    @Test
    void getProjectById_ShouldThrowException_WhenProjectDoesNotExist() {

        Long projectId = 999L;

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.getProjectById(projectId)
        );
    }

    // Test 13: Project manager can update their own project
    @Test
    void updateProject_ShouldUpdateProject_WhenManagerOwnsProject() {

        Project project = createProject(manager, "Old Project Name");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Updated Project Name");
        dto.setDescription("Updated project description");
        dto.setStatus(ProjectStatus.IN_PROGRESS);

        authenticate(manager);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertNotNull(result);
        assertEquals("Updated Project Name", result.getName());
        assertEquals("Updated project description", result.getDescription());
        assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus());
    }


    // Test 14: Admin can update another manager's project
    @Test
    void updateProject_ShouldAllowAdminToUpdateAnyProject() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager14@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project = createProject(anotherManager, "Original Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Admin Updated Project");

        authenticate(admin);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertNotNull(result);
        assertEquals("Admin Updated Project", result.getName());
    }


    // Test 15: Update fails when end date is before start date
    @Test
    void updateProject_ShouldThrowException_WhenEndDateIsBeforeStartDate() {

        Project project = createProject(manager, "Date Validation Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();

        dto.setStartDate(LocalDate.now().plusDays(10));
        dto.setEndDate(LocalDate.now().plusDays(5));

        authenticate(manager);

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.updateProject(project.getId(), dto)
        );
    }


    // Test 16: Manager cannot update another manager's project
    @Test
    void updateProject_ShouldThrowAccessDenied_WhenManagerDoesNotOwnProject() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager16@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project = createProject(anotherManager, "Other Manager Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Unauthorized Update");

        authenticate(manager);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProject(project.getId(), dto)
        );
    }


    // Helper method: Create a valid project update request
    private ProjectUpdateRequestDTO createProjectUpdateRequest() {

        ProjectUpdateRequestDTO dto = new ProjectUpdateRequestDTO();

        dto.setName("Updated Project");
        dto.setDescription("Updated project description");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(60));
        dto.setStatus(ProjectStatus.PLANNING);

        return dto;
    }

    // Test 17: Project manager can delete their own project
    @Test
    void deleteProject_ShouldDeleteProject_WhenManagerOwnsProject() {

        Project project = createProject(manager, "Project To Delete");

        authenticate(manager);

        projectService.deleteProject(project.getId());

        assertFalse(projectRepository.existsById(project.getId()));
    }


    // Test 18: Admin can delete another manager's project
    @Test
    void deleteProject_ShouldAllowAdminToDeleteAnyProject() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager18@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project = createProject(anotherManager, "Admin Delete Project");

        authenticate(admin);

        projectService.deleteProject(project.getId());

        assertFalse(projectRepository.existsById(project.getId()));
    }


    // Test 19: Manager cannot delete another manager's project
    @Test
    void deleteProject_ShouldThrowAccessDenied_WhenManagerDoesNotOwnProject() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager19@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project = createProject(anotherManager, "Restricted Delete Project");

        authenticate(manager);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteProject(project.getId())
        );

        assertTrue(projectRepository.existsById(project.getId()));
    }


    // Test 20: Deleting a nonexistent project throws ResourceNotFoundException
    @Test
    void deleteProject_ShouldThrowException_WhenProjectDoesNotExist() {

        Long projectId = 999L;

        authenticate(manager);

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.deleteProject(projectId)
        );
    }

    // Test 21: Admin can retrieve a project created by another manager
    @Test
    void getProjectById_ShouldAllowAdminToViewAnyProject() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager21@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project = createProject(anotherManager, "Admin View Project");

        authenticate(admin);

        ProjectResponseDTO result =
                projectService.getProjectById(project.getId());

        assertNotNull(result);
        assertEquals(project.getId(), result.getId());
        assertEquals("Admin View Project", result.getName());
    }


    // Test 22: Project manager cannot view another manager's project
    @Test
    void getProjectById_ShouldThrowAccessDenied_WhenManagerDoesNotOwnProject() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager22@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project project = createProject(anotherManager, "Restricted View Project");

        authenticate(manager);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.getProjectById(project.getId())
        );
    }


    // Test 23: Admin can retrieve an empty project list when no projects exist
    @Test
    void getAllProjects_ShouldReturnEmptyList_WhenAdminHasNoProjects() {

        authenticate(admin);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // Test 24: Member receives an empty list when they belong to no projects
    @Test
    void getAllProjects_ShouldReturnEmptyList_WhenMemberHasNoProjects() {

        authenticate(member);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Test 25: Project manager can create a project with IN_PROGRESS status
    @Test
    void createProject_ShouldPreserveProjectStatus_WhenStatusIsProvided() {

        ProjectRequestDTO dto = createProjectRequest();
        dto.setStatus(ProjectStatus.IN_PROGRESS);

        authenticate(manager);

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus());
    }


    // Test 26: Project manager can create a project with COMPLETED status
    @Test
    void createProject_ShouldPreserveCompletedStatus_WhenStatusIsCompleted() {

        ProjectRequestDTO dto = createProjectRequest();
        dto.setName("Completed Project");
        dto.setStatus(ProjectStatus.COMPLETED);

        authenticate(manager);

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals(ProjectStatus.COMPLETED, result.getStatus());
    }


    // Test 27: Admin can create a project with ON_HOLD status
    @Test
    void createProject_ShouldAllowAdminToCreateProjectWithOnHoldStatus() {

        ProjectRequestDTO dto = createProjectRequest();
        dto.setName("On Hold Project");
        dto.setStatus(ProjectStatus.ON_HOLD);

        authenticate(admin);

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals(ProjectStatus.ON_HOLD, result.getStatus());
    }


    // Test 28: Project manager can create a project with null status
    @Test
    void createProject_ShouldUseDefaultStatus_WhenStatusIsNull() {

        ProjectRequestDTO dto = createProjectRequest();
        dto.setStatus(null);

        authenticate(manager);

        ProjectResponseDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals(ProjectStatus.PLANNING, result.getStatus());
    }

    // Test 29: Manager can update project description
    @Test
    void updateProject_ShouldUpdateDescription_WhenManagerOwnsProject() {

        Project project = createProject(manager, "Description Update Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setDescription("New updated description");

        authenticate(manager);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertNotNull(result);
        assertEquals("New updated description", result.getDescription());
    }


    // Test 30: Manager can update project dates
    @Test
    void updateProject_ShouldUpdateDates_WhenManagerOwnsProject() {

        Project project = createProject(manager, "Date Update Project");

        LocalDate newStartDate = LocalDate.now().plusDays(5);
        LocalDate newEndDate = LocalDate.now().plusDays(90);

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setStartDate(newStartDate);
        dto.setEndDate(newEndDate);

        authenticate(manager);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertEquals(newStartDate, result.getStartDate());
        assertEquals(newEndDate, result.getEndDate());
    }


    // Test 31: Manager can change project status
    @Test
    void updateProject_ShouldChangeStatus_WhenManagerOwnsProject() {

        Project project = createProject(manager, "Status Update Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setStatus(ProjectStatus.COMPLETED);

        authenticate(manager);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertEquals(ProjectStatus.COMPLETED, result.getStatus());
    }


    // Test 32: Updating a nonexistent project throws ResourceNotFoundException
    @Test
    void updateProject_ShouldThrowException_WhenProjectDoesNotExist() {

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();

        authenticate(manager);

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.updateProject(999L, dto)
        );
    }

    // Test 33: Member cannot update a project, even if they belong to it
    @Test
    void updateProject_ShouldDenyMember_WhenMemberBelongsToProject() {

        Project project = createProject(manager, "Member Update Restricted");

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(member);
        projectMemberRepository.save(projectMember);

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Unauthorized Update");

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProject(project.getId(), dto)
        );

        Project unchangedProject = projectRepository.findById(project.getId())
                .orElseThrow();

        assertEquals("Member Update Restricted", unchangedProject.getName());
    }


    // Test 34: Member cannot delete a project, even if they belong to it
    @Test
    void deleteProject_ShouldDenyMember_WhenMemberBelongsToProject() {

        Project project = createProject(manager, "Member Delete Restricted");

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(member);
        projectMemberRepository.save(projectMember);

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteProject(project.getId())
        );

        assertTrue(projectRepository.existsById(project.getId()));
    }


    // Test 35: Admin can update project status to ON_HOLD
    @Test
    void updateProject_ShouldAllowAdminToSetOnHoldStatus() {

        Project project = createProject(manager, "Admin Status Update");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setStatus(ProjectStatus.ON_HOLD);

        authenticate(admin);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertEquals(ProjectStatus.ON_HOLD, result.getStatus());
    }


    // Test 36: Updating a project should preserve its original creator
    @Test
    void updateProject_ShouldPreserveOriginalCreator() {

        Project project = createProject(manager, "Creator Preservation Project");

        Long originalCreatorId = project.getCreatedBy().getId();

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Updated Creator Preservation Project");

        authenticate(manager);

        projectService.updateProject(project.getId(), dto);

        Project updatedProject = projectRepository.findById(project.getId())
                .orElseThrow();

        assertEquals(
                originalCreatorId,
                updatedProject.getCreatedBy().getId()
        );
    }


    // Test 37: Project creation should persist the supplied dates
    @Test
    void createProject_ShouldPersistStartDateAndEndDate() {

        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(45);

        ProjectRequestDTO dto = createProjectRequest();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        authenticate(manager);

        ProjectResponseDTO result = projectService.createProject(dto);

        Project savedProject = projectRepository.findById(result.getId())
                .orElseThrow();

        assertEquals(startDate, savedProject.getStartDate());
        assertEquals(endDate, savedProject.getEndDate());
    }


    // Test 38: Project creation should persist the supplied status
    @Test
    void createProject_ShouldPersistOnHoldStatus() {

        ProjectRequestDTO dto = createProjectRequest();
        dto.setName("On Hold Creation Project");
        dto.setStatus(ProjectStatus.ON_HOLD);

        authenticate(manager);

        ProjectResponseDTO result = projectService.createProject(dto);

        Project savedProject = projectRepository.findById(result.getId())
                .orElseThrow();

        assertEquals(ProjectStatus.ON_HOLD, savedProject.getStatus());
    }


    // Test 39: Updating a project should persist all updated fields
    @Test
    void updateProject_ShouldPersistAllUpdatedFields() {

        Project project = createProject(manager, "Original Project");

        LocalDate newStartDate = LocalDate.now().plusDays(5);
        LocalDate newEndDate = LocalDate.now().plusDays(100);

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Fully Updated Project");
        dto.setDescription("All project fields have been updated");
        dto.setStartDate(newStartDate);
        dto.setEndDate(newEndDate);
        dto.setStatus(ProjectStatus.COMPLETED);

        authenticate(manager);

        ProjectResponseDTO result =
                projectService.updateProject(project.getId(), dto);

        assertEquals("Fully Updated Project", result.getName());
        assertEquals("All project fields have been updated", result.getDescription());
        assertEquals(newStartDate, result.getStartDate());
        assertEquals(newEndDate, result.getEndDate());
        assertEquals(ProjectStatus.COMPLETED, result.getStatus());
    }


    // Test 40: Deleted project should no longer be retrievable
    @Test
    void deleteProject_ShouldMakeProjectUnavailableAfterDeletion() {

        Project project = createProject(manager, "Project Lifecycle Test");

        Long projectId = project.getId();

        authenticate(manager);

        projectService.deleteProject(projectId);

        assertFalse(projectRepository.existsById(projectId));

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.getProjectById(projectId)
        );
    }

    // Test 41: Admin can retrieve projects created by multiple managers
    @Test
    void getAllProjects_ShouldReturnProjectsFromAllManagers_WhenAdminRequests() {

        createProject(manager, "Manager One Project");

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager41@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        createProject(anotherManager, "Manager Two Project");

        authenticate(admin);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(2, result.size());
    }


    // Test 42: Manager should not see another manager's projects
    @Test
    void getAllProjects_ShouldExcludeOtherManagersProjects() {

        createProject(manager, "My Project");

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager42@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        createProject(anotherManager, "Other Project");

        authenticate(manager);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(1, result.size());
        assertEquals("My Project", result.get(0).getName());
    }


    // Test 43: Member should see multiple projects they belong to
    @Test
    void getAllProjects_ShouldReturnAllProjectsMemberBelongsTo() {

        Project project1 = createProject(manager, "Member Project One");
        Project project2 = createProject(manager, "Member Project Two");
        createProject(manager, "Unrelated Project");

        ProjectMember membership1 = new ProjectMember();
        membership1.setProject(project1);
        membership1.setUser(member);
        projectMemberRepository.save(membership1);

        ProjectMember membership2 = new ProjectMember();
        membership2.setProject(project2);
        membership2.setUser(member);
        projectMemberRepository.save(membership2);

        authenticate(member);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(2, result.size());
    }


    // Test 44: Admin can retrieve a project after its status changes
    @Test
    void getProjectById_ShouldReturnUpdatedStatus_WhenAdminRequests() {

        Project project = createProject(manager, "Status Retrieval Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setStatus(ProjectStatus.COMPLETED);

        authenticate(manager);
        projectService.updateProject(project.getId(), dto);

        authenticate(admin);

        ProjectResponseDTO result =
                projectService.getProjectById(project.getId());

        assertEquals(ProjectStatus.COMPLETED, result.getStatus());
    }


    // Test 45: Member cannot retrieve a project using an invalid ID
    @Test
    void getProjectById_ShouldThrowNotFound_WhenMemberRequestsInvalidId() {

        authenticate(member);

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.getProjectById(999L)
        );
    }


    // Test 46: Admin can delete a project and verify it is removed
    @Test
    void deleteProject_ShouldRemoveProject_WhenAdminDeletesIt() {

        Project project = createProject(manager, "Admin Removal Project");

        Long projectId = project.getId();

        authenticate(admin);
        projectService.deleteProject(projectId);

        assertFalse(projectRepository.existsById(projectId));
    }


    // Test 47: Project manager can retrieve an empty list when no projects exist
    @Test
    void getAllProjects_ShouldReturnEmptyList_WhenNoProjectsExist() {

        authenticate(manager);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // Test 48: Admin can retrieve project details after an update
    @Test
    void getProjectById_ShouldReturnUpdatedDetails_AfterManagerUpdatesProject() {

        Project project = createProject(manager, "Original Details Project");

        ProjectUpdateRequestDTO dto = createProjectUpdateRequest();
        dto.setName("Updated Details Project");
        dto.setDescription("Updated project details");

        authenticate(manager);
        projectService.updateProject(project.getId(), dto);

        authenticate(admin);

        ProjectResponseDTO result =
                projectService.getProjectById(project.getId());

        assertEquals("Updated Details Project", result.getName());
        assertEquals("Updated project details", result.getDescription());
    }
}