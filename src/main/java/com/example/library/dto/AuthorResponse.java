package com.example.library.dto;

import java.time.Instant;

public record AuthorResponse(
  Long id,
  String name,
  String email,
  Integer birthYear,
  Instant createdAt
) {

}
