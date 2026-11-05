package com.example.library.dto;

public record TokenResponse(
  String accessToken,
  String tokenType,
  long expiresIn
) {}