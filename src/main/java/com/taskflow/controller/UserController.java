package com.taskflow.controller;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO response = userService.registerUser(requestDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}