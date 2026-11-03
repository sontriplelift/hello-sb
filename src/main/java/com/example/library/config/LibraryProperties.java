package com.example.library.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "library")
@Validated
public record LibraryProperties(
  @Valid @NotNull Api api,
  @Valid Security security
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
      String readerPassword
  ) {
  }
}