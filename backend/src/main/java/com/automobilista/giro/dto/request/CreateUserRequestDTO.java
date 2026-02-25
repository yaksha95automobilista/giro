package com.automobilista.giro.dto.request;

import com.automobilista.giro.dto.BaseUserDTO;
import com.automobilista.giro.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
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
public class CreateUserRequestDTO extends BaseUserDTO {

  @NotBlank(message = "password is required")
  @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
  @Pattern(
      regexp = "^(?=.*\\d)(?=.*[\\W_]).{8,72}$",
      message = "password must contain at least one digit and one special character")
  private String password;

  @Builder.Default private List<Role> roles = new ArrayList<>();
}
