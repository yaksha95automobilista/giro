package com.automobilista.giro.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUserDTO {

  @NotBlank(message = "username is required")
  @Size(min = 5, max = 100, message = "username must be between 5 and 100 characters")
  private String username;

  @NotBlank(message = "fullName is required")
  @Size(min = 2, max = 100, message = "fullName must be between 2 and 100 characters")
  private String fullName;

  @NotBlank(message = "email is required")
  @Email(message = "email must be valid")
  @Size(max = 254, message = "email must be at most 254 characters")
  private String email;

  @NotNull(message = "orgUnitId is required")
  @Positive(message = "orgUnitId must be a positive number")
  @JsonProperty("orgUnitId")
  @JsonAlias("org_unit_id")
  private Long orgUnitId;
}
