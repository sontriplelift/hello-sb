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

import com.example.library.dto.LoginRequest;
import com.example.library.dto.RefreshTokenResult;
import com.example.library.dto.TokenResponse;
import com.example.library.dto.TokenWithRefreshResponse;
import com.example.library.exception.ErrorResponse;
import com.example.library.service.RefreshTokenService;
import com.example.library.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

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
  public ResponseEntity<TokenWithRefreshResponse> login(
      @Valid @RequestBody LoginRequest request) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()));

    // Tạo access token (JWT)
    TokenResponse accessTokenResponse = tokenService.generateToken(authentication);

    // Tạo Refresh Token và lưu hash vào DB
    RefreshTokenResult refreshTokenResult =
        refreshTokenService.create(authentication.getName());

    TokenWithRefreshResponse response = new TokenWithRefreshResponse(
        accessTokenResponse.accessToken(),
        accessTokenResponse.tokenType(),
        accessTokenResponse.expiresIn(),
        refreshTokenResult.rawToken(),
        refreshTokenResult.refreshExpiresIn()
    );

    return ResponseEntity.ok(response);
  }
}