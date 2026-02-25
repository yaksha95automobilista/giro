package com.automobilista.giro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.dto.request.CreateUserRequestDTO;
import com.automobilista.giro.exception.auth.LoginFailedException;
import com.automobilista.giro.model.Status;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.flyway.enabled=true")
class AuthServiceIT {

  @Autowired private AuthService authService;
  @Autowired private UserService userService;

  @Test
  void givenInactiveUserWhenLoginThenThrowsLoginFailedException() {
    String seed = String.valueOf(System.currentTimeMillis());
    String email = "itest.login.inactive." + seed + "@giro.com";

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username("itest.login.inactive." + seed)
                .fullName("Integration Inactive Login")
                .email(email)
                .orgUnitId(1L)
                .password("itest.login.inactive.giro123!")
                .build());

    assertThat(createdUser.getStatus()).isEqualTo(Status.INACTIVE);

    MockHttpServletResponse response = new MockHttpServletResponse();

    assertThatThrownBy(() -> authService.login(email, "itest.login.inactive.giro123!", response))
        .isInstanceOf(LoginFailedException.class);

    Cookie[] cookies = response.getCookies();
    assertThat(cookies).isNullOrEmpty();
  }
}
