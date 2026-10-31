package com.example.library.exception;

public class ResourceNotFoundException extends RuntimeException {
  
  public ResourceNotFoundException(String resourceName, Object id) {
    super(String.format("%s not found with id: %s", resourceName, id));
  }
}
