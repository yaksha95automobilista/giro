package com.automobilista.giro.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UpdateUserRequestDTO {

  @Size(min = 5, max = 100, message = "username must be between 5 and 100 characters")
  private String username;

  @Size(min = 2, max = 100, message = "fullName must be between 2 and 100 characters")
  private String fullName;

  @Email(message = "email must be valid")
  @Size(max = 254, message = "email must be at most 254 characters")
  private String email;

  @Positive(message = "orgUnitId must be a positive number")
  @JsonProperty("orgUnitId")
  @JsonAlias("org_unit_id")
  private Long orgUnitId;

  @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
  @Pattern(
      regexp = "^(?=.*\\d)(?=.*[\\W_]).{8,72}$",
      message = "password must contain at least one digit and one special character")
  private String password;

  private Status status;

  @Builder.Default private List<Role> roles = null;
}
