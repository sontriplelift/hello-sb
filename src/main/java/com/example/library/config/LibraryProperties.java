package com.example.library.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "library")
@Validated
public record LibraryProperties(@Valid Api api) {

  public record Api(
      @NotBlank(message = "library.api.title must not be blank") String title,
      String version,
      String description) {
  }
}