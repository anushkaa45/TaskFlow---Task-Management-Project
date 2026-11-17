
package com.taskflow.repository;

import com.taskflow.entity.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to create and save a user
    private User createUser() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("test" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.MEMBER);

        return userRepository.saveAndFlush(user);
    }

    // Helper method to create and save a project
    private Project createProject(User user) {

        Project project = new Project();

        project.setName("Test Project");
        project.setDescription("Project for repository testing");
        project.setCreatedBy(user);

        return projectRepository.saveAndFlush(project);
    }

    // Helper method to create and save a task
    private Task createTask() {

        User user = createUser();

        Project project = createProject(user);

        Task task = new Task();

        task.setTitle("Test Task");
        task.setDescription("Task for repository testing");
        task.setProject(project);
        task.setAssignedTo(user);

        return taskRepository.saveAndFlush(task);
    }

    // Test 1: Save task successfully
    @Test
    void saveTask_success() {

        Task task = createTask();

        assertNotNull(task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
    }

    // Test 2: Find task by ID successfully
    @Test
    void findById_success() {

        Task savedTask = createTask();

        Optional<Task> result =
                taskRepository.findById(savedTask.getId());

        assertTrue(result.isPresent());
        assertEquals("Test Task", result.get().getTitle());
    }

    // Test 3: Find all tasks successfully
    @Test
    void findAll_success() {

        createTask();
        createTask();

        List<Task> tasks = taskRepository.findAll();

        assertEquals(2, tasks.size());
    }

    // Test 4: Delete task successfully
    @Test
    void deleteTask_success() {

        Task savedTask = createTask();

        Long taskId = savedTask.getId();

        taskRepository.deleteById(taskId);
        taskRepository.flush();

        Optional<Task> result =
                taskRepository.findById(taskId);

        assertFalse(result.isPresent());
    }


    // Test 5: Find tasks by project ID
    @Test
    void findByProject_Id_success() {

        User user = createUser();
        Project project = createProject(user);

        Task task1 = new Task();
        task1.setTitle("Task One");
        task1.setProject(project);
        task1.setAssignedTo(user);

        Task task2 = new Task();
        task2.setTitle("Task Two");
        task2.setProject(project);
        task2.setAssignedTo(user);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.flush();

        List<Task> tasks =
                taskRepository.findByProject_Id(project.getId());

        assertEquals(2, tasks.size());
    }

    // Test 6: Find tasks by assigned user ID
    @Test
    void findByAssignedTo_Id_success() {

        Task task = createTask();

        List<Task> tasks =
                taskRepository.findByAssignedTo_Id(
                        task.getAssignedTo().getId()
                );

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    // Test 7: Find tasks by project ID and status
    @Test
    void findByProject_IdAndStatus_success() {

        User user = createUser();
        Project project = createProject(user);

        Task task1 = new Task();
        task1.setTitle("Pending Task");
        task1.setProject(project);
        task1.setAssignedTo(user);
        task1.setStatus(TaskStatus.TODO);

        Task task2 = new Task();
        task2.setTitle("Completed Task");
        task2.setProject(project);
        task2.setAssignedTo(user);
        task2.setStatus(TaskStatus.COMPLETED);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.flush();

        List<Task> tasks =
                taskRepository.findByProject_IdAndStatus(
                        project.getId(), TaskStatus.TODO
                );

        assertEquals(1, tasks.size());
        assertEquals("Pending Task", tasks.get(0).getTitle());
    }

    // Test 8: Find tasks by status with pagination
    @Test
    void findByStatus_success() {

        createTask();
        createTask();

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<Task> result =
                taskRepository.findByStatus(TaskStatus.TODO, pageable);

        assertEquals(2, result.getTotalElements());
    }


    // Test 9: Find tasks by priority with pagination
    @Test
    void findByPriority_success() {

        User user = createUser();
        Project project = createProject(user);

        Task task1 = new Task();
        task1.setTitle("High Priority Task");
        task1.setProject(project);
        task1.setAssignedTo(user);
        task1.setPriority(TaskPriority.HIGH);

        Task task2 = new Task();
        task2.setTitle("Low Priority Task");
        task2.setProject(project);
        task2.setAssignedTo(user);
        task2.setPriority(TaskPriority.LOW);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.flush();

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<Task> result =
                taskRepository.findByPriority(TaskPriority.HIGH, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("High Priority Task",
                result.getContent().get(0).getTitle());
    }

    // Test 10: Search tasks by title ignoring case
    @Test
    void findByTitleContainingIgnoreCase_success() {

        User user = createUser();
        Project project = createProject(user);

        Task task = new Task();
        task.setTitle("Spring Boot Testing");
        task.setProject(project);
        task.setAssignedTo(user);

        taskRepository.saveAndFlush(task);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<Task> result =
                taskRepository.findByTitleContainingIgnoreCase(
                        "spring boot", pageable
                );

        assertEquals(1, result.getTotalElements());
    }

    // Test 11: Find tasks by title, status and priority
    @Test
    void findByTitleStatusAndPriority_success() {

        User user = createUser();
        Project project = createProject(user);

        Task task = new Task();
        task.setTitle("Java Testing");
        task.setProject(project);
        task.setAssignedTo(user);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        taskRepository.saveAndFlush(task);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<Task> result =
                taskRepository
                        .findByTitleContainingIgnoreCaseAndStatusAndPriority(
                                "Java",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                pageable
                        );

        assertEquals(1, result.getTotalElements());
    }

    // Test 12: Count tasks by status
    @Test
    void countByStatus_success() {

        User user = createUser();
        Project project = createProject(user);

        Task task1 = new Task();
        task1.setTitle("Task One");
        task1.setProject(project);
        task1.setAssignedTo(user);
        task1.setStatus(TaskStatus.TODO);

        Task task2 = new Task();
        task2.setTitle("Task Two");
        task2.setProject(project);
        task2.setAssignedTo(user);
        task2.setStatus(TaskStatus.TODO);

        Task task3 = new Task();
        task3.setTitle("Task Three");
        task3.setProject(project);
        task3.setAssignedTo(user);
        task3.setStatus(TaskStatus.COMPLETED);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.flush();

        long count = taskRepository.countByStatus(TaskStatus.TODO);

        assertEquals(2, count);
    }

    // Helper method to create tasks with custom values
    private Task createTask(
            User user,
            Project project,
            String title,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate
    ) {
        Task task = new Task();

        task.setTitle(title);
        task.setProject(project);
        task.setAssignedTo(user);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);

        return taskRepository.saveAndFlush(task);
    }


    // Test 13: Count tasks by priority
    @Test
    void countByPriority_success() {

        User user = createUser();
        Project project = createProject(user);

        createTask(user, project, "High Task",
                TaskStatus.TODO, TaskPriority.HIGH, null);

        createTask(user, project, "Low Task",
                TaskStatus.TODO, TaskPriority.LOW, null);

        createTask(user, project, "Another High Task",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        long count = taskRepository.countByPriority(TaskPriority.HIGH);

        assertEquals(2, count);
    }

    // Test 14: Count tasks assigned to a user
    @Test
    void countByAssignedTo_Id_success() {

        User user = createUser();
        Project project = createProject(user);

        createTask(user, project, "Task One",
                TaskStatus.TODO, TaskPriority.MEDIUM, null);

        createTask(user, project, "Task Two",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        long count =
                taskRepository.countByAssignedTo_Id(user.getId());

        assertEquals(2, count);
    }

    // Test 15: Count tasks assigned to a user by status
    @Test
    void countByAssignedTo_IdAndStatus_success() {

        User user = createUser();
        Project project = createProject(user);

        createTask(user, project, "Pending Task",
                TaskStatus.TODO, TaskPriority.MEDIUM, null);

        createTask(user, project, "Completed Task",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        long count =
                taskRepository.countByAssignedTo_IdAndStatus(
                        user.getId(), TaskStatus.TODO);

        assertEquals(1, count);
    }

    // Test 16: Count overdue tasks assigned to a user,
    // excluding the specified status
    @Test
    void countByAssignedTo_IdAndDueDateBeforeAndStatusNot_success() {

        User user = createUser();
        Project project = createProject(user);

        createTask(user, project, "Overdue Task",
                TaskStatus.TODO, TaskPriority.HIGH,
                LocalDate.now().minusDays(5));

        createTask(user, project, "Completed Old Task",
                TaskStatus.COMPLETED, TaskPriority.HIGH,
                LocalDate.now().minusDays(3));

        createTask(user, project, "Future Task",
                TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5));

        long count =
                taskRepository
                        .countByAssignedTo_IdAndDueDateBeforeAndStatusNot(
                                user.getId(),
                                LocalDate.now(),
                                TaskStatus.COMPLETED
                        );

        assertEquals(1, count);
    }

    // Test 17: Find upcoming tasks assigned to a user,
    // excluding the specified status and ordering by due date
    @Test
    void findByAssignedTo_IdAndDueDateAfterAndStatusNotOrderByDueDateAsc_success() {

        User user = createUser();
        Project project = createProject(user);

        createTask(user, project, "Later Task",
                TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(10));

        createTask(user, project, "Earlier Task",
                TaskStatus.TODO, TaskPriority.HIGH,
                LocalDate.now().plusDays(3));

        createTask(user, project, "Completed Task",
                TaskStatus.COMPLETED, TaskPriority.LOW,
                LocalDate.now().plusDays(2));

        List<Task> tasks =
                taskRepository
                        .findByAssignedTo_IdAndDueDateAfterAndStatusNotOrderByDueDateAsc(
                                user.getId(),
                                LocalDate.now(),
                                TaskStatus.COMPLETED
                        );

        assertEquals(2, tasks.size());
        assertEquals("Earlier Task", tasks.get(0).getTitle());
        assertEquals("Later Task", tasks.get(1).getTitle());
    }

    // Test 18: Count tasks created by a project manager
    @Test
    void countByProject_CreatedBy_Id_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Task One",
                TaskStatus.TODO, TaskPriority.MEDIUM, null);

        createTask(manager, project, "Task Two",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        long count =
                taskRepository.countByProject_CreatedBy_Id(
                        manager.getId());

        assertEquals(2, count);
    }

    // Test 19: Count manager's tasks by status
    @Test
    void countByProject_CreatedBy_IdAndStatus_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Pending Task",
                TaskStatus.TODO, TaskPriority.MEDIUM, null);

        createTask(manager, project, "Completed Task",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        long count =
                taskRepository.countByProject_CreatedBy_IdAndStatus(
                        manager.getId(), TaskStatus.TODO);

        assertEquals(1, count);
    }

    // Test 20: Count manager's tasks by priority
    @Test
    void countByProject_CreatedBy_IdAndPriority_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "High Task One",
                TaskStatus.TODO, TaskPriority.HIGH, null);

        createTask(manager, project, "High Task Two",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        createTask(manager, project, "Low Task",
                TaskStatus.TODO, TaskPriority.LOW, null);

        long count =
                taskRepository.countByProject_CreatedBy_IdAndPriority(
                        manager.getId(), TaskPriority.HIGH);

        assertEquals(2, count);
    }

    // Test 21: Count manager's overdue tasks,
    // excluding the specified status
    @Test
    void countByProject_CreatedBy_IdAndDueDateBeforeAndStatusNot_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Overdue Task",
                TaskStatus.TODO, TaskPriority.HIGH,
                LocalDate.now().minusDays(4));

        createTask(manager, project, "Completed Task",
                TaskStatus.COMPLETED, TaskPriority.MEDIUM,
                LocalDate.now().minusDays(2));

        createTask(manager, project, "Future Task",
                TaskStatus.TODO, TaskPriority.LOW,
                LocalDate.now().plusDays(5));

        long count =
                taskRepository
                        .countByProject_CreatedBy_IdAndDueDateBeforeAndStatusNot(
                                manager.getId(),
                                LocalDate.now(),
                                TaskStatus.COMPLETED
                        );

        assertEquals(1, count);
    }

    // Test 22: Find all tasks created by a project manager
    @Test
    void findByProject_CreatedBy_Id_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Task One",
                TaskStatus.TODO, TaskPriority.MEDIUM, null);

        createTask(manager, project, "Task Two",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        List<Task> tasks =
                taskRepository.findByProject_CreatedBy_Id(
                        manager.getId());

        assertEquals(2, tasks.size());
    }

    // Test 23: Find manager's tasks by title, status and priority
    @Test
    void findByProject_CreatedBy_IdAndTitleStatusAndPriority_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Spring Boot Task",
                TaskStatus.TODO, TaskPriority.HIGH, null);

        createTask(manager, project, "Java Task",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository
                        .findByProject_CreatedBy_IdAndTitleContainingIgnoreCaseAndStatusAndPriority(
                                manager.getId(),
                                "spring",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                pageable
                        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Task",
                result.getContent().get(0).getTitle());
    }

    // Test 24: Find manager's tasks by status
    @Test
    void findByProject_CreatedBy_IdAndStatus_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Pending Task",
                TaskStatus.TODO, TaskPriority.MEDIUM, null);

        createTask(manager, project, "Completed Task",
                TaskStatus.COMPLETED, TaskPriority.HIGH, null);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByProject_CreatedBy_IdAndStatus(
                        manager.getId(),
                        TaskStatus.TODO,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
    }

    // Test 25: Find manager's tasks by priority
    @Test
    void findByProject_CreatedBy_IdAndPriority_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "High Task",
                TaskStatus.TODO, TaskPriority.HIGH, null);

        createTask(manager, project, "Low Task",
                TaskStatus.TODO, TaskPriority.LOW, null);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByProject_CreatedBy_IdAndPriority(
                        manager.getId(),
                        TaskPriority.HIGH,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("High Task",
                result.getContent().get(0).getTitle());
    }

    // Test 26: Find manager's tasks by title
    @Test
    void findByProject_CreatedBy_IdAndTitleContainingIgnoreCase_success() {

        User manager = createUser();
        Project project = createProject(manager);

        createTask(manager, project, "Spring Boot Project",
                TaskStatus.TODO, TaskPriority.HIGH, null);

        createTask(manager, project, "Java Development",
                TaskStatus.COMPLETED, TaskPriority.MEDIUM, null);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository
                        .findByProject_CreatedBy_IdAndTitleContainingIgnoreCase(
                                manager.getId(),
                                "spring",
                                pageable
                        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Project",
                result.getContent().get(0).getTitle());
    }

}
