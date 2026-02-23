package com.automobilista.giro.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 1xxx – infra/security
  INTERNAL_SERVER_ERROR(1000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
  LOGIN_FAILED(1101, "Login failed", HttpStatus.UNAUTHORIZED),
  LOGOUT_FAILED(1102, "Logout failed", HttpStatus.BAD_REQUEST),
  MISSING_JWT_TOKEN(1103, "Missing JWT token", HttpStatus.UNAUTHORIZED),
  INVALID_JWT_TOKEN(1104, "Invalid JWT token", HttpStatus.UNAUTHORIZED),
  MISSING_COOKIE(1105, "Missing cookie", HttpStatus.BAD_REQUEST),
  AUTHENTICATION_REQUIRED(1106, "Authentication required", HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED(1107, "Access denied", HttpStatus.FORBIDDEN),
  VALIDATION_FAILED(1400, "Validation failed", HttpStatus.BAD_REQUEST),

  // 21xx – user domain
  USERNAME_ALREADY_EXISTS(2101, "Username already exists", HttpStatus.CONFLICT),
  USER_NOT_FOUND(2102, "User not found", HttpStatus.NOT_FOUND),
  WEAK_PASSWORD(
      2103, "Password does not meet minimum strength requirements", HttpStatus.BAD_REQUEST),

  // 3xxx – generic resources
  RESOURCE_NOT_FOUND(3001, "Resource not found", HttpStatus.NOT_FOUND);

  private final int code;
  private final String message;
  private final HttpStatus httpStatus;
}
