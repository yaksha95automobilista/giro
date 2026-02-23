package com.automobilista.giro.exception.base;

import com.automobilista.giro.model.ErrorCode;

public class SecurityException extends RuntimeException implements CustomError {
  private final ErrorCode errorCode;

  public SecurityException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  @Override
  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
