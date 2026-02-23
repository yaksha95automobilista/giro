package com.automobilista.giro.exception.base;

import com.automobilista.giro.model.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException implements CustomError {
  private final ErrorCode errorCode;

  public CustomException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  @Override
  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
