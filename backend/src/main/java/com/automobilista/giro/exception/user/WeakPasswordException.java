package com.automobilista.giro.exception.user;

import com.automobilista.giro.exception.base.CustomException;
import com.automobilista.giro.model.ErrorCode;

public class WeakPasswordException extends CustomException {
  public WeakPasswordException() {
    super(
        "Password must be at least 8 characters long and contain at least one digit and one special character",
        ErrorCode.WEAK_PASSWORD);
  }
}
