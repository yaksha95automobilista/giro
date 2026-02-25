package com.automobilista.giro.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.dto.request.CreateUserRequestDTO;
import com.automobilista.giro.dto.request.UpdateUserRequestDTO;
import com.automobilista.giro.dto.response.MessageResponseDTO;
import com.automobilista.giro.dto.response.PagingResponseDTO;
import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import com.automobilista.giro.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserControllerIT {

  private static final String USERS_ENDPOINT = "/api/users";
  private static final String USER_ID_ENDPOINT = "/api/users/1";

  private MockMvc mockMvc;

  @Autowired private WebApplicationContext webApplicationContext;

  @MockitoBean private UserService userService;

  @BeforeEach
  void setUp() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(roles = "SUPER_ADMIN")
  void givenSuperAdminWhenCallUserEndpointsThenReturnsOk() throws Exception {
    UserDTO userDto =
        UserDTO.builder()
            .id(1L)
            .username("super.admin.user")
            .fullName("Super Admin User")
            .email("super.admin.user@giro.com")
            .orgUnitId(1L)
            .status(Status.ACTIVE)
            .roles(List.of(Role.SUPER_ADMIN))
            .build();

    PagingResponseDTO<UserDTO> pagingResponse =
        new PagingResponseDTO<>(List.of(userDto), 1, 1, 10, 0, false);

    when(userService.getUsers(any())).thenReturn(pagingResponse);
    when(userService.getUser(1L)).thenReturn(userDto);
    when(userService.createUser(any(CreateUserRequestDTO.class))).thenReturn(userDto);
    when(userService.updateUser(anyLong(), any(UpdateUserRequestDTO.class))).thenReturn(userDto);
    when(userService.deleteUser(1L)).thenReturn(new MessageResponseDTO("deleted"));

    mockMvc.perform(get(USERS_ENDPOINT)).andExpect(status().isOk());
    mockMvc.perform(get(USER_ID_ENDPOINT)).andExpect(status().isOk());
    mockMvc
        .perform(
            post(USERS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "new.user",
                      "fullName": "New User",
                      "email": "new.user@giro.com",
                      "org_unit_id": 1,
                      "password": "Aa1@aaaa",
                      "roles": ["USER"]
                    }
                    """))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            put(USER_ID_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isOk());
    mockMvc.perform(delete(USER_ID_ENDPOINT)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "BUSINESS_ADMIN")
  void givenNonSuperAdminWhenCallUserEndpointsThenReturnsForbidden() throws Exception {
    mockMvc.perform(get(USERS_ENDPOINT)).andExpect(status().isForbidden());
    mockMvc.perform(get(USER_ID_ENDPOINT)).andExpect(status().isForbidden());
    mockMvc
        .perform(
            post(USERS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "new.user",
                      "fullName": "New User",
                      "email": "new.user@giro.com",
                      "org_unit_id": 1,
                      "password": "Aa1@aaaa",
                      "roles": ["USER"]
                    }
                    """))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(
            put(USER_ID_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete(USER_ID_ENDPOINT)).andExpect(status().isForbidden());

    verify(userService, never()).getUsers(any());
    verify(userService, never()).getUser(anyLong());
    verify(userService, never()).createUser(any(CreateUserRequestDTO.class));
    verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequestDTO.class));
    verify(userService, never()).deleteUser(anyLong());
  }
}
