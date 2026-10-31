package com.example.library.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.library.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

  long countByAuthorId(Long authorId);

  boolean existsByIsbn(String isbn);

  List<Book> findByAuthorId(Long authorId);

  @Override
  @EntityGraph(attributePaths = "author")
  List<Book> findAll();

  @Query("""
      SELECT b
      FROM Book b
      JOIN FETCH b.author
      """)
  List<Book> findAllWithAuthor();

  @EntityGraph(attributePaths = "author")
  @Query(value = """
      SELECT b
      FROM Book b
      WHERE (:title IS NULL
             OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) ESCAPE '!')
        AND (:authorId IS NULL OR b.author.id = :authorId)
        AND (:minPrice IS NULL OR b.price >= :minPrice)
        AND (:maxPrice IS NULL OR b.price <= :maxPrice)
      """, countQuery = """
      SELECT COUNT(b)
      FROM Book b
      WHERE (:title IS NULL
             OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) ESCAPE '!')
        AND (:authorId IS NULL OR b.author.id = :authorId)
        AND (:minPrice IS NULL OR b.price >= :minPrice)
        AND (:maxPrice IS NULL OR b.price <= :maxPrice)
      """)
  Page<Book> searchBooks(
      @Param("title") String title,
      @Param("authorId") Long authorId,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable);
}
