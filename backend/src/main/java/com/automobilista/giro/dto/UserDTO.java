package com.automobilista.giro.dto;

import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class UserDTO extends BaseUserDTO {

  private final Long id;
  private final String orgUnitOj;
  private final Status status;
  private final List<Role> roles;
}
