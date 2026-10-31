package com.example.library.dto;

import java.math.BigDecimal;

public record BookSearchCriteria(
    String title,
    Long authorId,
    BigDecimal minPrice,
    BigDecimal maxPrice
) {
}