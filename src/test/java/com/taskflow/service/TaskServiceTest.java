
package com.taskflow.service;

import com.taskflow.dto.request.TaskRequestDTO;
import com.taskflow.dto.request.TaskUpdateRequestDTO;
import com.taskflow.dto.response.TaskResponseDTO;
import com.taskflow.entity.*;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void memberCannotViewAnotherUsersTasks() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(1L);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getTasksByUser(2L)
        );

        verify(userRepository, never()).existsById(anyLong());
    }

    @Test
    void searchTasksByTitleShouldRejectBlankTitle() {

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.searchTasksByTitle("   ", 0, 10)
        );

        verifyNoInteractions(
                taskRepository,
                authenticatedUserService
        );
    }

    @Test
    void getTasksByStatusShouldRejectNegativePage() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTasksByStatus(
                        null, -1, 10
                )
        );

        verifyNoInteractions(taskRepository);
    }


    @Test
    void adminCanRetrieveAllTasks() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(taskRepository.findAll())
                .thenReturn(List.of());

        List<?> result = taskService.getAllTasks();

        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void memberCannotRetrieveAllTasks() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getAllTasks()
        );

        verifyNoInteractions(taskRepository);
    }

    @Test
    void createTaskShouldThrowExceptionWhenProjectDoesNotExist() {

        TaskRequestDTO request = mock(TaskRequestDTO.class);

        when(request.getProjectId()).thenReturn(99L);

        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(request)
        );

        verify(projectRepository, times(1)).findById(99L);

        verifyNoInteractions(
                taskRepository,
                notificationService
        );
    }


    @Test
    void createTaskShouldFailWhenAssignedUserIsNotProjectMember() {

        TaskRequestDTO request = new TaskRequestDTO();
        request.setProjectId(10L);
        request.setAssignedToUserId(20L);

        Project project = mock(Project.class);
        User user = mock(User.class);

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(userRepository.findById(20L))
                .thenReturn(Optional.of(user));

        when(project.getId()).thenReturn(10L);
        when(user.getId()).thenReturn(20L);

        when(projectMemberRepository.existsByProject_IdAndUser_Id(10L, 20L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(request)
        );

        verify(taskRepository, never()).save(any(Task.class));

        verifyNoInteractions(notificationService);
    }

    @Test
    void createTaskShouldFailWhenAssignedUserDoesNotExist() {

        TaskRequestDTO request = new TaskRequestDTO();
        request.setProjectId(10L);
        request.setAssignedToUserId(20L);

        Project project = mock(Project.class);

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(userRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(request)
        );

        verify(taskRepository, never()).save(any(Task.class));

        verifyNoInteractions(notificationService);
    }

    @Test
    void memberCannotUpdateAnotherUsersTask() {

        Task task = mock(Task.class);
        User assignedUser = mock(User.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(task.getAssignedTo())
                .thenReturn(assignedUser);

        when(assignedUser.getId())
                .thenReturn(20L);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.updateTask(1L, null)
        );

        verify(taskRepository, never()).save(any(Task.class));

        verifyNoInteractions(notificationService);
    }


    @Test
    void adminCanSuccessfullyCreateTask() {

        // Arrange
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Implement login API");
        request.setDescription("Create JWT login functionality");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setProjectId(10L);
        request.setAssignedToUserId(20L);

        Project project = new Project();
        project.setId(10L);
        project.setName("TaskFlow");

        User user = new User();
        user.setId(20L);
        user.setName("Test User");

        Task savedTask = mock(Task.class);

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(userRepository.findById(20L))
                .thenReturn(Optional.of(user));

        when(projectMemberRepository.existsByProject_IdAndUser_Id(10L, 20L))
                .thenReturn(true);

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        when(savedTask.getId()).thenReturn(1L);
        when(savedTask.getTitle()).thenReturn("Implement login API");
        when(savedTask.getDescription())
                .thenReturn("Create JWT login functionality");
        when(savedTask.getStatus()).thenReturn(TaskStatus.TODO);
        when(savedTask.getPriority()).thenReturn(TaskPriority.HIGH);
        when(savedTask.getDueDate())
                .thenReturn(request.getDueDate());
        when(savedTask.getProject()).thenReturn(project);
        when(savedTask.getAssignedTo()).thenReturn(user);
        when(savedTask.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(savedTask.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // Act
        TaskResponseDTO response = taskService.createTask(request);

        // Assert
        assertNotNull(response);
        assertEquals("Implement login API", response.getTitle());

        verify(taskRepository).save(any(Task.class));

        verify(notificationService).createNotification(
                20L,
                "You have been assigned a new task: Implement login API",
                NotificationType.TASK_ASSIGNED
        );
    }

    @Test
    void memberCanSuccessfullyUpdateOwnTaskStatus() {

        // Arrange
        Task task = mock(Task.class);

        User assignedUser = new User();
        assignedUser.setId(10L);
        assignedUser.setName("Test Member");

        Project project = new Project();
        project.setId(10L);
        project.setName("TaskFlow");

        TaskUpdateRequestDTO request = new TaskUpdateRequestDTO();
        request.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(10L);

        when(task.getAssignedTo()).thenReturn(assignedUser);
        when(task.getProject()).thenReturn(project);

        when(task.getId()).thenReturn(1L);
        when(task.getTitle()).thenReturn("Implement login API");
        when(task.getDescription()).thenReturn("Create JWT login");
        when(task.getStatus()).thenReturn(TaskStatus.COMPLETED);
        when(task.getPriority()).thenReturn(TaskPriority.HIGH);
        when(task.getDueDate()).thenReturn(LocalDate.now().plusDays(5));
        when(task.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(task.getUpdatedAt()).thenReturn(LocalDateTime.now());

        when(taskRepository.save(any(Task.class)))
                .thenReturn(task);

        // Act
        TaskResponseDTO response = taskService.updateTask(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(TaskStatus.COMPLETED, response.getStatus());

        verify(task).setStatus(TaskStatus.COMPLETED);
        verify(taskRepository).save(task);

        verifyNoInteractions(notificationService);
    }


    @Test
    void adminCanSuccessfullyDeleteTask() {

        // Arrange
        Task task = mock(Task.class);
        Project project = mock(Project.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(task.getProject()).thenReturn(project);

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void memberCannotDeleteTask() {

        // Arrange
        Task task = mock(Task.class);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        // Act and Assert
        assertThrows(
                AccessDeniedException.class,
                () -> taskService.deleteTask(1L)
        );

        verify(taskRepository, never()).delete(any(Task.class));
    }


    @Test
    void projectManagerCanUpdateTaskDetails() {

        // Arrange
        User manager = new User();
        manager.setId(5L);

        User assignedUser = new User();
        assignedUser.setId(10L);
        assignedUser.setName("Test Member");

        Project project = new Project();
        project.setId(1L);
        project.setName("TaskFlow");
        project.setCreatedBy(manager);

        Task task = new Task();
        task.setTitle("Old title");
        task.setDescription("Old description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.LOW);
        task.setDueDate(LocalDate.now().plusDays(5));
        task.setProject(project);
        task.setAssignedTo(assignedUser);

        TaskUpdateRequestDTO request = new TaskUpdateRequestDTO();
        request.setTitle("Updated title");
        request.setDescription("Updated description");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(10));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponseDTO response = taskService.updateTask(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated title", response.getTitle());
        assertEquals(TaskPriority.HIGH, response.getPriority());

        verify(taskRepository).save(task);
        verifyNoInteractions(notificationService);
    }

    @Test
    void projectManagerStatusChangeShouldCreateNotification() {

        // Arrange
        User manager = new User();
        manager.setId(5L);

        User assignedUser = new User();
        assignedUser.setId(10L);
        assignedUser.setName("Test Member");

        Project project = new Project();
        project.setId(1L);
        project.setName("TaskFlow");
        project.setCreatedBy(manager);

        Task task = new Task();
        task.setTitle("Implement login");
        task.setDescription("Create login API");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(5));
        task.setProject(project);
        task.setAssignedTo(assignedUser);

        TaskUpdateRequestDTO request = new TaskUpdateRequestDTO();
        request.setTitle("Implement login");
        request.setDescription("Create login API");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponseDTO response = taskService.updateTask(1L, request);

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());

        verify(notificationService).createNotification(
                10L,
                "Your task Implement login status has changed to IN_PROGRESS",
                NotificationType.TASK_STATUS_CHANGED
        );
    }


    @Test
    void projectManagerCannotUpdateAnotherManagersTask() {

        // Arrange
        User anotherManager = new User();
        anotherManager.setId(20L);

        Project project = new Project();
        project.setId(1L);
        project.setCreatedBy(anotherManager);

        Task task = new Task();
        task.setProject(project);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        TaskUpdateRequestDTO request = new TaskUpdateRequestDTO();

        // Act and Assert
        assertThrows(
                AccessDeniedException.class,
                () -> taskService.updateTask(1L, request)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTaskShouldThrowExceptionWhenTaskDoesNotExist() {

        // Arrange
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.deleteTask(99L)
        );

        verify(taskRepository, never()).delete(any(Task.class));
    }


    @Test
    void getTaskByIdShouldThrowExceptionWhenTaskDoesNotExist() {

        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L)
        );

        verify(taskRepository).findById(99L);
    }

    @Test
    void memberCannotSearchTasksByStatus() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getTasksByStatus(
                        TaskStatus.TODO, 0, 10
                )
        );

        verifyNoInteractions(taskRepository);
    }

    @Test
    void adminCanRetrieveTasksByStatus() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(taskRepository.findByStatus(
                eq(TaskStatus.TODO), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.getTasksByStatus(
                        TaskStatus.TODO, 0, 10
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByStatus(
                eq(TaskStatus.TODO), any(Pageable.class)
        );
    }

    @Test
    void invalidPageSizeShouldBeRejected() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTasksByStatus(
                        TaskStatus.TODO, 0, 0
                )
        );

        verifyNoInteractions(taskRepository);
    }


    @Test
    void projectManagerCanRetrieveTasksByStatus() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(taskRepository.findByProject_CreatedBy_IdAndStatus(
                eq(5L),
                eq(TaskStatus.TODO),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.getTasksByStatus(
                        TaskStatus.TODO, 0, 10
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByProject_CreatedBy_IdAndStatus(
                eq(5L),
                eq(TaskStatus.TODO),
                any(Pageable.class)
        );
    }

    @Test
    void projectManagerCanSearchTasksByTitle() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(taskRepository
                .findByProject_CreatedBy_IdAndTitleContainingIgnoreCase(
                        eq(5L),
                        eq("login"),
                        any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.searchTasksByTitle(
                        " login ", 0, 10
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository)
                .findByProject_CreatedBy_IdAndTitleContainingIgnoreCase(
                        eq(5L),
                        eq("login"),
                        any(Pageable.class)
                );
    }

    @Test
    void adminCanRetrieveTasksByPriority() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(taskRepository.findByPriority(
                eq(TaskPriority.HIGH),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.getTasksByPriority(
                        TaskPriority.HIGH, 0, 10
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByPriority(
                eq(TaskPriority.HIGH),
                any(Pageable.class)
        );
    }

    @Test
    void adminCanSearchAndFilterTasks() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(taskRepository
                .findByTitleContainingIgnoreCaseAndStatusAndPriority(
                        eq("login"),
                        eq(TaskStatus.TODO),
                        eq(TaskPriority.HIGH),
                        any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.searchAndFilterTasks(
                        " login ",
                        TaskStatus.TODO,
                        TaskPriority.HIGH,
                        0,
                        10
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository)
                .findByTitleContainingIgnoreCaseAndStatusAndPriority(
                        eq("login"),
                        eq(TaskStatus.TODO),
                        eq(TaskPriority.HIGH),
                        any(Pageable.class)
                );
    }


    @Test
    void adminCanSearchTasksByTitle() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("ADMIN");

        when(taskRepository.findByTitleContainingIgnoreCase(
                eq("login"), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.searchTasksByTitle(" login ", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByTitleContainingIgnoreCase(
                eq("login"), any(Pageable.class));
    }

    @Test
    void memberCannotSearchTasksByTitle() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("MEMBER");

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.searchTasksByTitle("login", 0, 10)
        );

        verifyNoInteractions(taskRepository);
    }

    @Test
    void projectManagerCanRetrieveTasksByPriority() {

        when(authenticatedUserService.getCurrentUserRole())
                .thenReturn("PROJECT_MANAGER");

        when(authenticatedUserService.getCurrentUserId())
                .thenReturn(5L);

        when(taskRepository.findByProject_CreatedBy_IdAndPriority(
                eq(5L),
                eq(TaskPriority.HIGH),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<TaskResponseDTO> result =
                taskService.getTasksByPriority(
                        TaskPriority.HIGH, 0, 10
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByProject_CreatedBy_IdAndPriority(
                eq(5L),
                eq(TaskPriority.HIGH),
                any(Pageable.class)
        );
    }

    @Test
    void searchAndFilterTasksShouldRejectBlankTitle() {

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.searchAndFilterTasks(
                        "   ",
                        TaskStatus.TODO,
                        TaskPriority.HIGH,
                        0,
                        10
                )
        );

        verifyNoInteractions(
                taskRepository,
                authenticatedUserService
        );
    }
}
