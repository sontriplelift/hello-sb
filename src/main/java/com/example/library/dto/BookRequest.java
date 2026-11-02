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

import io.swagger.v3.oas.annotations.media.Schema;

public record BookRequest(

	@NotBlank(message = "Title is required")
	@Size(max = 200, message = "Title must not exceed 200 characters")
	@Schema(
		description = "Book title",
		example = "Java Fundamental"
	)
	String title,

	@NotBlank(message = "ISBN is required")
	@Pattern(
		regexp = "^(\\d{10}|\\d{13})$",
		message = "ISBN must contain exactly 10 or 13 digits"
	)
	@Schema(
		description = "Book ISBN",
		example = "1234567890"
	)
	String isbn,

	@Min(value = 1000, message = "Published year must be greater than or equal to 1000")
	@Schema(
		description = "Published year",
		example = "2020"
	)
	Integer publishedYear,

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
	@Digits(integer = 8, fraction = 2,
			message = "Price must have at most 8 integer digits and 2 decimal places")
	@Schema(
		description = "Book price",
		example = "500000"
	)
	BigDecimal price,

	@NotNull(message = "Author ID is required")
	@Positive(message = "Author ID must be greater than 0")
	@Schema(
		description = "Author id",
		example = "1"
	)
	Long authorId
) {
}