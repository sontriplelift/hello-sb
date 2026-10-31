package com.example.library.exception;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

// Annotation này khiến các thuộc tính có giá trị null bị bỏ qua khi chuyển sang JSON
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
  Instant timestamp,
  int status,
  String error,
  String message,
  String path,
  Map<String, String> fieldErrors
) {
  
}
