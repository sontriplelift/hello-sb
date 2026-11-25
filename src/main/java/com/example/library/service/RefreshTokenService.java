package com.example.library.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.library.config.LibraryProperties;
import com.example.library.dto.RefreshResult;
import com.example.library.dto.RefreshTokenResult;
import com.example.library.dto.AccessTokenResponse;
import com.example.library.dto.TokenResponse;
import com.example.library.entity.RefreshToken;
import com.example.library.exception.InvalidRefreshTokenException;
import com.example.library.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

  private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

  private final RefreshTokenRepository refreshTokenRepository;
  private final LibraryProperties libraryProperties;
  private final UserDetailsService userDetailsService;
  private final TokenService tokenService;
  private final SecureRandom secureRandom = new SecureRandom();

  public RefreshTokenService(
      RefreshTokenRepository refreshTokenRepository,
      LibraryProperties libraryProperties,
      UserDetailsService userDetailsService,
      TokenService tokenService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.libraryProperties = libraryProperties;
    this.userDetailsService = userDetailsService;
    this.tokenService = tokenService;
  }

  private String generateRawRefreshToken() {
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);

    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  private String hashToken(String rawRefreshToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(
          rawRefreshToken.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    }
  }

  @Transactional
  public RefreshTokenResult create(String username) {
    String rawRefreshToken = generateRawRefreshToken();
    String tokenHash = hashToken(rawRefreshToken);

    Duration refreshExpiration = libraryProperties.security().jwt().refreshExpiration();

    Instant expiresAt = Instant.now().plus(refreshExpiration);

    RefreshToken refreshToken = new RefreshToken(
        tokenHash,
        username,
        expiresAt);

    refreshTokenRepository.save(refreshToken);

    return new RefreshTokenResult(rawRefreshToken, refreshExpiration.toSeconds());
  }

  @Transactional
  public RefreshResult refresh(String rawRefreshToken) {
    String tokenHash = hashToken(rawRefreshToken);

    RefreshToken oldToken = refreshTokenRepository
        .findByTokenHashForUpdate(tokenHash)
        .orElseThrow(InvalidRefreshTokenException::new);

    Instant now = Instant.now();

    // Token đã bị revoke
    if (oldToken.getRevokedAt() != null) {

      // Token đã từng được rotation -> nghi ngờ token bị đánh cắp
      if (oldToken.getReplacedById() != null) {
        revokeAllActiveTokens(oldToken.getUsername(), now);

        logger.warn(
            "Refresh token reuse detected for user: {}. Revoking all active refresh tokens.",
            oldToken.getUsername());

        // Trả kết quả để transaction commit việc revoke
        return new RefreshResult(true, null);
      }

      // Token đã logout hoặc bị thu hồi theo cách khác
      logger.warn(
          "Refresh token already revoked for user: {}.",
          oldToken.getUsername());
      throw new InvalidRefreshTokenException();
    }

    // Token hết hạn
    if (!oldToken.getExpiresAt().isAfter(now)) {
      logger.warn("Refresh token expired for user: {}", oldToken.getUsername());
      throw new InvalidRefreshTokenException();
    }

    // Lấy thông tin user hiện tại, không sử dụng roles từ JWT cũ
    UserDetails userDetails = userDetailsService.loadUserByUsername(
        oldToken.getUsername());

    if (!userDetails.isEnabled()
        || !userDetails.isAccountNonExpired()
        || !userDetails.isAccountNonLocked()
        || !userDetails.isCredentialsNonExpired()) {
      throw new InvalidRefreshTokenException();
    }

    // Tạo refresh token mới
    RefreshTokenResult newRefreshTokenResult = create(
        userDetails.getUsername());

    // Tìm bản ghi token mới vừa được tạo
    RefreshToken newToken = refreshTokenRepository
        .findByTokenHash(
            hashToken(newRefreshTokenResult.refreshToken()))
        .orElseThrow(() -> new IllegalStateException(
            "New refresh token was not persisted"));

    // Revoke token cũ và liên kết với token mới
    oldToken.setRevokedAt(now);
    oldToken.setReplacedById(newToken.getId());

    // Tạo access token mới từ UserDetails hiện tại
    AccessTokenResponse accessTokenResponse = tokenService.generateToken(
        userDetails);

    TokenResponse response = new TokenResponse(
        accessTokenResponse.accessToken(),
        accessTokenResponse.tokenType(),
        accessTokenResponse.expiresIn(),
        newRefreshTokenResult.refreshToken(),
        newRefreshTokenResult.refreshExpiresIn());

    logger.info("Refresh token rotated successfully for user: {}", userDetails.getUsername());

    return new RefreshResult(false, response);
  }

  private void revokeAllActiveTokens(String username, Instant now) {
    List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUsernameAndRevokedAtIsNull(username);

    activeTokens.forEach(token -> token.setRevokedAt(now));
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    String tokenHash = hashToken(rawRefreshToken);

    refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
        .ifPresentOrElse(
            token -> {
              if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());

                logger.info("Logout successful for user: {}",
                    token.getUsername());
              }
            },
            () -> logger.debug("Logout requested with unknown refresh token"));
  }
}
