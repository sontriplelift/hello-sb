package com.example.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
    @NotBlank(message = "Name is required") 
    @Size(max = 100, message = "Name must not exceed 100 characters") 
    String name,

    @Email(message = "Invalid email format") 
    @NotBlank(message = "Email is required") 
    @Size(max = 150, message = "Email must not exceed 150 characters") 
    String email,

    @Min(value = 1000, message = "Birth year must be greater than or equal to 1000") 
    Integer birthYear
) {
}
