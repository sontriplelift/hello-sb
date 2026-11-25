package com.example.library.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "refresh_tokens",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_refresh_tokens_token_hash", columnNames = "token_hash")
  }
)
public class RefreshToken {

  @Id 
  @GeneratedValue (strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false, name = "token_hash", length = 64)
  private String tokenHash;

  @Column(nullable = false, name = "username", length = 100)
  private String username;

  @CreationTimestamp
  @Column(nullable = false, name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(nullable = false, name = "expires_at")
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "replaced_by_id")
  private Long replacedById;

  protected RefreshToken() {}

  public RefreshToken(
      String tokenHash,
      String username,
      Instant expiresAt) {
    this.tokenHash = tokenHash;
    this.username = username;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }

  public Long getReplacedById() {
    return replacedById;
  }

  public void setReplacedById(Long replacedById) {
    this.replacedById = replacedById;
  }
}
