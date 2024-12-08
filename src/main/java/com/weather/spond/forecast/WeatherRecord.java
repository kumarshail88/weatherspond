package com.weather.spond.forecast;

import java.time.OffsetDateTime;

public record WeatherRecord(OffsetDateTime dateTime, Float airTemperature, Float windSpeed) {}
