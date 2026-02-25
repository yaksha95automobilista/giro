package com.automobilista.giro.controller;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.dto.request.CreateUserRequestDTO;
import com.automobilista.giro.dto.request.UpdateUserRequestDTO;
import com.automobilista.giro.dto.request.UserSearchRequestDTO;
import com.automobilista.giro.dto.response.MessageResponseDTO;
import com.automobilista.giro.dto.response.PagingResponseDTO;
import com.automobilista.giro.dto.response.SuccessResponseDTO;
import com.automobilista.giro.service.UserService;
import com.automobilista.giro.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users")
@RestController
@AllArgsConstructor
public class UserController {

  private final UserService service;

  @GetMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<SuccessResponseDTO<PagingResponseDTO<UserDTO>>> getUsers(
      @Valid @ModelAttribute UserSearchRequestDTO searchRequest, HttpServletRequest request) {
    return ResponseEntity.ok(
        ResponseUtil.success(
            service.getUsers(searchRequest),
            "All users retrieved successfully",
            request.getRequestURI()));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<SuccessResponseDTO<UserDTO>> getUser(
      @PathVariable Long id, HttpServletRequest request) {

    return ResponseEntity.ok(
        ResponseUtil.success(
            service.getUser(id), "User retrieved successfully", request.getRequestURI()));
  }

  @PostMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<SuccessResponseDTO<UserDTO>> createUser(
      @Valid @RequestBody CreateUserRequestDTO createUserRequestDTO, HttpServletRequest request) {
    return ResponseEntity.ok(
        ResponseUtil.success(
            service.createUser(createUserRequestDTO),
            "User created successfully",
            request.getRequestURI()));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<SuccessResponseDTO<UserDTO>> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserRequestDTO updateUserRequestDTO,
      HttpServletRequest request) {
    return ResponseEntity.ok(
        ResponseUtil.success(
            service.updateUser(id, updateUserRequestDTO),
            "User updated successfully",
            request.getRequestURI()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<SuccessResponseDTO<MessageResponseDTO>> deleteUser(
      @PathVariable Long id, HttpServletRequest request) {
    return ResponseEntity.ok(
        ResponseUtil.success(
            service.deleteUser(id), "User deleted successfully", request.getRequestURI()));
  }
}
