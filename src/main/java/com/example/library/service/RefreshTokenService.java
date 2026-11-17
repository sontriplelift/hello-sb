package com.example.library.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.library.config.LibraryProperties;
import com.example.library.dto.RefreshTokenResult;
import com.example.library.entity.RefreshToken;
import com.example.library.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final LibraryProperties libraryProperties;
  private final SecureRandom secureRandom = new SecureRandom();

  public RefreshTokenService(
      RefreshTokenRepository refreshTokenRepository,
      LibraryProperties libraryProperties) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.libraryProperties = libraryProperties;
  }

  private String generateRawToken() {
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);

    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  private String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(
          rawToken.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    }
  }

  @Transactional
  public RefreshTokenResult create(String username) {
    String rawToken = generateRawToken();
    String tokenHash = hashToken(rawToken);

    Duration refreshExpiration = libraryProperties.security().jwt().refreshExpiration();

    Instant expiresAt = Instant.now().plus(refreshExpiration);

    RefreshToken refreshToken = new RefreshToken(
        tokenHash,
        username,
        expiresAt);

    refreshTokenRepository.save(refreshToken);

    return new RefreshTokenResult(rawToken, refreshExpiration.toSeconds());
  }
}
