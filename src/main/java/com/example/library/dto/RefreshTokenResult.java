package com.example.library.dto;

public record RefreshTokenResult(
  String rawToken,
  long refreshExpiresIn
) {}
