package com.weather.spond.error;

import com.weatherspond.api.ApiError;
import io.opentelemetry.api.trace.Span;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ServiceExceptionHandler {

  @ExceptionHandler(ServiceApiRuntimeException.class)
  protected ResponseEntity<Object> handleServiceApiRuntimeException(
      ServiceApiRuntimeException ex, HttpServletRequest request) {
    log.error("internal server error", ex);
    return new ResponseEntity<>(toApiError(ex.getErrorCode(), request), ex.getHttpStatus());
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleServiceApiRuntimeException(
      Exception ex, HttpServletRequest request) {
    log.error("internal server error", ex);
    return new ResponseEntity<>(
        toApiError(ErrorCode.INTERNAL_SERVER_ERROR, request), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ApiError toApiError(ErrorCode errorCode, HttpServletRequest request) {
    return new ApiError()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .timestamp(OffsetDateTime.now())
        .resource(request.getRequestURI())
        .traceId(Span.current().getSpanContext().getTraceId());
  }
}
