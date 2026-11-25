package com.example.library.dto;

public record RefreshResult(
  boolean reuseDetected,
  TokenResponse tokenWithRefreshResponse
) {
  
}
