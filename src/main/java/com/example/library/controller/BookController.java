package com.example.library.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.BookSearchCriteria;
import com.example.library.dto.PageResponse;
import com.example.library.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  // @GetMapping
  // public ResponseEntity<List<BookResponse>> getAllBooks() {
  // return ResponseEntity.ok(bookService.findAll());
  // }

  // @GetMapping("/join-fetch")
  // public ResponseEntity<List<BookResponse>> getAllBooksJoinFetch() {
  // return ResponseEntity.ok(bookService.findAllJoinFetch());
  // }

  @GetMapping
  public ResponseEntity<PageResponse<BookResponse>> getBooks(
      BookSearchCriteria criteria,
      @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(
        bookService.searchBooks(criteria, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
    BookResponse book = bookService.findById(id);
    return ResponseEntity.ok(book);
  }

  @PostMapping
  public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest bookRequest) {
    BookResponse createdBook = bookService.create(bookRequest);
    URI location = URI.create(String.format("/api/books/%s", createdBook.id()));
    return ResponseEntity.created(location).body(createdBook);
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
    BookResponse updatedBook = bookService.update(id, bookRequest);
    return ResponseEntity.ok(updatedBook);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    bookService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
