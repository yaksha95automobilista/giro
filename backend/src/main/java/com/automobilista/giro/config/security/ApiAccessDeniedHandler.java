package com.automobilista.giro.config.security;

import com.automobilista.giro.model.ErrorCode;
import com.automobilista.giro.util.ErrorResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    ErrorResponseUtil.writeJsonResponse(
        response,
        ErrorCode.ACCESS_DENIED,
        ErrorCode.ACCESS_DENIED.getMessage(),
        List.of("You do not have permission to access this resource"));
  }
}
