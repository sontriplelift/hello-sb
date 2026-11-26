package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "refreshToken must not be blank") 
    String refreshToken) {
}