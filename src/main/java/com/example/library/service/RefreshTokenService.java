package com.example.library.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

  private Duration refreshExpiration() {
    return libraryProperties.security().jwt().refreshExpiration();
  }

  // Lưu hash của token vào DB và trả về entity đã lưu.
  // Với IDENTITY, câu INSERT chạy ngay trong save(), nên entity trả về đã có id.
  private RefreshToken saveNewToken(String username, String rawRefreshToken) {
    Instant expiresAt = Instant.now().plus(refreshExpiration());

    RefreshToken refreshToken = new RefreshToken(
        hashToken(rawRefreshToken),
        username,
        expiresAt);

    return refreshTokenRepository.save(refreshToken);
  }

  @Transactional
  public RefreshTokenResult create(String username) {
    String rawRefreshToken = generateRawRefreshToken();

    saveNewToken(username, rawRefreshToken);

    return new RefreshTokenResult(rawRefreshToken, refreshExpiration().toSeconds());
  }

  // Code cũ (trước khi tách saveNewToken):
  // @Transactional
  // public RefreshTokenResult create(String username) {
  //   String rawRefreshToken = generateRawRefreshToken();
  //   String tokenHash = hashToken(rawRefreshToken);
  //   Duration refreshExpiration = libraryProperties.security().jwt().refreshExpiration();
  //   Instant expiresAt = Instant.now().plus(refreshExpiration);
  //   RefreshToken refreshToken = new RefreshToken(tokenHash, username, expiresAt);
  //   refreshTokenRepository.save(refreshToken);
  //   return new RefreshTokenResult(rawRefreshToken, refreshExpiration.toSeconds());
  // }

  @Transactional
  public RefreshResult refresh(String rawRefreshToken) {
    String tokenHash = hashToken(rawRefreshToken);

    // Code cũ: chỉ khoá đúng dòng của token này.
    // Hai request với 2 token KHÁC NHAU của cùng user (A bị dùng lại, B đang rotation)
    // không chặn nhau, nên token mới C sinh ra từ B có thể thoát khỏi đợt thu hồi.
    // RefreshToken oldRefreshToken = refreshTokenRepository
    //     .findByTokenHashForUpdate(tokenHash)
    //     .orElseThrow(InvalidRefreshTokenException::new);

    // B1: chỉ lấy username, chưa nạp entity và chưa khoá
    String username = refreshTokenRepository
        .findUsernameByTokenHash(tokenHash)
        .orElseThrow(InvalidRefreshTokenException::new);

    // B2: khoá mọi token còn hiệu lực của user.
    // Mọi request refresh của cùng một user giờ chạy lần lượt.
    refreshTokenRepository.lockActiveTokensByUsername(username);

    // B3: nạp token SAU khi đã có khoá, nên đọc được trạng thái mới nhất
    RefreshToken oldRefreshToken = refreshTokenRepository
        .findByTokenHash(tokenHash)
        .orElseThrow(InvalidRefreshTokenException::new);

    Instant now = Instant.now();

    // Token đã bị revoke
    if (oldRefreshToken.getRevokedAt() != null) {

      // Token đã từng được rotation -> nghi ngờ token bị đánh cắp
      if (oldRefreshToken.getReplacedById() != null) {
        revokeAllActiveTokens(oldRefreshToken.getUsername(), now);

        logger.warn(
            "Refresh token reuse detected for user: {}. Revoking all active refresh tokens.",
            oldRefreshToken.getUsername());

        // Trả kết quả để transaction commit việc revoke
        return new RefreshResult(true, null);
      }

      // Token đã logout hoặc bị thu hồi theo cách khác
      logger.warn(
          "Refresh token already revoked for user: {}.",
          oldRefreshToken.getUsername());
      throw new InvalidRefreshTokenException();
    }

    // Token hết hạn
    if (!oldRefreshToken.getExpiresAt().isAfter(now)) {
      logger.warn("Refresh token expired for user: {}", oldRefreshToken.getUsername());
      throw new InvalidRefreshTokenException();
    }

    // Lấy thông tin user hiện tại, không sử dụng roles từ JWT cũ
    UserDetails userDetails;

    try {
      userDetails = userDetailsService.loadUserByUsername(
          oldRefreshToken.getUsername());
    } catch (UsernameNotFoundException ex) {
      throw new InvalidRefreshTokenException();
    }

    if (!userDetails.isEnabled()
        || !userDetails.isAccountNonExpired()
        || !userDetails.isAccountNonLocked()
        || !userDetails.isCredentialsNonExpired()) {
      throw new InvalidRefreshTokenException();
    }

    // Code cũ: tạo token qua create() rồi phải tìm lại entity theo hash (thêm 1 truy vấn)
    // RefreshTokenResult newRefreshTokenResult = create(
    //     userDetails.getUsername());
    // RefreshToken newRefreshToken = refreshTokenRepository
    //     .findByTokenHash(
    //         hashToken(newRefreshTokenResult.refreshToken()))
    //     .orElseThrow(() -> new IllegalStateException(
    //         "New refresh token was not persisted"));

    // Tạo refresh token mới, nhận luôn entity đã lưu (đã có id)
    String newRawRefreshToken = generateRawRefreshToken();
    RefreshToken newRefreshToken = saveNewToken(
        userDetails.getUsername(),
        newRawRefreshToken);

    // Revoke token cũ và liên kết với token mới
    oldRefreshToken.setRevokedAt(now);
    oldRefreshToken.setReplacedById(newRefreshToken.getId());

    // Tạo access token mới từ UserDetails hiện tại
    AccessTokenResponse accessTokenResponse = tokenService.generateToken(
        userDetails);

    TokenResponse response = new TokenResponse(
        accessTokenResponse.accessToken(),
        accessTokenResponse.tokenType(),
        accessTokenResponse.expiresIn(),
        newRawRefreshToken,
        refreshExpiration().toSeconds());

    logger.info("Refresh token rotated successfully for user: {}", userDetails.getUsername());

    return new RefreshResult(false, response);
  }

  // Code cũ: nạp entity rồi set từng cái.
  // - Danh sách đọc ở thời điểm đó, nên không thấy token vừa được commit sau đó.
  // - Khi flush, Hibernate UPDATE mọi cột, có thể ghi đè replaced_by_id bằng giá trị cũ (null).
  // private void revokeAllActiveTokens(String username, Instant now) {
  //   List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUsernameAndRevokedAtIsNull(username);
  //   activeTokens.forEach(token -> token.setRevokedAt(now));
  // }

  // Một câu UPDATE: chỉ sửa cột revoked_at, và là câu lệnh mới nên thấy được
  // mọi token đã commit trước đó (kể cả token vừa sinh ra từ rotation)
  private void revokeAllActiveTokens(String username, Instant now) {
    int revoked = refreshTokenRepository.revokeAllActiveByUsername(username, now);

    logger.debug("Revoked {} active refresh tokens", revoked);
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
