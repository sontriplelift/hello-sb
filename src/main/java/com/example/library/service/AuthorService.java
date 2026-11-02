package com.example.library.service;

import java.time.Year;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.library.dto.AuthorRequest;
import com.example.library.dto.AuthorResponse;
import com.example.library.dto.PageResponse;
import com.example.library.entity.Author;
import com.example.library.exception.BusinessException;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.InvalidRequestException;
import com.example.library.exception.ResourceInUseException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;

@Service
public class AuthorService {

  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;
  private final AuthorMapper authorMapper;

  private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);

  public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository, AuthorMapper authorMapper) {
    this.authorRepository = authorRepository;
    this.bookRepository = bookRepository;
    this.authorMapper = authorMapper;
  }

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "id", "name", "email", "birthYear", "createdAt");

  private void validateSort(Pageable pageable) {
    for (Sort.Order order : pageable.getSort()) {
      if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
        throw new InvalidRequestException(
            "Invalid sort field: " + order.getProperty());
      }
    }
  }

  public Author getAuthorById(Long id) {
    return authorRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Author", id));
  }

  private void validateBirthYear(Integer birthYear) {
    if (birthYear != null && birthYear > Year.now().getValue()) {
      throw new BusinessException("Birth year cannot be in the future: " + birthYear);
    }
  }

  @Transactional(readOnly = true)
  public List<AuthorResponse> findAll() {
    return authorRepository.findAll().stream()
        .map(authorMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<AuthorResponse> searchAuthors(
      String name,
      Pageable pageable) {
    
    logger.debug(
        "Searching authors with name={}, page={}, size={}, sort={}",
        name,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        pageable.getSort()
    );

    validateSort(pageable);
    
    Page<Author> authorPage;

    if (name == null || name.isBlank()) {
      authorPage = authorRepository.findAll(pageable);
    } else {
      authorPage = authorRepository.findByNameContainingIgnoreCase(
          name.trim(),
          pageable);
    }

    Page<AuthorResponse> responsePage = authorPage.map(authorMapper::toResponse);

    return PageResponse.from(responsePage);
  }

  @Transactional(readOnly = true)
  public AuthorResponse findById(Long id) {
    Author author = getAuthorById(id);
    return authorMapper.toResponse(author);
  }

  @Transactional
  public AuthorResponse create(AuthorRequest authorRequest) {
    validateBirthYear(authorRequest.birthYear());

    if (authorRepository.existsByEmail(authorRequest.email())) {
      throw new DuplicateResourceException("Email already exists: " + authorRequest.email());
    }
    Author savedAuthor = authorRepository.save(authorMapper.toEntity(authorRequest));

    logger.info("Created author id={}", savedAuthor.getId());
    
    return authorMapper.toResponse(savedAuthor);
  }

  @Transactional
  public AuthorResponse update(Long id, AuthorRequest authorRequest) {
    validateBirthYear(authorRequest.birthYear());

    Author author = getAuthorById(id);
    String newEmail = authorRequest.email();
    if (!author.getEmail().equals(newEmail) && authorRepository.existsByEmail(newEmail)) {
      throw new DuplicateResourceException("Email already exists: " + newEmail);
    }

    authorMapper.updateEntity(author, authorRequest);
    logger.info("Updated author id={}", id);
    return authorMapper.toResponse(author);
  }

  @Transactional
  public void delete(Long id) {
    Author existingAuthor = getAuthorById(id);
    long bookCount = bookRepository.countByAuthorId(id);
    if (bookCount > 0) {
      throw new ResourceInUseException(
          "Cannot delete author with associated books. Author ID: " + id + " has " + bookCount + " book(s)");
    }
    authorRepository.delete(existingAuthor);
    logger.info("Deleted author id={}", id);
  }
}
