package com.example.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.library.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllByUsername(String username);

  List<RefreshToken> findAllByUsernameAndRevokedAtIsNull(String username);

  boolean existsByTokenHash(String tokenHash);
}