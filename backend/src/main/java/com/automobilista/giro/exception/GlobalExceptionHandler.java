package com.automobilista.giro.exception;

import com.automobilista.giro.dto.response.ErrorResponseDTO;
import com.automobilista.giro.exception.base.CustomError;
import com.automobilista.giro.model.ErrorCode;
import com.automobilista.giro.util.ErrorResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String LOG_PATTERN = "[{}] {} - {}: {}";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details = new ArrayList<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError ->
                details.add(fieldError.getField() + ": " + fieldError.getDefaultMessage()));
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(
            objectError ->
                details.add(objectError.getObjectName() + ": " + objectError.getDefaultMessage()));

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    List<String> details =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponseDTO> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    List<String> details = List.of(ex.getParameterName() + " parameter is required");

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String parameterName = ex.getName();
    Class<?> requiredType = ex.getRequiredType();
    String expectedType = requiredType != null ? requiredType.getSimpleName() : "valid type";
    List<String> details = List.of(parameterName + " must be of type " + expectedType);

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    List<String> details = List.of("Request body is malformed or missing required fields");

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    List<String> details =
        List.of("Method " + ex.getMethod() + " is not supported for this endpoint");

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponseDTO> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    List<String> details = List.of("Content-Type " + ex.getContentType() + " is not supported");

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getMessage(), details);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponseDTO> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    List<String> details = List.of("Endpoint not found: " + ex.getRequestURL());

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getMessage(), details);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponseDTO> handleAuthorizationDenied(
      AuthorizationDeniedException ex, HttpServletRequest request) {
    List<String> details =
        List.of(
            "Nemate pravo pristupa ovom resursu",
            "Endpoint: " + request.getMethod() + " " + request.getRequestURI());

    logWarn(ex, request);
    return buildErrorResponse(
        ErrorCode.ACCESS_DENIED, "Pristup je zabranjen za traženi resurs", details);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
      RuntimeException ex, HttpServletRequest request) {
    if (!(ex instanceof CustomError customError)) {
      return handleGeneralException(ex, request);
    }

    ErrorCode errorCode = customError.getErrorCode();
    List<String> details = List.of(ex.getMessage());

    logError(ex, request);
    return buildErrorResponse(errorCode, errorCode.getMessage(), details);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    logError(ex, request);
    return buildErrorResponse(
        ErrorCode.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
        List.of("Unexpected error occurred"));
  }

  private ResponseEntity<ErrorResponseDTO> buildErrorResponse(
      ErrorCode errorCode, String message, List<String> details) {
    return ErrorResponseUtil.response(errorCode, message, details);
  }

  private void logWarn(Exception ex, HttpServletRequest request) {
    logger.warn(
        LOG_PATTERN,
        request.getMethod(),
        request.getRequestURI(),
        ex.getClass().getSimpleName(),
        ex.getMessage());
  }

  private void logError(Exception ex, HttpServletRequest request) {
    logger.error(
        LOG_PATTERN,
        request.getMethod(),
        request.getRequestURI(),
        ex.getClass().getSimpleName(),
        ex.getMessage(),
        ex);
  }
}
