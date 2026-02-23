package com.automobilista.giro.exception.user;

import com.automobilista.giro.exception.base.CustomException;
import com.automobilista.giro.model.ErrorCode;

public class UsernameAlreadyExistsException extends CustomException {
  public UsernameAlreadyExistsException(String username) {
    super("User with username: " + username + " already exists", ErrorCode.USERNAME_ALREADY_EXISTS);
  }
}
