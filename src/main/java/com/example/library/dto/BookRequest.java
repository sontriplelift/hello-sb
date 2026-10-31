package com.example.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BookRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @NotBlank(message = "ISBN is required")
    @Pattern(
      regexp = "^(\\d{10}|\\d{13})$",
      message = "ISBN must contain exactly 10 or 13 digits"
    )
    String isbn,

    @Min(value = 1000, message = "Published year must be greater than or equal to 1000")
    Integer publishedYear,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    @Digits(integer = 8, fraction = 2,
        message = "Price must have at most 8 integer digits and 2 decimal places")
    BigDecimal price,

    @NotNull(message = "Author ID is required")
    @Positive(message = "Author ID must be greater than 0")
    Long authorId
) {
}