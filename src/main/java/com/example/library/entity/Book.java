package com.example.library.entity;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "books", uniqueConstraints = {
    @UniqueConstraint(name = "uk_books_isbn", columnNames = "isbn")
})
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 13)
  private String isbn;

  @Column(name = "published_year")
  private Integer publishedYear;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private Author author;

  protected Book() {
  }

  public Book(
      String title,
      String isbn,
      Integer publishedYear,
      BigDecimal price,
      Author author) {
    this.title = title;
    this.isbn = isbn;
    this.publishedYear = publishedYear;
    this.price = price;
    this.author = author;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public Integer getPublishedYear() {
    return publishedYear;
  }

  public void setPublishedYear(Integer publishedYear) {
    this.publishedYear = publishedYear;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Author getAuthor() {
    return author;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }
}