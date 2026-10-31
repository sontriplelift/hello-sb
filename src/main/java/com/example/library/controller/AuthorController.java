package com.example.library.controller;

import java.net.URI;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.AuthorRequest;
import com.example.library.dto.AuthorResponse;
import com.example.library.dto.BookResponse;
import com.example.library.dto.PageResponse;
import com.example.library.service.AuthorService;
import com.example.library.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

  private final AuthorService authorService;
  private final BookService bookService;

  public AuthorController(AuthorService authorService, BookService bookService) {
    this.authorService = authorService;
    this.bookService = bookService;
  }

  // @GetMapping
  // public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
  // return ResponseEntity.ok(authorService.findAll());
  // }

  @GetMapping
  public ResponseEntity<PageResponse<AuthorResponse>> getAuthors(
      @RequestParam(required = false) String name,
      @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(authorService.searchAuthors(name, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
    AuthorResponse author = authorService.findById(id);
    return ResponseEntity.ok(author);
  }

  @PostMapping
  public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
    AuthorResponse createdAuthor = authorService.create(authorRequest);
    URI location = URI.create(String.format("/api/authors/%s", createdAuthor.id()));
    return ResponseEntity.created(location).body(createdAuthor);
  }

  @PutMapping("/{id}")
  public ResponseEntity<AuthorResponse> updateAuthor(@PathVariable Long id,
      @Valid @RequestBody AuthorRequest authorRequest) {
    AuthorResponse updatedAuthor = authorService.update(id, authorRequest);
    return ResponseEntity.ok(updatedAuthor);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
    authorService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/books")
  public ResponseEntity<List<BookResponse>> getBooksByAuthor(@PathVariable Long id) {
    List<BookResponse> books = bookService.findBooksByAuthorId(id);
    return ResponseEntity.ok(books);
  }

}
