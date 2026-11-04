package com.example.library.service;

import java.math.BigDecimal;
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

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.dto.BookSearchCriteria;
import com.example.library.dto.PageResponse;
import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.exception.BusinessException;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.InvalidRequestException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.BookRepository;
import com.example.library.security.SecurityUtils;

@Service
public class BookService {
  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final AuthorService authorService;

  private static final Logger logger = LoggerFactory.getLogger(BookService.class);

  private void validatePublishedYear(Integer publishedYear) {
    if (publishedYear != null && publishedYear > Year.now().getValue()) {
      throw new BusinessException("Published year cannot be in the future: " + publishedYear);
    }
  }

  public BookService(BookRepository bookRepository, BookMapper bookMapper, AuthorService authorService) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
    this.authorService = authorService;
  }

  private Book getBookById(Long id) {
    return bookRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Book", id));
  }

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "id", "title", "isbn", "publishedYear", "price", "createdAt");

  private void validateSort(Pageable pageable) {
    for (Sort.Order order : pageable.getSort()) {
      if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
        throw new InvalidRequestException(
            "Invalid sort field: " + order.getProperty());
      }
    }
  }

  // Escape ký tự đại diện của LIKE để tìm đúng chữ '%' và '_' người dùng gõ.
  // '!' phải escape trước, vì nó là ký tự escape (khai báo ESCAPE '!' trong query).
  private static String escapeLike(String value) {
    return value
        .replace("!", "!!")
        .replace("%", "!%")
        .replace("_", "!_");
  }

  @Transactional(readOnly = true)
  public List<BookResponse> findAll() {
    return bookRepository.findAll().stream()
        .map(bookMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<BookResponse> findAllJoinFetch() {
    return bookRepository.findAllWithAuthor().stream()
        .map(bookMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public BookResponse findById(Long id) {
    Book book = getBookById(id);
    return bookMapper.toResponse(book);
  }

  @Transactional
  public BookResponse create(BookRequest bookRequest) {
    validatePublishedYear(bookRequest.publishedYear());

    if (bookRepository.existsByIsbn(bookRequest.isbn())) {
      throw new DuplicateResourceException("Book with ISBN " + bookRequest.isbn() + " already exists.");
    }

    Author author = authorService.getAuthorById(bookRequest.authorId());
    Book book = bookMapper.toEntity(bookRequest, author);
    Book savedBook = bookRepository.save(book);

    logger.info("Created book id={} isbn={} author={} by {}", savedBook.getId(), savedBook.getIsbn(), savedBook.getAuthor().getName(), SecurityUtils.getCurrentUsername());

    return bookMapper.toResponse(savedBook);
  }

  @Transactional
  public BookResponse update(Long id, BookRequest bookRequest) {
    validatePublishedYear(bookRequest.publishedYear());

    Book book = getBookById(id);

    if (!bookRequest.isbn().equals(book.getIsbn()) && bookRepository.existsByIsbn(bookRequest.isbn())) {
      throw new DuplicateResourceException("Book with ISBN " + bookRequest.isbn() + " already exists.");
    }

    Author author = authorService.getAuthorById(bookRequest.authorId());

    bookMapper.updateEntity(book, bookRequest, author);

    logger.info("Updated book id={} by {}", id, SecurityUtils.getCurrentUsername());

    return bookMapper.toResponse(book);
  }

  @Transactional
  public void delete(Long id) {
    Book book = getBookById(id);
    bookRepository.delete(book);
    logger.info("Deleted book id={} by {}", id, SecurityUtils.getCurrentUsername());
  }

  @Transactional(readOnly = true)
  public List<BookResponse> findBooksByAuthorId(Long authorId) {
    authorService.getAuthorById(authorId);
    return bookRepository.findByAuthorId(authorId).stream()
        .map(bookMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<BookResponse> searchBooks(
      BookSearchCriteria criteria,
      Pageable pageable) {

    BigDecimal minPrice = criteria.minPrice();
    BigDecimal maxPrice = criteria.maxPrice();
    String title = criteria.title();

    logger.debug(
        "Searching books with criteria: title={}, authorId={}, minPrice={}, maxPrice={}, page={}, size={}, sort={}",
        title,
        criteria.authorId(),
        minPrice,
        maxPrice,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        pageable.getSort()
    );

    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      throw new BusinessException(
          "minPrice must be less than or equal to maxPrice");
    }

    validateSort(pageable);

    if (title != null) {
      title = title.trim();
      if (title.isEmpty()) {
        title = null;
      } else {
        title = escapeLike(title);
      }
    }

    Page<Book> bookPage = bookRepository.searchBooks(
        title,
        criteria.authorId(),
        minPrice,
        maxPrice,
        pageable);

    Page<BookResponse> responsePage = bookPage.map(bookMapper::toResponse);

    return PageResponse.from(responsePage);
  }

}
