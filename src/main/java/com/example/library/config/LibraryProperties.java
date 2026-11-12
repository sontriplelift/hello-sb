package com.example.library.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "library")
@Validated
public record LibraryProperties(
  @Valid @NotNull Api api,
  @Valid @NotNull Security security
) {

  public record Api(
      @NotBlank(message = "library.api.title must not be blank") 
      String title,
      String version,
      String description) {
  }

  public record Security(
      @NotBlank(message = "library.security.admin-password must not be blank") 
      String adminPassword,

      @NotBlank(message = "library.security.reader-password must not be blank") 
      String readerPassword,

      @Valid @NotNull Jwt jwt
  ) {
  }

  public record Jwt(
    @NotBlank(message = "library.security.jwt.secret must not be blank")
    String secret,

    @NotNull(message = "library.security.jwt.expiration must not be null")
    Duration expiration
  ) {
    @AssertTrue(message = "JWT secret must be at least 32 bytes")
    public boolean isSecretAtLeast32Bytes() {
      return secret != null
        && secret.getBytes(StandardCharsets.UTF_8).length >= 32;
    }

    @AssertTrue(message = "JWT expiration must be positive")
    public boolean isExpirationPositive() {
      return expiration != null
        && !expiration.isZero()
        && !expiration.isNegative();
    }
  }
}