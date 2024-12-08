package com.weather.spond.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
  WEATHER_DATA_NOT_FOUND(1001, "Weather data not found."),

  INTERNAL_SERVER_ERROR(1002, "Unable to process the request.");

  private final int code;

  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
