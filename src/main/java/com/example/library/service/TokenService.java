package com.example.library.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.library.config.LibraryProperties;
import com.example.library.dto.AccessTokenResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

  private final JwtEncoder jwtEncoder;
  private final LibraryProperties properties;

  public TokenService(
      JwtEncoder jwtEncoder,
      LibraryProperties properties) {
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
  }

  public AccessTokenResponse generateToken(Authentication authentication) {
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
        .id(UUID.randomUUID().toString())
        .claim("roles", roles)
        .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
        .build();

    String token = jwtEncoder.encode(
        JwtEncoderParameters.from(header, claims)).getTokenValue();

    return new AccessTokenResponse(token, "Bearer", expiresIn);
  }

  public AccessTokenResponse generateToken(UserDetails userDetails) {
    Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
        userDetails,
        null,
        userDetails.getAuthorities());

    return generateToken(authentication);
  }
}