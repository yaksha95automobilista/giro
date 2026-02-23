package com.automobilista.giro.exception.user;

import com.automobilista.giro.exception.base.NotFoundException;
import com.automobilista.giro.model.ErrorCode;

public class UserNotFoundException extends NotFoundException {
  public UserNotFoundException(String username) {
    super("Could not find the user with username: " + username, ErrorCode.USER_NOT_FOUND);
  }

  public UserNotFoundException(Long id) {
    super("Could not find the user with id: " + id, ErrorCode.USER_NOT_FOUND);
  }
}
