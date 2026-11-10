package com.example.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.LoginRequest;
import com.example.library.dto.TokenResponse;
import com.example.library.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  public AuthController(
      AuthenticationManager authenticationManager,
      TokenService tokenService,
      AuthenticationEntryPoint authenticationEntryPoint) {
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) throws Exception {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.username(),
              request.password()));

      return ResponseEntity.ok(tokenService.generateToken(authentication));
    } catch (AuthenticationException exception) {
      authenticationEntryPoint.commence(
          httpRequest,
          httpResponse,
          exception);

      return null;
    }
  }
}