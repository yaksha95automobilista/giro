package com.automobilista.giro.dto.request;

import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UserSearchRequestDTO extends PagingRequestDTO {

  private Long id;
  private String username;
  private String fullName;
  private String email;
  private Long orgUnitId;
  private String orgUnitOj;
  private List<Role> roles;
  private Status status;
}
