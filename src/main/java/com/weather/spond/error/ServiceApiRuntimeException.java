package com.weather.spond.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceApiRuntimeException extends RuntimeException {

  private final HttpStatus httpStatus;

  private final ErrorCode errorCode;

  public ServiceApiRuntimeException(ErrorCode errorCode, HttpStatus httpStatus) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }
}
