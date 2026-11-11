
package com.taskflow.service;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.request.UserUpdateRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import com.taskflow.exception.ResourceAlreadyExistsException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void checkAdminAccess() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN"))) {

            throw new AccessDeniedException(
                    "Only ADMIN can perform this operation"
            );
        }
    }

    private UserResponseDTO mapToResponse(User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(UserRole.MEMBER);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public List<UserResponseDTO> getAllUsers() {

        checkAdminAccess();

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {

        checkAdminAccess();

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        return mapToResponse(user);
    }

    public UserResponseDTO updateUser(
            Long id,
            UserUpdateRequestDTO requestDTO) {

        checkAdminAccess();

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        userRepository.findByEmail(requestDTO.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException(
                                "Email is already registered"
                        );
                    }
                });

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        checkAdminAccess();

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        userRepository.delete(user);
    }

    public UserResponseDTO getUserByEmail(String email) {

        checkAdminAccess();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return mapToResponse(user);
    }
}
