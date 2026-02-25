package com.automobilista.giro.dto.response;

import com.automobilista.giro.dto.BaseMessageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageResponseDTO extends BaseMessageDTO {
  public MessageResponseDTO(String message) {
    super(message);
  }
}
