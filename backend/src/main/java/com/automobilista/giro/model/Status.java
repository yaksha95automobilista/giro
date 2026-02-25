package com.automobilista.giro.model;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Status {
  INACTIVE(0),
  ACTIVE(1),
  DELETED(2);

  private final int code;

  Status(int code) {
    this.code = code;
  }

  public static Status fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(value -> value.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown status code: " + code));
  }
}
