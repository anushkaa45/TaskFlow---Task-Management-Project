
package com.taskflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Response DTO containing user information returned by TaskFlow APIs."
)
public class UserResponseDTO {

    @Schema(
            description = "Unique identifier of the user",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Full name of the user",
            example = "Rahul Sharma"
    )
    private String name;

    @Schema(
            description = "Registered email address of the user",
            example = "rahul@gmail.com"
    )
    private String email;

    @Schema(
            description = "Role assigned to the user",
            example = "PROJECT_MANAGER",
            allowableValues = {"ADMIN", "PROJECT_MANAGER", "MEMBER"}
    )
    private String role;

    @Schema(
            description = "Date and time when the user account was created",
            example = "2026-11-12T10:30:00"
    )
    private LocalDateTime createdAt;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String name, String email,
                           String role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
