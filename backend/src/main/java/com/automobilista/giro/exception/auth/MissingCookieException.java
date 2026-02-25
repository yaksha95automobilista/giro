package com.automobilista.giro.exception.auth;

import com.automobilista.giro.exception.base.SecurityException;
import com.automobilista.giro.model.ErrorCode;

public class MissingCookieException extends SecurityException {
  public MissingCookieException() {
    super(ErrorCode.MISSING_COOKIE, "Cookie is missing.");
  }
}
