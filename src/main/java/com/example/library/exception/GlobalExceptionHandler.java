package com.example.library.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, HttpServletRequest request,
      Map<String, String> fieldErrors) {
    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        fieldErrors);
    return ResponseEntity.status(status).body(body);
  }

  // Không tìm thấy resource: 404
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.NOT_FOUND,
        ex.getMessage(),
        request,
        null);
  }

  // Resource bị trùng: 409
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(
      DuplicateResourceException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.CONFLICT,
        ex.getMessage(),
        request,
        null);
  }

  // Resource in use: 409
  @ExceptionHandler(ResourceInUseException.class)
  public ResponseEntity<ErrorResponse> handleResourceInUse(
      ResourceInUseException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.CONFLICT,
        ex.getMessage(),
        request,
        null);
  }

  // Dữ liệu không hợp lệ: 400
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(
      BusinessException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        ex.getMessage(),
        request,
        null);
  }

  // Dữ liệu sort không hợp lệ: 400
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequest(
      InvalidRequestException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        ex.getMessage(),
        request,
        null);
  }

  // Bean validation thất bại: 400
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

    return buildError(
        HttpStatus.BAD_REQUEST,
        "Invalid input data",
        request,
        fieldErrors);
  }

  // JSON sai cú pháp hoặc không thể chuyển kiểu: 400
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableMessage(
      HttpMessageNotReadableException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Invalid JSON format",
        request,
        null);
  }

  // Path variable hoặc query parameter sai kiểu: 400
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex,
      HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Invalid parameter type: " + ex.getName(),
        request,
        null);
  }

  // Vi phạm constraint database: 400 (409)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex,
      HttpServletRequest request) {

    logger.warn("Data integrity violation at path {}", request.getRequestURI(), ex);
    return buildError(
        HttpStatus.CONFLICT,
        "Data integrity violation",
        request,
        null);
  }

  // HTTP method không được hỗ trợ: 405
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.METHOD_NOT_ALLOWED,
        "HTTP method not allowed: " + ex.getMethod(),
        request,
        null);
  }

  // Sai Content-Type hoặc Accept header: 415
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Unsupported media type: " + ex.getContentType(),
        request,
        null);
  }

  // URL không tồn tại: 404
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      NoResourceFoundException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.NOT_FOUND,
        "Resource not found",
        request,
        null);
  }

  // Đăng nhập thất bại: 401
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(
      AuthenticationException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.UNAUTHORIZED,
        "Invalid username or password",
        request,
        null);
  }

  // Lỗi không xác định: 500
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(
      Exception ex,
      HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message = "An unexpected error occurred";

    if (ex instanceof org.springframework.web.ErrorResponse springError) {
      status = HttpStatus.valueOf(springError.getStatusCode().value());
      message = springError.getBody().getDetail();

      if (message == null || message.isBlank()) {
        message = springError.getBody().getTitle();
      }

      if (message == null || message.isBlank()) {
        message = status.getReasonPhrase();
      }
    }

    if (status.is5xxServerError()) {
      logger.error(
          "Unexpected server error at path {}",
          request.getRequestURI(),
          ex);
    } else if (status.is4xxClientError()) {
      logger.warn(
          "Client error: status={}, path={}, message={}",
          status.value(),
          request.getRequestURI(),
          message);
    } else {
      logger.warn(
          "Request failed: status={}, path={}",
          status.value(),
          request.getRequestURI());
    }

    return buildError(
        status,
        message,
        request,
        null);
  }
}