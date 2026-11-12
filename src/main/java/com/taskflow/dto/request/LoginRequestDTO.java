
package com.taskflow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        description = "Request DTO containing credentials required to authenticate a TaskFlow user."
)
public class LoginRequestDTO {

    @Schema(
            description = "Registered email address of the user",
            example = "rahul@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Schema(
            description = "Password associated with the user's account",
            example = "Rahul@12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
