
package com.taskflow.controller;

import com.taskflow.dto.request.UserRequestDTO;
import com.taskflow.dto.request.UserUpdateRequestDTO;
import com.taskflow.dto.response.UserResponseDTO;
import com.taskflow.dto.response.ErrorResponseDTO;
import com.taskflow.service.UserService;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Reusable error examples
    private static final String EXAMPLE_400 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 400,
              "error": "Validation Failed",
              "fieldErrors": {
                "name": "Name is required",
                "email": "Enter a valid email"
              }
            }
            """;

    private static final String EXAMPLE_401 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 401,
              "error": "Unauthorized",
              "message": "Authentication is required. Please provide a valid JWT token."
            }
            """;

    private static final String EXAMPLE_403 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 403,
              "error": "Forbidden",
              "message": "You do not have permission to access this resource."
            }
            """;

    private static final String EXAMPLE_404 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "User not found with id: 10"
            }
            """;

    private static final String EXAMPLE_409 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 409,
              "error": "Conflict",
              "message": "Email already exists"
            }
            """;

    private static final String EXAMPLE_500 = """
            {
              "timestamp": "2026-11-12T10:30:00",
              "status": 500,
              "error": "Internal Server Error",
              "message": "Something went wrong. Please try again later."
            }
            """;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Public registration
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Registration is public and does not require JWT authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
                                            value = EXAMPLE_400
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Request Body",
                                            value = """
                                                    {
                                                      "timestamp": "2026-11-12T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Invalid request body. Check your JSON values, dates, and enum fields."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Duplicate Email",
                                    value = EXAMPLE_409
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
                                    value = EXAMPLE_500
                            )
                    )
            )
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO response = userService.registerUser(requestDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Only ADMIN can view all users
    @Operation(
            summary = "Get all users",
            description = "Retrieves all registered users. Only administrators can access this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        List<UserResponseDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    // Only ADMIN can view a user by ID
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves details of a specific user using their ID. Admin access required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id) {

        UserResponseDTO user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    // Only ADMIN can update a user
    @Operation(
            summary = "Update user",
            description = "Updates an existing user's details. Only administrators can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
                                            value = EXAMPLE_400
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Request Body",
                                            value = """
                                                    {
                                                      "timestamp": "2026-11-12T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Invalid request body. Check your JSON values, dates, and enum fields."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO requestDTO) {

        UserResponseDTO updatedUser =
                userService.updateUser(id, requestDTO);

        return ResponseEntity.ok(updatedUser);
    }

    // Only ADMIN can delete a user
    @Operation(
            summary = "Delete user",
            description = "Deletes an existing user account. Only administrators can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_404)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // Only ADMIN can search users by email
    @Operation(
            summary = "Get user by email",
            description = "Searches for a registered user using their email address. Admin access required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_401)
                    )
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_403)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-11-12T10:30:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with email: user@example.com"
                                    }
                                    """)
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = EXAMPLE_500)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable String email) {

        UserResponseDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user);
    }
}
