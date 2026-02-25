package com.automobilista.giro.controller;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.dto.request.LoginRequestDTO;
import com.automobilista.giro.dto.response.MessageResponseDTO;
import com.automobilista.giro.dto.response.SuccessResponseDTO;
import com.automobilista.giro.service.AuthService;
import com.automobilista.giro.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService service;

  @PostMapping("/login")
  public ResponseEntity<SuccessResponseDTO<MessageResponseDTO>> login(
      @Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletRequest request,
      HttpServletResponse response) {

    service.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword(), response);
    return ResponseEntity.ok(
        ResponseUtil.success(
            new MessageResponseDTO("Login successful"),
            "Login successful",
            request.getRequestURI()));
  }

  @PostMapping("/logout")
  public ResponseEntity<SuccessResponseDTO<MessageResponseDTO>> logout(
      HttpServletRequest request, HttpServletResponse response) {

    service.logout(request, response);
    return ResponseEntity.ok(
        ResponseUtil.success(
            new MessageResponseDTO("Logout successful"),
            "Logout successful",
            request.getRequestURI()));
  }

  @GetMapping("/me")
  public ResponseEntity<SuccessResponseDTO<UserDTO>> me(HttpServletRequest request) {

    return ResponseEntity.ok(
        ResponseUtil.success(service.me(request), "User identified", request.getRequestURI()));
  }

  @GetMapping("/validate-session")
  public ResponseEntity<SuccessResponseDTO<MessageResponseDTO>> validateSession(
      HttpServletRequest request) {

    service.validateSession(request);
    return ResponseEntity.ok(
        ResponseUtil.success(
            new MessageResponseDTO("Session is active"),
            "Session is active",
            request.getRequestURI()));
  }
}
