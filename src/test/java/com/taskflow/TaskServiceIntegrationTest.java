
package com.taskflow;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.entity.*;
import com.taskflow.repository.*;

import com.taskflow.exception.ResourceNotFoundException;

import com.taskflow.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        "spring.datasource.url=jdbc:h2:mem:taskflow_service_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User manager;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {

        // Create project manager
        manager = new User();
        manager.setName("Test Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword("password123");
        manager.setRole(UserRole.PROJECT_MANAGER);

        manager = userRepository.save(manager);

        // Create project member
        member = new User();
        member.setName("Test Member");
        member.setEmail("member@test.com");
        member.setPassword("password123");
        member.setRole(UserRole.MEMBER);

        member = userRepository.save(member);

        // Create project owned by manager
        project = new Project();
        project.setName("Integration Test Project");
        project.setDescription("Project for integration testing");
        project.setCreatedBy(manager);

        project = projectRepository.save(project);

        // Add member to project
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(member);

        projectMemberRepository.save(projectMember);

        // Authenticate project manager
        authenticate(manager);
    }

    private void authenticate(User user) {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of()
                )
        );
    }

    private TaskRequestDTO createTaskRequest(
            Long projectId, Long assignedUserId) {

        TaskRequestDTO request = new TaskRequestDTO();

        request.setTitle("Integration Test Task");
        request.setDescription("Testing TaskService with real database");
        request.setDueDate(LocalDate.now().plusDays(7));
        request.setProjectId(projectId);
        request.setAssignedToUserId(assignedUserId);

        return request;
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // TEST 1: Successfully create a task and notification
    @Test
    void createTask_ShouldSaveTaskAndNotification() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        // Verify task was saved
        List<Task> tasks =
                taskRepository.findByProject_Id(project.getId());

        assertEquals(1, tasks.size());
        assertEquals("Integration Test Task", tasks.get(0).getTitle());
        assertEquals(member.getId(), tasks.get(0).getAssignedTo().getId());

        // Verify notification was saved
        long notificationCount =
                notificationRepository
                        .countByRecipient_IdAndIsReadFalse(member.getId());

        assertEquals(1, notificationCount);
    }

    // TEST 2: Project does not exist
    @Test
    void createTask_ShouldThrowException_WhenProjectDoesNotExist() {

        TaskRequestDTO request =
                createTaskRequest(999999L, member.getId());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(request)
        );
    }

    // TEST 3: Manager cannot manage another manager's project
    @Test
    void createTask_ShouldThrowException_WhenManagerDoesNotOwnProject() {

        // Create another project manager
        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("anothermanager@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);

        anotherManager = userRepository.save(anotherManager);

        // Create project owned by another manager
        Project anotherProject = new Project();
        anotherProject.setName("Another Project");
        anotherProject.setDescription("Not owned by current manager");
        anotherProject.setCreatedBy(anotherManager);

        anotherProject = projectRepository.save(anotherProject);

        TaskRequestDTO request =
                createTaskRequest(anotherProject.getId(), member.getId());

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.createTask(request)
        );
    }

    // TEST 4: Assigned user does not exist
    @Test
    void createTask_ShouldThrowException_WhenAssignedUserDoesNotExist() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), 999999L);

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(request)
        );
    }


    // TEST 5: Assigned user is not a member of the project
    @Test
    void createTask_ShouldThrowException_WhenAssignedUserIsNotProjectMember() {

        User outsider = new User();
        outsider.setName("Outsider");
        outsider.setEmail("outsider@test.com");
        outsider.setPassword("password123");
        outsider.setRole(UserRole.MEMBER);

        outsider = userRepository.save(outsider);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), outsider.getId());

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(request)
        );
    }

    // TEST 6: Successfully retrieve a task by ID
    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        var response = taskService.getTaskById(createdTask.getId());

        assertNotNull(response);
        assertEquals(createdTask.getId(), response.getId());
        assertEquals("Integration Test Task", response.getTitle());
    }

    // TEST 7: Task does not exist
    @Test
    void getTaskById_ShouldThrowException_WhenTaskDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(999999L)
        );
    }

    // TEST 8: Successfully retrieve tasks belonging to a project
    @Test
    void getTasksByProject_ShouldReturnProjectTasks() {

        TaskRequestDTO request1 =
                createTaskRequest(project.getId(), member.getId());

        TaskRequestDTO request2 =
                createTaskRequest(project.getId(), member.getId());

        request2.setTitle("Second Integration Task");

        taskService.createTask(request1);
        taskService.createTask(request2);

        var responses = taskService.getTasksByProject(project.getId());

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertTrue(responses.stream().anyMatch(
                task -> task.getTitle().equals("Integration Test Task")
        ));

        assertTrue(responses.stream().anyMatch(
                task -> task.getTitle().equals("Second Integration Task")
        ));
    }


    // TEST 9: Member can retrieve tasks from their own project
    @Test
    void getTasksByProject_ShouldAllowMemberOfProject() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        // Login as project member
        authenticate(member);

        var responses = taskService.getTasksByProject(project.getId());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Integration Test Task", responses.get(0).getTitle());
    }

    // TEST 10: Member cannot retrieve tasks from another project
    @Test
    void getTasksByProject_ShouldDenyNonMember() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("manager2@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);

        anotherManager = userRepository.save(anotherManager);

        Project anotherProject = new Project();
        anotherProject.setName("Private Project");
        anotherProject.setDescription("Another manager's project");
        anotherProject.setCreatedBy(anotherManager);

        anotherProject = projectRepository.save(anotherProject);

        // Login as member who does not belong to this project
        authenticate(member);

        Long projectId = anotherProject.getId();

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> taskService.getTasksByProject(projectId)
        );
    }

    // TEST 11: Member can retrieve their own assigned tasks
    @Test
    void getTasksByUser_ShouldAllowMemberToViewOwnTasks() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        authenticate(member);

        var responses = taskService.getTasksByUser(member.getId());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Integration Test Task", responses.get(0).getTitle());
    }

    // TEST 12: Member cannot retrieve another user's tasks
    @Test
    void getTasksByUser_ShouldDenyMemberAccessToAnotherUsersTasks() {

        User anotherUser = new User();
        anotherUser.setName("Another Member");
        anotherUser.setEmail("anothermember@test.com");
        anotherUser.setPassword("password123");
        anotherUser.setRole(UserRole.MEMBER);

        anotherUser = userRepository.save(anotherUser);

        authenticate(member);

        Long anotherUserId = anotherUser.getId();

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> taskService.getTasksByUser(anotherUserId)
        );
    }


    // TEST 13: Project manager successfully updates a task
    @Test
    void updateTask_ShouldAllowProjectManagerToUpdateTask() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();

        updateRequest.setTitle("Updated Task Title");
        updateRequest.setDescription("Updated task description");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        var response = taskService.updateTask(
                createdTask.getId(), updateRequest);

        assertNotNull(response);
        assertEquals("Updated Task Title", response.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
        assertEquals(TaskPriority.HIGH, response.getPriority());

        // Verify changes were persisted
        Task savedTask = taskRepository.findById(createdTask.getId())
                .orElseThrow();

        assertEquals("Updated Task Title", savedTask.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());

        // Task creation and status change each generate a notification
        assertEquals(2,
                notificationRepository
                        .countByRecipient_IdAndIsReadFalse(member.getId()));
    }

    // TEST 14: Updating a nonexistent task
    @Test
    void updateTask_ShouldThrowException_WhenTaskDoesNotExist() {

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();

        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.now().plusDays(5));

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.updateTask(999999L, updateRequest)
        );
    }

    // TEST 15: Member successfully updates assigned task status
    @Test
    void updateTask_ShouldAllowMemberToUpdateOwnTaskStatus() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        // Login as assigned member
        authenticate(member);

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();

        updateRequest.setTitle("Attempted Title Change");
        updateRequest.setDescription("Attempted description change");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.now().plusDays(20));

        var response = taskService.updateTask(
                createdTask.getId(), updateRequest);

        // Member can update status
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());

        // Member cannot modify other task fields
        assertEquals("Integration Test Task", response.getTitle());
        assertEquals(
                "Testing TaskService with real database",
                response.getDescription()
        );

        Task savedTask = taskRepository.findById(createdTask.getId())
                .orElseThrow();

        assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());
        assertEquals("Integration Test Task", savedTask.getTitle());
    }

    // TEST 16: Member cannot update another user's task
    @Test
    void updateTask_ShouldDenyMemberUpdatingAnotherUsersTask() {

        User anotherMember = new User();
        anotherMember.setName("Another Member");
        anotherMember.setEmail("updateother@test.com");
        anotherMember.setPassword("password123");
        anotherMember.setRole(UserRole.MEMBER);

        anotherMember = userRepository.save(anotherMember);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(anotherMember);

        projectMemberRepository.save(projectMember);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), anotherMember.getId());

        var createdTask = taskService.createTask(request);

        // Login as original member, not the assigned member
        authenticate(member);

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();

        updateRequest.setTitle("Unauthorized Update");
        updateRequest.setDescription("Should not be updated");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.updateTask(
                        createdTask.getId(), updateRequest)
        );
    }


    // TEST 17: Project manager successfully deletes a task
    @Test
    void deleteTask_ShouldDeleteTask_WhenManagerOwnsProject() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        taskService.deleteTask(createdTask.getId());

        assertFalse(taskRepository.existsById(createdTask.getId()));
    }

    // TEST 18: Deleting a nonexistent task
    @Test
    void deleteTask_ShouldThrowException_WhenTaskDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(999999L)
        );
    }

    // TEST 19: Manager cannot delete another manager's task
    @Test
    void deleteTask_ShouldDenyManager_WhenProjectBelongsToAnotherManager() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("delete-manager@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);

        anotherManager = userRepository.save(anotherManager);

        Project anotherProject = new Project();
        anotherProject.setName("Another Manager Project");
        anotherProject.setDescription("Private project");
        anotherProject.setCreatedBy(anotherManager);

        anotherProject = projectRepository.save(anotherProject);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(anotherProject);
        projectMember.setUser(member);

        projectMemberRepository.save(projectMember);

        TaskRequestDTO request =
                createTaskRequest(anotherProject.getId(), member.getId());

        // Authenticate the owner to create the task
        authenticate(anotherManager);

        var createdTask = taskService.createTask(request);

        // Switch back to original manager
        authenticate(manager);

        Long taskId = createdTask.getId();

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.deleteTask(taskId)
        );

        assertTrue(taskRepository.existsById(taskId));
    }

    // TEST 20: Member cannot delete a task
    @Test
    void deleteTask_ShouldDenyMember() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        authenticate(member);

        Long taskId = createdTask.getId();

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.deleteTask(taskId)
        );

        assertTrue(taskRepository.existsById(taskId));
    }


    // TEST 21: ADMIN can retrieve all tasks with pagination
    @Test
    void getAllTasksWithPagination_ShouldAllowAdmin() {

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-pagination@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.getAllTasksWithPagination(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }

    // TEST 22: Non-admin cannot retrieve all tasks with pagination
    @Test
    void getAllTasksWithPagination_ShouldDenyProjectManager() {

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getAllTasksWithPagination(0, 10)
        );
    }

    // TEST 23: Negative page number should throw exception
    @Test
    void getAllTasksWithPagination_ShouldRejectNegativePage() {

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-negative-page@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getAllTasksWithPagination(-1, 10)
        );
    }

    // TEST 24: Zero page size should throw exception
    @Test
    void getAllTasksWithPagination_ShouldRejectZeroSize() {

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-zero-size@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getAllTasksWithPagination(0, 0)
        );
    }


    // TEST 25: Admin can filter tasks by status
    @Test
    void getTasksByStatus_ShouldReturnTasksForAdmin() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-status@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.getTasksByStatus(
                TaskStatus.TODO, 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(TaskStatus.TODO, response.getContent().get(0).getStatus());
    }

    // TEST 26: Project manager can filter own project tasks by status
    @Test
    void getTasksByStatus_ShouldReturnManagerOwnProjectTasks() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        var response = taskService.getTasksByStatus(
                TaskStatus.TODO, 0, 10);

        assertEquals(1, response.getTotalElements());
    }

    // TEST 27: Admin can filter tasks by priority
    @Test
    void getTasksByPriority_ShouldReturnTasksForAdmin() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-priority@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setPriority(TaskPriority.HIGH);

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.getTasksByPriority(
                TaskPriority.HIGH, 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(TaskPriority.HIGH,
                response.getContent().get(0).getPriority());
    }

    // TEST 28: Member cannot filter all tasks by status
    @Test
    void getTasksByStatus_ShouldDenyMember() {

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getTasksByStatus(
                        TaskStatus.TODO, 0, 10)
        );
    }


    // TEST 29: Admin can search tasks by title
    @Test
    void searchTasksByTitle_ShouldReturnMatchingTasksForAdmin() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-search@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setTitle("Java Backend Development");

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.searchTasksByTitle(
                "Java Backend", 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Java Backend Development",
                response.getContent().get(0).getTitle()
        );
    }

    // TEST 30: Project manager can search tasks in own projects
    @Test
    void searchTasksByTitle_ShouldReturnManagerOwnTasks() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setTitle("Spring Boot Task");

        taskService.createTask(request);

        var response = taskService.searchTasksByTitle(
                "Spring Boot", 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Spring Boot Task",
                response.getContent().get(0).getTitle()
        );
    }

    // TEST 31: Search with blank title should throw exception
    @Test
    void searchTasksByTitle_ShouldRejectBlankTitle() {

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.searchTasksByTitle("   ", 0, 10)
        );
    }

    // TEST 32: Admin can search and filter tasks together
    @Test
    void searchAndFilterTasks_ShouldReturnMatchingTasksForAdmin() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-combined@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setTitle("Database Integration Task");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.searchAndFilterTasks(
                "Database",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                0,
                10
        );

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Database Integration Task",
                response.getContent().get(0).getTitle()
        );
        assertEquals(TaskStatus.TODO, response.getContent().get(0).getStatus());
        assertEquals(TaskPriority.HIGH, response.getContent().get(0).getPriority());
    }

    // TEST 33: Project manager can search and filter tasks in own projects
    @Test
    void searchAndFilterTasks_ShouldReturnManagerOwnTasks() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setTitle("Spring Boot Development");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);

        taskService.createTask(request);

        var response = taskService.searchAndFilterTasks(
                "Spring Boot",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                0,
                10
        );

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Spring Boot Development",
                response.getContent().get(0).getTitle()
        );
    }

    // TEST 34: Member cannot search all tasks by title
    @Test
    void searchTasksByTitle_ShouldDenyMember() {

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.searchTasksByTitle(
                        "Integration", 0, 10)
        );
    }

    // TEST 35: Member cannot use combined task filters
    @Test
    void searchAndFilterTasks_ShouldDenyMember() {

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.searchAndFilterTasks(
                        "Integration",
                        TaskStatus.TODO,
                        TaskPriority.MEDIUM,
                        0,
                        10)
        );
    }

    // TEST 36: Project manager can filter own tasks by priority
    @Test
    void getTasksByPriority_ShouldReturnManagerOwnTasks() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setPriority(TaskPriority.HIGH);

        taskService.createTask(request);

        var response = taskService.getTasksByPriority(
                TaskPriority.HIGH, 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(
                TaskPriority.HIGH,
                response.getContent().get(0).getPriority()
        );
    }


    // TEST 37: Admin can retrieve all tasks
    @Test
    void getAllTasks_ShouldReturnAllTasksForAdmin() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-alltasks@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.getAllTasks();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Integration Test Task", response.get(0).getTitle());
    }

    // TEST 38: Project manager cannot retrieve all tasks
    @Test
    void getAllTasks_ShouldDenyProjectManager() {

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getAllTasks()
        );
    }

    // TEST 39: Project manager can retrieve only tasks from own projects
    @Test
    void getTasksByUser_ShouldFilterTasksForProjectManager() {

        // Create task in manager's own project
        TaskRequestDTO ownRequest =
                createTaskRequest(project.getId(), member.getId());

        ownRequest.setTitle("Managers Own Task");
        taskService.createTask(ownRequest);

        // Create another manager
        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("manager-filter@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        // Create another manager's project
        Project anotherProject = new Project();
        anotherProject.setName("Other Manager Project");
        anotherProject.setDescription("Another manager's project");
        anotherProject.setCreatedBy(anotherManager);
        anotherProject = projectRepository.save(anotherProject);

        // Add the same member to the other project
        ProjectMember membership = new ProjectMember();
        membership.setProject(anotherProject);
        membership.setUser(member);
        projectMemberRepository.save(membership);

        // Authenticate the other manager to create their task
        authenticate(anotherManager);

        TaskRequestDTO otherRequest =
                createTaskRequest(anotherProject.getId(), member.getId());

        otherRequest.setTitle("Other Managers Task");
        taskService.createTask(otherRequest);

        // Switch back to original project manager
        authenticate(manager);

        var response = taskService.getTasksByUser(member.getId());

        assertEquals(1, response.size());
        assertEquals("Managers Own Task", response.get(0).getTitle());
    }

    // TEST 40: Retrieving tasks for nonexistent user
    @Test
    void getTasksByUser_ShouldThrowException_WhenUserDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTasksByUser(999999L)
        );
    }


    // TEST 41: Member can retrieve a task from their own project
    @Test
    void getTaskById_ShouldAllowProjectMember() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        authenticate(member);

        var response = taskService.getTaskById(createdTask.getId());

        assertNotNull(response);
        assertEquals(createdTask.getId(), response.getId());
        assertEquals("Integration Test Task", response.getTitle());
    }

    // TEST 42: Member cannot retrieve a task from another project
    @Test
    void getTaskById_ShouldDenyNonProjectMember() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("manager-task-access@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project anotherProject = new Project();
        anotherProject.setName("Private Project");
        anotherProject.setDescription("Another manager's project");
        anotherProject.setCreatedBy(anotherManager);
        anotherProject = projectRepository.save(anotherProject);

        // Add the assigned user to the other project
        ProjectMember membership = new ProjectMember();
        membership.setProject(anotherProject);
        membership.setUser(member);
        projectMemberRepository.save(membership);

        // Authenticate project owner and create task
        authenticate(anotherManager);

        TaskRequestDTO request =
                createTaskRequest(anotherProject.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        // Original member is not a member of this project
        authenticate(manager);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getTaskById(createdTask.getId())
        );
    }

    // TEST 43: Retrieve tasks from a nonexistent project
    @Test
    void getTasksByProject_ShouldThrowException_WhenProjectDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTasksByProject(999999L)
        );
    }

    // TEST 44: Admin can retrieve tasks assigned to a particular user
    @Test
    void getTasksByUser_ShouldAllowAdminToViewUserTasks() {

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-user-tasks@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.getTasksByUser(member.getId());

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Integration Test Task", response.get(0).getTitle());
    }


    // TEST 45: Pagination returns the correct number of tasks
    @Test
    void getAllTasksWithPagination_ShouldReturnCorrectPageSize() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-page-size@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        for (int i = 1; i <= 3; i++) {
            TaskRequestDTO request =
                    createTaskRequest(project.getId(), member.getId());
            request.setTitle("Pagination Task " + i);
            taskService.createTask(request);
        }

        authenticate(admin);

        var response = taskService.getAllTasksWithPagination(0, 2);

        assertEquals(3, response.getTotalElements());
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalPages());
    }

    // TEST 46: Manager's status filter returns only their own project tasks
    @Test
    void getTasksByStatus_ShouldExcludeOtherManagersTasks() {

        taskService.createTask(
                createTaskRequest(project.getId(), member.getId()));

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("manager-status-filter@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project anotherProject = new Project();
        anotherProject.setName("Other Project");
        anotherProject.setDescription("Other manager project");
        anotherProject.setCreatedBy(anotherManager);
        anotherProject = projectRepository.save(anotherProject);

        ProjectMember membership = new ProjectMember();
        membership.setProject(anotherProject);
        membership.setUser(member);
        projectMemberRepository.save(membership);

        authenticate(anotherManager);

        TaskRequestDTO otherRequest =
                createTaskRequest(anotherProject.getId(), member.getId());
        taskService.createTask(otherRequest);

        authenticate(manager);

        var response = taskService.getTasksByStatus(
                TaskStatus.TODO, 0, 10);

        assertEquals(1, response.getTotalElements());
    }

    // TEST 47: Title search is case-insensitive
    @Test
    void searchTasksByTitle_ShouldIgnoreTitleCase() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-case-search@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());
        request.setTitle("Spring Boot Integration");

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.searchTasksByTitle(
                "sPrInG bOoT", 0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Spring Boot Integration",
                response.getContent().get(0).getTitle()
        );
    }

    // TEST 48: Combined search returns empty result when no task matches
    @Test
    void searchAndFilterTasks_ShouldReturnEmptyPageWhenNoMatch() {

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-no-match@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());
        request.setTitle("Java Development");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);

        taskService.createTask(request);

        authenticate(admin);

        var response = taskService.searchAndFilterTasks(
                "Python",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                0,
                10
        );

        assertEquals(0, response.getTotalElements());
        assertTrue(response.getContent().isEmpty());
    }


    // TEST 49: Creating a task generates an assignment notification
    @Test
    void createTask_ShouldGenerateAssignmentNotification() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        taskService.createTask(request);

        var notifications = notificationRepository.findAll();

        assertEquals(1, notifications.size());
        assertEquals(
                NotificationType.TASK_ASSIGNED,
                notifications.get(0).getType()
        );
        assertEquals(
                member.getId(),
                notifications.get(0).getRecipient().getId()
        );
    }

    // TEST 50: Manager changing task status generates a notification
    @Test
    void updateTask_ShouldGenerateStatusChangeNotification() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();
        updateRequest.setTitle("Integration Test Task");
        updateRequest.setDescription("Testing status notification");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.MEDIUM);
        updateRequest.setDueDate(LocalDate.now().plusDays(7));

        taskService.updateTask(createdTask.getId(), updateRequest);

        var notifications = notificationRepository.findAll();

        assertEquals(2, notifications.size());
        assertTrue(
                notifications.stream().anyMatch(
                        n -> n.getType() == NotificationType.TASK_STATUS_CHANGED
                )
        );
    }

    // TEST 51: Member changing task status does not generate a status notification
    @Test
    void updateTask_ByMember_ShouldNotGenerateStatusChangeNotification() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        long notificationCountBefore = notificationRepository.count();

        authenticate(member);

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();
        updateRequest.setTitle("Integration Test Task");
        updateRequest.setDescription("Member updating own task");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.MEDIUM);
        updateRequest.setDueDate(LocalDate.now().plusDays(7));

        taskService.updateTask(createdTask.getId(), updateRequest);

        assertEquals(
                notificationCountBefore,
                notificationRepository.count()
        );
    }

    // TEST 52: Updating task details without changing status
// should not generate a status-change notification
    @Test
    void updateTask_WithoutStatusChange_ShouldNotGenerateNotification() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        long notificationCountBefore = notificationRepository.count();

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();
        updateRequest.setTitle("Updated Task Title");
        updateRequest.setDescription("Updated task description");
        updateRequest.setStatus(TaskStatus.TODO);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        taskService.updateTask(createdTask.getId(), updateRequest);

        assertEquals(
                notificationCountBefore,
                notificationRepository.count()
        );

        var updatedTask = taskRepository.findById(createdTask.getId())
                .orElseThrow();

        assertEquals("Updated Task Title", updatedTask.getTitle());
        assertEquals(TaskPriority.HIGH, updatedTask.getPriority());
    }


    // TEST 53: Creating a task with explicit status and priority
// should preserve both values
    @Test
    void createTask_ShouldPreserveProvidedStatusAndPriority() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);

        var response = taskService.createTask(request);

        var savedTask = taskRepository.findById(response.getId())
                .orElseThrow();

        assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());
        assertEquals(TaskPriority.HIGH, savedTask.getPriority());
    }

    @Test
    void createTask_ShouldUseDefaultStatusAndPriority() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        request.setStatus(null);
        request.setPriority(null);

        var response = taskService.createTask(request);

        var savedTask = taskRepository.findById(response.getId())
                .orElseThrow();

        assertEquals(TaskStatus.TODO, savedTask.getStatus());
        assertEquals(TaskPriority.MEDIUM, savedTask.getPriority());
    }

    @Test
    void getAllTasks_ShouldReturnEmptyList_WhenNoTasksExist() {

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-empty-tasks@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        var response = taskService.getAllTasks();

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void createTask_ShouldAllowAdmin() {

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-create-task@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Integration Test Task", response.getTitle());
        assertTrue(taskRepository.existsById(response.getId()));
    }

    @Test
    void updateTask_ShouldAllowAdmin() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-update-task@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO();
        updateRequest.setTitle("Admin Updated Task");
        updateRequest.setDescription("Updated by administrator");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.HIGH);
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        var response = taskService.updateTask(
                createdTask.getId(), updateRequest);

        assertEquals("Admin Updated Task", response.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
        assertEquals(TaskPriority.HIGH, response.getPriority());
    }

    @Test
    void deleteTask_ShouldAllowAdmin() {

        TaskRequestDTO request =
                createTaskRequest(project.getId(), member.getId());

        var createdTask = taskService.createTask(request);

        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin-delete-task@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        taskService.deleteTask(createdTask.getId());

        assertFalse(taskRepository.existsById(createdTask.getId()));
    }

    // TEST 60: Member cannot retrieve another user's tasks
    @Test
    void getTasksByUser_ShouldDenyMemberAccessToOtherUsersTasks() {

        User anotherUser = new User();
        anotherUser.setName("Another Member");
        anotherUser.setEmail("another-member-tasks@test.com");
        anotherUser.setPassword("password123");
        anotherUser.setRole(UserRole.MEMBER);

        User savedAnotherUser = userRepository.save(anotherUser);

        authenticate(member);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getTasksByUser(savedAnotherUser.getId())
        );
    }

    @Test
    void getTaskById_ShouldAllowMemberToViewTaskInTheirProject() {

        TaskResponseDTO createdTask = taskService.createTask(
                createTaskRequest(project.getId(), member.getId())
        );

        authenticate(member);

        TaskResponseDTO result = taskService.getTaskById(createdTask.getId());

        assertNotNull(result);
        assertEquals(createdTask.getId(), result.getId());
    }

    @Test
    void getTaskById_ShouldAllowManagerToViewOwnProjectTask() {

        TaskResponseDTO createdTask = taskService.createTask(
                createTaskRequest(project.getId(), member.getId())
        );

        authenticate(manager);

        TaskResponseDTO result = taskService.getTaskById(createdTask.getId());

        assertNotNull(result);
        assertEquals(createdTask.getId(), result.getId());
    }

    @Test
    void getTaskById_ShouldAllowAdminToViewAnyProjectTask() {

        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager60@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        Project anotherProject = new Project();
        anotherProject.setName("Another Manager Project");
        anotherProject.setDescription("Project for admin access testing");
        anotherProject.setCreatedBy(anotherManager);
        anotherProject = projectRepository.save(anotherProject);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(anotherProject);
        projectMember.setUser(member);
        projectMemberRepository.save(projectMember);

        Task task = new Task();
        task.setTitle("Admin Access Task");
        task.setDescription("Testing admin access");
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(anotherProject);
        task.setAssignedTo(member);

        task = taskRepository.save(task);
        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin60@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        authenticate(admin);

        TaskResponseDTO result = taskService.getTaskById(task.getId());

        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
    }

    @Test
    void getTasksByProject_ShouldAllowAdminToViewAnyProject() {

        // Create another project manager
        User anotherManager = new User();
        anotherManager.setName("Another Manager");
        anotherManager.setEmail("another-manager63@test.com");
        anotherManager.setPassword("password123");
        anotherManager.setRole(UserRole.PROJECT_MANAGER);
        anotherManager = userRepository.save(anotherManager);

        // Create another manager's project
        Project anotherProject = new Project();
        anotherProject.setName("Admin Access Project");
        anotherProject.setDescription("Testing admin project access");
        anotherProject.setCreatedBy(anotherManager);
        anotherProject = projectRepository.save(anotherProject);

        // Add member to the project
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(anotherProject);
        projectMember.setUser(member);
        projectMemberRepository.save(projectMember);

        // Create task directly using repository
        Task task = new Task();
        task.setTitle("Admin Project Task");
        task.setDescription("Testing admin project access");
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(anotherProject);
        task.setAssignedTo(member);

        task = taskRepository.save(task);

        // Create admin user
        User admin = new User();
        admin.setName("Test Admin");
        admin.setEmail("admin63@test.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        // Authenticate as admin
        authenticate(admin);

        // Retrieve tasks from another manager's project
        List<TaskResponseDTO> result =
                taskService.getTasksByProject(anotherProject.getId());

        // Assertions
        assertEquals(1, result.size());
        assertEquals(task.getId(), result.get(0).getId());
    }

}