
package com.taskflow.repository;

import com.taskflow.entity.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to create a user
    private User createUser() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("project" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.PROJECT_MANAGER);

        return userRepository.saveAndFlush(user);
    }

    // Helper method to create a project
    private Project createProject(User user) {

        Project project = new Project();

        project.setName("Test Project");
        project.setDescription("Project for repository testing");
        project.setCreatedBy(user);

        return projectRepository.saveAndFlush(project);
    }

    // Test 1: Save project successfully
    @Test
    void saveProject_success() {

        User user = createUser();

        Project project = createProject(user);

        assertNotNull(project.getId());
        assertEquals("Test Project", project.getName());
        assertEquals(ProjectStatus.PLANNING, project.getStatus());
    }

    // Test 2: Find project by ID
    @Test
    void findById_success() {

        User user = createUser();

        Project savedProject = createProject(user);

        Optional<Project> result =
                projectRepository.findById(savedProject.getId());

        assertTrue(result.isPresent());
        assertEquals("Test Project", result.get().getName());
    }

    // Test 3: Find all projects
    @Test
    void findAll_success() {

        User user = createUser();

        createProject(user);
        createProject(user);

        List<Project> projects = projectRepository.findAll();

        assertEquals(2, projects.size());
    }

    // Test 4: Delete project successfully
    @Test
    void deleteProject_success() {

        User user = createUser();

        Project savedProject = createProject(user);

        Long projectId = savedProject.getId();

        projectRepository.deleteById(projectId);
        projectRepository.flush();

        Optional<Project> result =
                projectRepository.findById(projectId);

        assertFalse(result.isPresent());
    }

    // Test 5: Count projects by creator ID
    @Test
    void countByCreatedBy_Id_success() {

        User user = createUser();

        createProject(user);
        createProject(user);
        createProject(user);

        long count =
                projectRepository.countByCreatedBy_Id(user.getId());

        assertEquals(3, count);
    }

    // Test 6: Find projects by creator ID
    @Test
    void findByCreatedBy_Id_success() {

        User user = createUser();

        createProject(user);
        createProject(user);

        List<Project> projects =
                projectRepository.findByCreatedBy_Id(user.getId());

        assertEquals(2, projects.size());

        assertTrue(
                projects.stream().allMatch(
                        project -> project.getCreatedBy().getId()
                                .equals(user.getId())
                )
        );
    }
}
