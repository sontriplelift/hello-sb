package com.example.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.AccessTokenResponse;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.LogoutRequest;
import com.example.library.dto.RefreshRequest;
import com.example.library.dto.RefreshResult;
import com.example.library.dto.RefreshTokenResult;
import com.example.library.dto.TokenResponse;
import com.example.library.exception.ErrorResponse;
import com.example.library.exception.InvalidRefreshTokenException;
import com.example.library.service.RefreshTokenService;
import com.example.library.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag (name = "Auth", description = "API Auth")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final RefreshTokenService refreshTokenService;

  public AuthController(
      AuthenticationManager authenticationManager,
      TokenService tokenService,
      RefreshTokenService refreshTokenService) {
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
    this.refreshTokenService = refreshTokenService;
  }

  @Operation(summary = "Login to the application")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Login successful"
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Invalid username or password",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @Valid @RequestBody LoginRequest request) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()));

    // Tạo access token (JWT)
    AccessTokenResponse accessTokenResponse = tokenService.generateToken(authentication);

    // Tạo Refresh Token và lưu hash vào DB
    RefreshTokenResult refreshTokenResult =
        refreshTokenService.create(authentication.getName());

    TokenResponse response = new TokenResponse(
        accessTokenResponse.accessToken(),
        accessTokenResponse.tokenType(),
        accessTokenResponse.expiresIn(),
        refreshTokenResult.refreshToken(),
        refreshTokenResult.refreshExpiresIn()
    );

    logger.info("Login successful for user: {}", authentication.getName());

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Refresh token rotation")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Refresh token rotation successful"
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Invalid or expired refresh token",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    )
  })
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
      @Valid @RequestBody RefreshRequest request) {

    RefreshResult result = refreshTokenService.refresh(
        request.refreshToken()
    );

    if (result.reuseDetected()) {
      throw new InvalidRefreshTokenException();
    }

    return ResponseEntity.ok(result.tokenResponse());
  }

  @Operation(summary = "Logout")
  @ApiResponses({
    @ApiResponse(
      responseCode = "204",
      description = "Logout successfully"
    ),
    @ApiResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(
        schema = @Schema(implementation = ErrorResponse.class)
      )
    ),
  })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
      refreshTokenService.logout(request.refreshToken());

      return ResponseEntity.noContent().build();
  }
  
}