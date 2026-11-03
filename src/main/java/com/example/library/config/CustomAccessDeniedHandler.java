package com.example.library.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.library.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final JsonMapper jsonMapper;

  public CustomAccessDeniedHandler(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException
  ) throws IOException {

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        HttpStatus.FORBIDDEN.value(),
        "Forbidden",
        "You do not have permission to access this resource",
        request.getRequestURI(),
        null
    );

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    jsonMapper.writeValue(response.getOutputStream(), errorResponse);
  }
}