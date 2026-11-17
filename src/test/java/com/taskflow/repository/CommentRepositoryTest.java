
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to create a user
    private User createUser() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("comment" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.MEMBER);

        return userRepository.saveAndFlush(user);
    }

    // Helper method to create a task
    private Task createTask(User user) {

        Project project = new Project();

        project.setName("Comment Test Project");
        project.setDescription("Project for comment testing");
        project.setCreatedBy(user);

        project = projectRepository.saveAndFlush(project);

        Task task = new Task();

        task.setTitle("Comment Test Task");
        task.setDescription("Task for comment testing");
        task.setProject(project);
        task.setAssignedTo(user);

        return taskRepository.saveAndFlush(task);
    }

    // Helper method to create a comment
    private Comment createComment(Task task, User user) {

        Comment comment = new Comment();

        comment.setContent("This is a test comment");
        comment.setTask(task);
        comment.setUser(user);

        return commentRepository.saveAndFlush(comment);
    }

    // Test 1: Save comment successfully
    @Test
    void saveComment_success() {

        User user = createUser();
        Task task = createTask(user);

        Comment comment = createComment(task, user);

        assertNotNull(comment.getId());
        assertEquals("This is a test comment", comment.getContent());
        assertNotNull(comment.getCreatedAt());
    }

    // Test 2: Find comment by ID
    @Test
    void findById_success() {

        User user = createUser();
        Task task = createTask(user);

        Comment savedComment = createComment(task, user);

        Optional<Comment> result =
                commentRepository.findById(savedComment.getId());

        assertTrue(result.isPresent());
        assertEquals(
                "This is a test comment",
                result.get().getContent()
        );
    }

    // Test 3: Find all comments
    @Test
    void findAll_success() {

        User user = createUser();
        Task task = createTask(user);

        createComment(task, user);
        createComment(task, user);

        List<Comment> comments = commentRepository.findAll();

        assertEquals(2, comments.size());
    }

    // Test 4: Delete comment successfully
    @Test
    void deleteComment_success() {

        User user = createUser();
        Task task = createTask(user);

        Comment savedComment = createComment(task, user);

        Long commentId = savedComment.getId();

        commentRepository.deleteById(commentId);
        commentRepository.flush();

        Optional<Comment> result =
                commentRepository.findById(commentId);

        assertFalse(result.isPresent());
    }

    // Test 5: Find comments by task ID
    @Test
    void findByTask_Id_success() {

        User user = createUser();
        Task task = createTask(user);

        createComment(task, user);
        createComment(task, user);

        List<Comment> comments =
                commentRepository.findByTask_Id(task.getId());

        assertEquals(2, comments.size());
    }

    // Test 6: Find comments by user ID
    @Test
    void findByUser_Id_success() {

        User user = createUser();
        Task task = createTask(user);

        createComment(task, user);
        createComment(task, user);

        List<Comment> comments =
                commentRepository.findByUser_Id(user.getId());

        assertEquals(2, comments.size());
    }
}
