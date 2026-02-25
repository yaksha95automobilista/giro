package com.automobilista.giro.exception.auth;

import com.automobilista.giro.exception.base.SecurityException;
import com.automobilista.giro.model.ErrorCode;

public class LogoutFailedException extends SecurityException {
  public LogoutFailedException(String message) {
    super(ErrorCode.LOGOUT_FAILED, message);
  }
}
