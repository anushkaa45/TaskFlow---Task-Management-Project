
package com.taskflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Response DTO returned after successful authentication."
)
public class AuthResponseDTO {

    @Schema(
            description = "JWT access token used to authenticate subsequent API requests",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String token;

    @Schema(
            description = "Authentication scheme used with the access token",
            example = "Bearer"
    )
    private String tokenType;

    @Schema(
            description = "Token validity duration in seconds",
            example = "86400"
    )
    private Long expiresIn;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String tokenType, Long expiresIn) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }
}
