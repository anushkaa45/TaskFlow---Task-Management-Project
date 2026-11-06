package com.taskflow.service;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.request.UserUpdateRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import com.taskflow.exception.ResourceAlreadyExistsException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {


        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        User user = new User();

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());

        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        user.setRole(UserRole.MEMBER);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getCreatedAt()
        );
    }

    public List<UserResponseDTO> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    public UserResponseDTO updateUser(
            Long id,
            UserUpdateRequestDTO requestDTO) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        // 2. Check whether email belongs to another user
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

        return new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getCreatedAt()
        );
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );

        userRepository.delete(user);
    }

    public UserResponseDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}