package com.automobilista.giro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

  @NotBlank(message = "email is required")
  @Email(message = "email must be valid")
  @Size(max = 254, message = "email must be at most 254 characters")
  private String email;

  @NotBlank(message = "password is required")
  @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
  private String password;
}
