package com.example.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.library.entity.RefreshToken;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllByUsername(String username);

  List<RefreshToken> findAllByUsernameAndRevokedAtIsNull(String username);

  boolean existsByTokenHash(String tokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT rt
      FROM RefreshToken rt
      WHERE rt.tokenHash = :tokenHash
      """)
  Optional<RefreshToken> findByTokenHashForUpdate(
      @Param("tokenHash") String tokenHash);
}