package com.automobilista.giro.exception.base;

import com.automobilista.giro.model.ErrorCode;

public class BusinessException extends RuntimeException implements CustomError {
  private final ErrorCode errorCode;

  public BusinessException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  @Override
  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
