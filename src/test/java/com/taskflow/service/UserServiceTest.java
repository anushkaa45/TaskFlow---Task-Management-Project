package com.taskflow.service;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.request.UserUpdateRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import com.taskflow.exception.ResourceAlreadyExistsException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsAdmin() {
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Rahul");
        user.setEmail("rahul@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.MEMBER);
        return user;
    }

    private UserRequestDTO createUserRequest() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Rahul");
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul@12345");
        return request;
    }

    private UserUpdateRequestDTO createUpdateRequest() {
        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Rahul");
        request.setEmail("updated@gmail.com");
        return request;
    }

    // 1. Successful registration

    @Test
    void registerUserSuccessfully() {

        UserRequestDTO request = createUserRequest();

        when(userRepository.existsByEmail("rahul@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Rahul@12345"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserResponseDTO response =
                userService.registerUser(request);

        assertNotNull(response);
        assertEquals("Rahul", response.getName());
        assertEquals("rahul@gmail.com", response.getEmail());

        verify(passwordEncoder).encode("Rahul@12345");
        verify(userRepository).save(any(User.class));
    }

    // 2. Registration with existing email

    @Test
    void registerUserThrowsExceptionWhenEmailAlreadyExists() {

        UserRequestDTO request = createUserRequest();

        when(userRepository.existsByEmail("rahul@gmail.com"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // 3. Admin retrieves all users

    @Test
    void adminCanGetAllUsers() {

        authenticateAsAdmin();

        User user = createUser();

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserResponseDTO> response =
                userService.getAllUsers();

        assertEquals(1, response.size());
        assertEquals("Rahul", response.get(0).getName());

        verify(userRepository).findAll();
    }

    // 4. Non-admin cannot retrieve all users

    @Test
    void memberCannotGetAllUsers() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "member",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AccessDeniedException.class,
                () -> userService.getAllUsers()
        );

        verifyNoInteractions(userRepository);
    }

    // 5. Admin retrieves user by ID

    @Test
    void adminCanGetUserById() {

        authenticateAsAdmin();

        User user = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponseDTO response =
                userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("Rahul", response.getName());

        verify(userRepository).findById(1L);
    }

    // 6. User not found by ID

    @Test
    void getUserByIdThrowsExceptionWhenUserDoesNotExist() {

        authenticateAsAdmin();

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(999L)
        );
    }

    // 7. Admin updates user successfully

    @Test
    void adminCanUpdateUserSuccessfully() {

        authenticateAsAdmin();

        User user = createUser();
        UserUpdateRequestDTO request = createUpdateRequest();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("updated@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response =
                userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("Updated Rahul", response.getName());
        assertEquals("updated@gmail.com", response.getEmail());

        verify(userRepository).save(user);
    }

    // 8. Admin deletes user successfully

    @Test
    void adminCanDeleteUserSuccessfully() {

        authenticateAsAdmin();

        User user = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    // 9. Non-admin cannot get user by ID

    @Test
    void memberCannotGetUserById() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "member",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AccessDeniedException.class,
                () -> userService.getUserById(1L)
        );

        verifyNoInteractions(userRepository);
    }


// 10. Admin can get user by email

    @Test
    void adminCanGetUserByEmail() {

        authenticateAsAdmin();

        User user = createUser();

        when(userRepository.findByEmail("rahul@gmail.com"))
                .thenReturn(Optional.of(user));

        UserResponseDTO response =
                userService.getUserByEmail("rahul@gmail.com");

        assertNotNull(response);
        assertEquals("Rahul", response.getName());
        assertEquals("rahul@gmail.com", response.getEmail());

        verify(userRepository).findByEmail("rahul@gmail.com");
    }


// 11. User not found by email

    @Test
    void getUserByEmailThrowsExceptionWhenUserDoesNotExist() {

        authenticateAsAdmin();

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByEmail("unknown@gmail.com")
        );
    }


// 12. Update user when user does not exist

    @Test
    void updateUserThrowsExceptionWhenUserDoesNotExist() {

        authenticateAsAdmin();

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(
                        999L, createUpdateRequest())
        );

        verify(userRepository, never()).save(any(User.class));
    }


// 13. Update user with an email already used by another user

    @Test
    void updateUserThrowsExceptionWhenEmailBelongsToAnotherUser() {

        authenticateAsAdmin();

        User existingUser = createUser();

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setName("Amit");
        anotherUser.setEmail("updated@gmail.com");
        anotherUser.setRole(UserRole.MEMBER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmail("updated@gmail.com"))
                .thenReturn(Optional.of(anotherUser));

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> userService.updateUser(
                        1L, createUpdateRequest())
        );

        verify(userRepository, never()).save(any(User.class));
    }


// 14. Delete user when user does not exist

    @Test
    void deleteUserThrowsExceptionWhenUserDoesNotExist() {

        authenticateAsAdmin();

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(999L)
        );

        verify(userRepository, never()).delete(any(User.class));
    }


// 15. Unauthenticated user cannot access all users

    @Test
    void unauthenticatedUserCannotGetAllUsers() {

        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> userService.getAllUsers()
        );

        verifyNoInteractions(userRepository);
    }

    // 16. Member cannot update a user

    @Test
    void memberCannotUpdateUser() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "member",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(1L, createUpdateRequest())
        );

        verifyNoInteractions(userRepository);
    }


// 17. Member cannot delete a user

    @Test
    void memberCannotDeleteUser() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "member",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUser(1L)
        );

        verifyNoInteractions(userRepository);
    }


// 18. Member cannot retrieve user by email

    @Test
    void memberCannotGetUserByEmail() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "member",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AccessDeniedException.class,
                () -> userService.getUserByEmail("rahul@gmail.com")
        );

        verifyNoInteractions(userRepository);
    }


// 19. Admin can update user without changing their email

    @Test
    void adminCanUpdateUserWithoutChangingEmail() {

        authenticateAsAdmin();

        User user = createUser();

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Rahul");
        request.setEmail("rahul@gmail.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("rahul@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response =
                userService.updateUser(1L, request);

        assertEquals("Updated Rahul", response.getName());
        assertEquals("rahul@gmail.com", response.getEmail());

        verify(userRepository).save(user);
    }
}