package com.weather.spond.controller;

import com.weather.spond.forecast.WeatherForecastService;
import com.weatherspond.api.EventForecast;
import com.weatherspond.api.Events;
import com.weatherspond.api.WeatherApi;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController implements WeatherApi {

  private final WeatherForecastService weatherForecastService;

  public WeatherController(WeatherForecastService weatherForecastService) {
    this.weatherForecastService = weatherForecastService;
  }

  @Override
  public ResponseEntity<List<EventForecast>> getWeather(String lat, String lon, Events events) {
    return new ResponseEntity<>(
        weatherForecastService.getEventForecasts(
            Double.valueOf(lat), Double.valueOf(lon), events.getEvents()),
        HttpStatus.OK);
  }
}
