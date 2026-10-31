package com.example.library.mapper;

import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.example.library.dto.AuthorSummary;
import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.entity.Author;
import com.example.library.entity.Book;

@Component
public class BookMapper {

  // Book request to Book entity mapping
  public Book toEntity(BookRequest request, Author author) {
    if (request == null) {
      return null;
    }
    return new Book(
        request.title(),
        request.isbn(),
        request.publishedYear(),
        request.price(),
        author);
  }

  // Update Book entity with Book request mapping
  public void updateEntity(Book book, BookRequest request, Author author) {
    if (book == null || request == null) {
      return;
    }
    book.setTitle(request.title());
    book.setIsbn(request.isbn());
    book.setPublishedYear(request.publishedYear());
    book.setPrice(request.price());
    book.setAuthor(author);
  }

  // Book entity to Book response mapping
  public BookResponse toResponse(Book book) {
    if (book == null) {
      return null;
    }
    AuthorSummary authorSummary = null;
    if (book.getAuthor() != null) {
      authorSummary = new AuthorSummary(
          book.getAuthor().getId(),
          book.getAuthor().getName());
    }

    return new BookResponse(
        book.getId(),
        book.getTitle(),
        book.getIsbn(),
        book.getPublishedYear(),
        book.getPrice().setScale(2, RoundingMode.HALF_UP),
        book.getCreatedAt(),
        authorSummary);
  }
}
