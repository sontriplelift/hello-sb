package com.example.library.controller;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
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
import com.example.library.exception.ErrorResponse;
import com.example.library.service.AuthorService;
import com.example.library.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Authors", description = "API quản lý tác giả")
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

  @Operation(summary = "Get list author")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Get list authors successfully"
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @GetMapping
  public ResponseEntity<PageResponse<AuthorResponse>> getAuthors(
      @RequestParam(required = false) String name,
      @ParameterObject @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(authorService.searchAuthors(name, pageable));
  }

  @Operation(summary = "Get author by id")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Get author by id successfully"
    ),
    @ApiResponse(
      responseCode = "404",
      description = "Author not found",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @GetMapping("/{id}")
  public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
    AuthorResponse author = authorService.findById(id);
    return ResponseEntity.ok(author);
  }

  @Operation(summary = "Create author")
  @ApiResponses({
    @ApiResponse(
      responseCode = "201",
      description = "Author created successfully"
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
    @ApiResponse(
      responseCode = "409",
      description = "Email already exists",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @PostMapping
  public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
    AuthorResponse createdAuthor = authorService.create(authorRequest);
    URI location = URI.create(String.format("/api/authors/%s", createdAuthor.id()));
    return ResponseEntity.created(location).body(createdAuthor);
  }

  @Operation(summary = "Update author")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Author updated successfully"
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
    @ApiResponse(
      responseCode = "404",
      description = "Author not found",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
    @ApiResponse(
      responseCode = "409",
      description = "Email already exists",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @PutMapping("/{id}")
  public ResponseEntity<AuthorResponse> updateAuthor(@PathVariable Long id,
      @Valid @RequestBody AuthorRequest authorRequest) {
    AuthorResponse updatedAuthor = authorService.update(id, authorRequest);
    return ResponseEntity.ok(updatedAuthor);
  }

  @Operation(summary = "Delete author")
  @ApiResponses({
    @ApiResponse(
      responseCode = "204",
      description = "Author deleted successfully"
    ),
    @ApiResponse(
      responseCode = "404",
      description = "Author not found",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
    @ApiResponse(
      responseCode = "409",
      description = "Author has book(s)",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
    authorService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get books by author")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Get books by author successfully"
    ),
    @ApiResponse(
      responseCode = "404",
      description = "Author not found",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @GetMapping("/{id}/books")
  public ResponseEntity<List<BookResponse>> getBooksByAuthor(@PathVariable Long id) {
    List<BookResponse> books = bookService.findBooksByAuthorId(id);
    return ResponseEntity.ok(books);
  }

}
