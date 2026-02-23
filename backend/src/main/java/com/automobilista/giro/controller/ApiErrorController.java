package com.automobilista.giro.controller;

import com.automobilista.giro.dto.response.ErrorResponseDTO;
import com.automobilista.giro.model.ErrorCode;
import com.automobilista.giro.util.ErrorResponseUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiErrorController implements ErrorController {

  @RequestMapping("/error")
  public ResponseEntity<ErrorResponseDTO> handleError(HttpServletRequest request) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

    if (statusCode == 404) {
      return ErrorResponseUtil.response(
          ErrorCode.RESOURCE_NOT_FOUND,
          ErrorCode.RESOURCE_NOT_FOUND.getMessage(),
          List.of("Endpoint not found"));
    }
    if (statusCode == 401) {
      return ErrorResponseUtil.response(
          ErrorCode.AUTHENTICATION_REQUIRED,
          ErrorCode.AUTHENTICATION_REQUIRED.getMessage(),
          List.of("Authentication is required to access this resource"));
    }
    if (statusCode == 403) {
      return ErrorResponseUtil.response(
          ErrorCode.ACCESS_DENIED,
          ErrorCode.ACCESS_DENIED.getMessage(),
          List.of("You do not have permission to access this resource"));
    }

    return ErrorResponseUtil.response(
        ErrorCode.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
        List.of("Unexpected error occurred"));
  }
}
