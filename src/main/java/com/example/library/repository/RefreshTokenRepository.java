package com.example.library.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

  // Chỉ lấy username, KHÔNG nạp entity vào persistence context.
  // Nhờ vậy entity sẽ được nạp lần đầu SAU khi đã khoá, nên không bị trạng thái cũ.
  @Query("""
      SELECT rt.username
      FROM RefreshToken rt
      WHERE rt.tokenHash = :tokenHash
      """)
  Optional<String> findUsernameByTokenHash(
      @Param("tokenHash") String tokenHash);

  // Khoá (SELECT ... FOR UPDATE) mọi token còn hiệu lực của user.
  // ORDER BY id: mọi transaction khoá theo cùng thứ tự, tránh deadlock.
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT rt
      FROM RefreshToken rt
      WHERE rt.username = :username
        AND rt.revokedAt IS NULL
      ORDER BY rt.id
      """)
  List<RefreshToken> lockActiveTokensByUsername(
      @Param("username") String username);

  // Bulk update: chạy thẳng xuống DB, bỏ qua persistence context.
  // flushAutomatically: ghi các thay đổi đang chờ xuống DB trước khi chạy.
  // clearAutomatically: xoá persistence context sau khi chạy, để không còn entity
  // cũ trong bộ nhớ.
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      UPDATE RefreshToken rt
      SET rt.revokedAt = :now
      WHERE rt.username = :username
        AND rt.revokedAt IS NULL
      """)
  int revokeAllActiveByUsername(
      @Param("username") String username,
      @Param("now") Instant now);
}