package com.automobilista.giro.exception.auth;

import com.automobilista.giro.exception.base.SecurityException;
import com.automobilista.giro.model.ErrorCode;

public class LoginFailedException extends SecurityException {
  public LoginFailedException(String message) {
    super(ErrorCode.LOGIN_FAILED, message);
  }
}
