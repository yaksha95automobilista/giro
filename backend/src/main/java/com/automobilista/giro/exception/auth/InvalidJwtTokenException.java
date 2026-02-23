package com.automobilista.giro.exception.auth;

import com.automobilista.giro.exception.base.SecurityException;
import com.automobilista.giro.model.ErrorCode;

public class InvalidJwtTokenException extends SecurityException {
  public InvalidJwtTokenException() {
    super(ErrorCode.INVALID_JWT_TOKEN, "JWT token is invalid.");
  }
}
