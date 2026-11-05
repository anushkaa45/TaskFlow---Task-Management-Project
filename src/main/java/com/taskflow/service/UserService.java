package com.taskflow.service;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import com.taskflow.exception.ResourceAlreadyExistsException;
import com.taskflow.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}