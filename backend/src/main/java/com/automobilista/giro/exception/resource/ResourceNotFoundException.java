package com.automobilista.giro.exception.resource;

import com.automobilista.giro.exception.base.NotFoundException;
import com.automobilista.giro.model.ErrorCode;

public class ResourceNotFoundException extends NotFoundException {
  public ResourceNotFoundException(String message) {
    super(message, ErrorCode.RESOURCE_NOT_FOUND);
  }
}
