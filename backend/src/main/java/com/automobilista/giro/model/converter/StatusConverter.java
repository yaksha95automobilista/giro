package com.automobilista.giro.model.converter;

import com.automobilista.giro.model.Status;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, Integer> {

  @Override
  public Integer convertToDatabaseColumn(Status status) {
    return status != null ? status.getCode() : null;
  }

  @Override
  public Status convertToEntityAttribute(Integer statusCode) {
    return Status.fromCode(statusCode);
  }
}
