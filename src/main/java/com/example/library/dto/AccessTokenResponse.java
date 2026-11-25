package com.example.library.dto;

public record AccessTokenResponse(
  String accessToken,
  String tokenType,
  long expiresIn
) {}