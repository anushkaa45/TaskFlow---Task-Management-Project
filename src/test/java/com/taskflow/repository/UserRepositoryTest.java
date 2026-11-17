
package com.taskflow.repository;

import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Helper method to create a user
    private User createUser() {

        User user = new User();

        user.setName("Test User");
        user.setEmail("user" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.MEMBER);

        return userRepository.saveAndFlush(user);
    }

    // Test 1: Save user successfully
    @Test
    void saveUser_success() {

        User user = createUser();

        assertNotNull(user.getId());
        assertEquals("Test User", user.getName());
        assertEquals(UserRole.MEMBER, user.getRole());
    }

    // Test 2: Find user by ID
    @Test
    void findById_success() {

        User savedUser = createUser();

        Optional<User> result =
                userRepository.findById(savedUser.getId());

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
    }

    // Test 3: Find all users
    @Test
    void findAll_success() {

        createUser();
        createUser();

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
    }

    // Test 4: Delete user successfully
    @Test
    void deleteUser_success() {

        User savedUser = createUser();

        Long userId = savedUser.getId();

        userRepository.deleteById(userId);
        userRepository.flush();

        Optional<User> result =
                userRepository.findById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_success() {

        User savedUser = createUser();

        Optional<User> result =
                userRepository.findByEmail(savedUser.getEmail());

        assertTrue(result.isPresent());
        assertEquals(savedUser.getEmail(), result.get().getEmail());
    }

    @Test
    void existsByEmail_success() {

        User savedUser = createUser();

        boolean exists =
                userRepository.existsByEmail(savedUser.getEmail());

        assertTrue(exists);

        boolean notExists =
                userRepository.existsByEmail("unknown@example.com");

        assertFalse(notExists);
    }

    @Test
    void countByRole_success() {

        createUser();
        createUser();

        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin" + UUID.randomUUID() + "@example.com");
        admin.setPassword("password123");
        admin.setRole(UserRole.ADMIN);

        userRepository.saveAndFlush(admin);

        long memberCount =
                userRepository.countByRole(UserRole.MEMBER);

        assertEquals(2, memberCount);
    }
}
