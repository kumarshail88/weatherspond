package com.weather.spond.forecast;

import static java.lang.Double.valueOf;

import com.weather.spond.cache.Location;
import com.weather.spond.cache.LocationRegistry;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WeatherApiScheduler {

  private final Duration cooldownPeriod;

  private final LocationRegistry locationRegistry;

  private final MetNoWeatherApi metNoWeatherApi;

  public WeatherApiScheduler(
      @Value("${weather.api.cooldown_period}") final Duration cooldownPeriod,
      LocationRegistry locationRegistry,
      MetNoWeatherApi metNoWeatherApi) {
    this.locationRegistry = locationRegistry;
    this.cooldownPeriod = cooldownPeriod;
    this.metNoWeatherApi = metNoWeatherApi;
  }

  @Scheduled(cron = "${weather.api.scheduler_cron}")
  public void fetchWeatherData() {
    log.info("Fetching weather data...");

    OffsetDateTime now = OffsetDateTime.now();

    for (Map.Entry<Location, OffsetDateTime> entry :
        locationRegistry.getRegisteredLocations().entrySet()) {
      Location location = entry.getKey();
      OffsetDateTime lastUpdated = entry.getValue();

      if (lastUpdated.plusSeconds(cooldownPeriod.toSeconds()).isBefore(now)) {
        try {
          log.info("Fetching weather data for location {}", location);
          metNoWeatherApi.retrieveFromMetNoApi(
              valueOf(location.latitude()), valueOf(location.longitude()));
        } catch (IOException | InterruptedException e) {
          log.error("failed to retrieve weather data for location {}", location, e);
        }
      }
    }
  }
}
