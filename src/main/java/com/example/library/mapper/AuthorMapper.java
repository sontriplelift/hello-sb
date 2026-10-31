package com.example.library.mapper;

import org.springframework.stereotype.Component;

import com.example.library.dto.AuthorRequest;
import com.example.library.dto.AuthorResponse;
import com.example.library.entity.Author;

@Component
public class AuthorMapper {

  // Author request to Author entity mapping
  public Author toEntity(AuthorRequest request) {
    if (request == null) {
      return null;
    }
    Author author = new Author(
        request.name(),
        request.email(),
        request.birthYear());
    return author;
  }

  // Update Author entity with Author request mapping
  public void updateEntity(Author author, AuthorRequest request) {
    if (author == null || request == null) {
      return;
    }
    author.setName(request.name());
    author.setEmail(request.email());
    author.setBirthYear(request.birthYear());
  }

  // Author entity to Author response mapping
  public AuthorResponse toResponse(Author author) {
    if (author == null) {
      return null;
    }
    return new AuthorResponse(
        author.getId(),
        author.getName(),
        author.getEmail(),
        author.getBirthYear(),
        author.getCreatedAt());
  }
}
