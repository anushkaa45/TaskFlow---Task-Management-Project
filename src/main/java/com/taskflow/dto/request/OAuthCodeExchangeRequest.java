package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OAuthCodeExchangeRequest {

    @NotBlank(message = "Login code is required")
    private String code;

    public OAuthCodeExchangeRequest() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}