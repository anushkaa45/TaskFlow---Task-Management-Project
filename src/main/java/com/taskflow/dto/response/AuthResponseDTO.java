
package com.taskflow.dto.response;

public class AuthResponseDTO {

    private String token;
    private String tokenType;
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
