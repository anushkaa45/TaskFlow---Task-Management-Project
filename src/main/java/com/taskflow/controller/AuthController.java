
package com.taskflow.controller;

import com.taskflow.dto.request.LoginRequestDTO;
import com.taskflow.dto.response.AuthResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Public login API - JWT not required
    @Operation(
            summary = "User login",
            description = "Authenticates a user using email and password and returns a JWT access token."
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful. JWT token returned."
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Login request validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "timestamp": "2026-11-12T10:30:00",
                                              "status": 400,
                                              "error": "Validation Failed",
                                              "fieldErrors": {
                                                "email": "Enter a valid email",
                                                "password": "Password is required"
                                              }
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                            {
                                              "timestamp": "2026-11-12T10:30:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Invalid email or password."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Internal Server Error",
                                    value = """
                                            {
                                              "timestamp": "2026-11-12T10:30:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Something went wrong. Please try again later."
                                            }
                                            """
                            )
                    )
            )
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        AuthResponseDTO response = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
