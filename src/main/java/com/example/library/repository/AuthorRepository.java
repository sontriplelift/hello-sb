package com.example.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.library.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
  boolean existsByEmail(String email);

  Page<Author> findByNameContainingIgnoreCase(
      String name,
      Pageable pageable);
}
