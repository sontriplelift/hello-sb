package com.example.library.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BookResponse(
    Long id,
    String title,
    String isbn,
    Integer publishedYear,
    BigDecimal price,
    Instant createdAt,
    AuthorSummary author
) {
}