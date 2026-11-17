package com.example.library.dto;

public record TokenWithRefreshResponse (
  String accessToken,
  String tokenType,
  long expiresIn,
  String rawToken,
  long refreshExpiresIn
) {}
