package com.weather.spond.forecast;

import static com.weather.spond.cache.CacheService.keyForWeatherDataCache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.weather.spond.cache.CacheService;
import com.weather.spond.cache.Location;
import com.weather.spond.cache.LocationRegistry;
import com.weather.spond.error.ErrorCode;
import com.weather.spond.error.ServiceApiRuntimeException;
import com.weather.spond.util.JsonUtil;
import com.weatherspond.api.Event;
import com.weatherspond.api.EventForecast;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherForecastService {

  private final CacheService cacheService;

  private final LocationRegistry locationRegistry;

  private final MetNoWeatherApi metNoWeatherApi;

  private final JsonUtil jsonUtil;

  public WeatherForecastService(
      CacheService cacheService,
      LocationRegistry locationRegistry,
      MetNoWeatherApi metNoWeatherApi,
      JsonUtil jsonUtil) {
    this.cacheService = cacheService;
    this.locationRegistry = locationRegistry;
    this.metNoWeatherApi = metNoWeatherApi;
    this.jsonUtil = jsonUtil;
  }

  public List<EventForecast> getEventForecasts(
      Double latitude, Double longitude, List<Event> events) {
    Location location = new Location(latitude.toString(), longitude.toString());
    try {

      // Fetch weather data from met.no api and update the #weatherDataCache.
      if (!locationRegistry.isLocationRegistered(location)) {
        log.debug("Location not registered with scheduler, registering location...{} ", location);
        locationRegistry.registerLocation(location);

        // Only to force the weather data to be fetched from the met.no api if
        // the location is not registered with the scheduler yet.
        metNoWeatherApi.retrieveFromMetNoApi(latitude, longitude);
      }

      // Process event forecasts from the #weatherDataCache.
      return processFromWeatherDataCache(location, events);

    } catch (IOException | InterruptedException e) {
      log.error("failed to get weather data", e);
      throw new ServiceApiRuntimeException(
          ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private List<EventForecast> processFromWeatherDataCache(Location location, List<Event> events)
      throws JsonProcessingException {
    log.debug("Processing weather data from cache for location {}", location);
    List<WeatherRecord> weatherRecords =
        jsonUtil.fromJson(
            (String) cacheService.get(keyForWeatherDataCache(location)),
            new TypeReference<List<WeatherRecord>>() {});
    return extractWeatherDataPerEvent(weatherRecords, events);
  }

  private List<EventForecast> extractWeatherDataPerEvent(
      List<WeatherRecord> weatherData, List<Event> events) {
    log.debug("Extracting weather data per event...");
    return events.stream()
        .map(event -> findClosestMatchingWeather(weatherData, event))
        .filter(Objects::nonNull)
        .toList();
  }

  private EventForecast findClosestMatchingWeather(List<WeatherRecord> weatherData, Event event) {
    log.debug("Finding closest matching weather for event {}", event.getId());
    WeatherRecord weatherForEvent = null;
    for (WeatherRecord weatherRecord : weatherData) {
      if (weatherRecord.dateTime().isEqual(event.getStart())) {
        weatherForEvent = weatherRecord;
        break;
      } else if (weatherRecord.dateTime().isBefore(event.getStart())) {
        weatherForEvent = weatherRecord;
      }
    }

    return toEventForecast(weatherForEvent, event);
  }

  private EventForecast toEventForecast(WeatherRecord weatherRecord, Event event) {

    if (weatherRecord == null || event == null) {
      return null;
    }
    log.debug("Matching weather record found for event {}", event.getId());
    return new EventForecast()
        .id(event.getId())
        .datetime(weatherRecord.dateTime())
        .airTemperature(weatherRecord.airTemperature())
        .windSpeed(weatherRecord.windSpeed());
  }
}
