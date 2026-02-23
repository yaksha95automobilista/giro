package com.automobilista.giro.exception.base;

import com.automobilista.giro.model.ErrorCode;

public abstract class NotFoundException extends RuntimeException implements CustomError {
  private final ErrorCode errorCode;

  protected NotFoundException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  @Override
  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
