package com.example.library.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "authors")
public class Author {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "birth_year")
  private Integer birthYear;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "author")
  private List<Book> books = new ArrayList<>();

  public List<Book> getBooks() {
    return books;
  }

  public void addBook(Book book) {
    books.add(book);
    book.setAuthor(this);
  }

  public void removeBook(Book book) {
    books.remove(book);
    book.setAuthor(null);
  }

  // Constructor không tham số
  protected Author() {
  }

  public Author(String name, String email, Integer birthYear) {
    this.name = name;
    this.email = email;
    this.birthYear = birthYear;
  }

  // Getters và setters
  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getBirthYear() {
    return birthYear;
  }

  public void setBirthYear(Integer birthYear) {
    this.birthYear = birthYear;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
