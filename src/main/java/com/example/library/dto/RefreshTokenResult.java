package com.example.library.dto;

public record RefreshTokenResult(
  String refreshToken,
  long refreshExpiresIn
) {}
