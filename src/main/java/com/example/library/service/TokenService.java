package com.example.library.service;

import java.time.Instant;
import java.util.List;

import com.example.library.config.LibraryProperties;
import com.example.library.dto.TokenResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final JwtEncoder jwtEncoder;
  private final LibraryProperties properties;

  public TokenService(
    JwtEncoder jwtEncoder,
    LibraryProperties properties
  ) {
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
  }

  public TokenResponse generateToken(Authentication authentication) {
    Instant now = Instant.now();

    long expiresIn = properties.security()
      .jwt()
      .expiration()
      .toSeconds();

    List<String> roles = authentication.getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .filter(authority -> authority.startsWith("ROLE_"))
      .map(authority -> authority.substring(5))
      .toList();

    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer(properties.api().title())
      .subject(authentication.getName())
      .issuedAt(now)
      .expiresAt(now.plusSeconds(expiresIn))
      .claim("roles", roles)
      .build();

    String token = jwtEncoder.encode(
      JwtEncoderParameters.from(claims)
    ).getTokenValue();

    return new TokenResponse(token, "Bearer", expiresIn);
  }
}