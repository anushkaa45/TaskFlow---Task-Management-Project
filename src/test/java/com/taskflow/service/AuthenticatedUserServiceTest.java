package com.taskflow.service;

import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import com.taskflow.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticatedUserService authenticatedUserService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(String email) {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
    private User createUser() {
        User user = new User();
        user.setId(10L);
        user.setName("Rahul");
        user.setEmail("rahul@gmail.com");
        user.setRole(UserRole.MEMBER);
        return user;
    }

    // 1. Get current user successfully

    @Test
    void getCurrentUserSuccessfully() {

        authenticateUser("rahul@gmail.com");

        User user = createUser();

        when(userRepository.findByEmail("rahul@gmail.com"))
                .thenReturn(Optional.of(user));

        User response = authenticatedUserService.getCurrentUser();

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Rahul", response.getName());

        verify(userRepository).findByEmail("rahul@gmail.com");
    }

    // 2. Get current user when authentication is missing

    @Test
    void getCurrentUserThrowsExceptionWhenAuthenticationIsMissing() {

        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalStateException.class,
                () -> authenticatedUserService.getCurrentUser()
        );

        verifyNoInteractions(userRepository);
    }

    // 3. Get current user when authentication is anonymous

    @Test
    void getCurrentUserThrowsExceptionForAnonymousUser() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "anonymousUser",
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalStateException.class,
                () -> authenticatedUserService.getCurrentUser()
        );

        verifyNoInteractions(userRepository);
    }

    // 4. Get current user when user does not exist in database

    @Test
    void getCurrentUserThrowsExceptionWhenUserDoesNotExist() {

        authenticateUser("unknown@gmail.com");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> authenticatedUserService.getCurrentUser()
        );

        verify(userRepository).findByEmail("unknown@gmail.com");
    }

    // 5. Get current user ID

    @Test
    void getCurrentUserIdSuccessfully() {

        authenticateUser("rahul@gmail.com");

        User user = createUser();

        when(userRepository.findByEmail("rahul@gmail.com"))
                .thenReturn(Optional.of(user));

        Long userId = authenticatedUserService.getCurrentUserId();

        assertEquals(10L, userId);
    }

    // 6. Get current user role

    @Test
    void getCurrentUserRoleSuccessfully() {

        authenticateUser("rahul@gmail.com");

        User user = createUser();

        when(userRepository.findByEmail("rahul@gmail.com"))
                .thenReturn(Optional.of(user));

        String role = authenticatedUserService.getCurrentUserRole();

        assertEquals("MEMBER", role);
    }

    // 7. Get current user ID when unauthenticated

    @Test
    void getCurrentUserIdThrowsExceptionWhenUnauthenticated() {

        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalStateException.class,
                () -> authenticatedUserService.getCurrentUserId()
        );

        verifyNoInteractions(userRepository);
    }

    // 8. Get current user role when unauthenticated

    @Test
    void getCurrentUserRoleThrowsExceptionWhenUnauthenticated() {

        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalStateException.class,
                () -> authenticatedUserService.getCurrentUserRole()
        );

        verifyNoInteractions(userRepository);
    }
}