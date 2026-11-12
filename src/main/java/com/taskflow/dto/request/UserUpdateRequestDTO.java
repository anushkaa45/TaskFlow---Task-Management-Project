
package com.taskflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Request DTO used to update an existing user's details."
)
public class UserUpdateRequestDTO {

    @Schema(
            description = "Updated full name of the user",
            example = "Rahul Sharma",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100,
            message = "Name must be between 3 and 100 characters")
    private String name;

    @Schema(
            description = "Updated email address of the user",
            example = "rahul@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

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
}
