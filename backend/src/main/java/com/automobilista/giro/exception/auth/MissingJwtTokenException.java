package com.automobilista.giro.exception.auth;

import com.automobilista.giro.exception.base.SecurityException;
import com.automobilista.giro.model.ErrorCode;

public class MissingJwtTokenException extends SecurityException {
  public MissingJwtTokenException() {
    super(ErrorCode.MISSING_JWT_TOKEN, "JWT token is missing.");
  }
}
