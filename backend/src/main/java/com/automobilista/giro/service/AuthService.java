package com.automobilista.giro.service;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.exception.auth.InvalidJwtTokenException;
import com.automobilista.giro.exception.auth.LoginFailedException;
import com.automobilista.giro.exception.auth.MissingCookieException;
import com.automobilista.giro.exception.auth.MissingJwtTokenException;
import com.automobilista.giro.exception.user.UserNotFoundException;
import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.entity.User;
import com.automobilista.giro.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

  private final JwtService jwtService;
  private final AuthenticationProvider authenticationProvider;
  private final CustomUserDetailsService customUserDetailsService;
  private final UserRepository userRepository;

  public void login(String email, String password, HttpServletResponse response) {
    try {
      Authentication authentication =
          authenticationProvider.authenticate(
              new UsernamePasswordAuthenticationToken(email, password));
      assert authentication != null;
      UserDetails user = (UserDetails) authentication.getPrincipal();
      String jwt = jwtService.generateToken(user);
      response.addCookie(jwtService.generateCookie(jwt));
    } catch (BadCredentialsException ex) {
      throw new LoginFailedException("Invalid username or password");
    } catch (AuthenticationException ex) {
      throw new LoginFailedException("Authentication failed: " + ex.getMessage());
    }
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String jwt = jwtService.extractTokenFromCookie(request);

    if (jwt != null && !jwt.isEmpty()) {
      HttpSession session = request.getSession(false);

      if (session != null) {
        session.invalidate();
      }

      response.addCookie(createExpiredCookie(jwtService.getCookieName()));
    } else {
      throw new MissingJwtTokenException();
    }
  }

  public UserDTO me(HttpServletRequest request) {
    return getJwtUserDTO(jwtService.extractTokenFromCookie(request));
  }

  public void validateSession(HttpServletRequest request) {
    if (request.getCookies() == null) {
      throw new MissingCookieException();
    }

    String jwt = jwtService.extractTokenFromCookie(request);
    UserDetails userDetails = getJwtUserDetails(jwt);

    if (!jwtService.isTokenValid(jwt, userDetails)) {
      throw new InvalidJwtTokenException();
    }
  }

  public UserDTO getJwtUserDTO(String jwt) {
    return toDto((User) getJwtUserDetails(jwt));
  }

  public UserDetails getJwtUserDetails(String jwt) {
    if (jwt == null || jwt.isEmpty()) {
      throw new MissingJwtTokenException();
    }

    String email = jwtService.extractUsername(jwt);

    User user;
    try {
      user = (User) customUserDetailsService.loadUserByUsername(email);
    } catch (UsernameNotFoundException ex) {
      throw new UserNotFoundException(email);
    }

    return user;
  }

  private Cookie createExpiredCookie(String cookieName) {
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(jwtService.isCookieSecure());
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }

  private UserDTO toDto(User user) {
    List<Role> roles =
        userRepository.findRoleCodesByUserId(user.getId()).stream().map(Role::valueOf).toList();
    return UserDTO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .orgUnitId(user.getOrganizacionaJedinica().getId())
        .orgUnitOj(user.getOrganizacionaJedinica().getOj())
        .status(user.getStatus())
        .roles(roles)
        .build();
  }
}
