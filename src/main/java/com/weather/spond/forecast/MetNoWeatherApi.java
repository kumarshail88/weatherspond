package com.weather.spond.forecast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.spond.cache.CacheService;
import com.weather.spond.cache.Location;
import com.weather.spond.util.JsonUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service to retrieve and transform the weather data from the met.no api. The weather data is
 * transformed using the {@link WeatherDataTransformer} followed by storing the transformation into
 * the cache to be reused.
 */
@Service
@Slf4j
public class MetNoWeatherApi {

  private final CacheService cacheService;

  private final String weatherApiUrl;

  private final OkHttpClient restClient;

  private final String weatherTransformerQuery;

  private final WeatherDataTransformer weatherDataTransformer;

  private final JsonUtil jsonUtil;

  public MetNoWeatherApi(
      CacheService cacheService,
      WeatherDataTransformer weatherDataTransformer,
      OkHttpClient restClient,
      @Value("${queries.weather_transformer_query}") final String weatherTransformerQuery,
      @Value("${weather.api.baseurl}") final String metNoServerBaseUrl,
      @Value("${weather.api.version}") final String metNoServerVersion,
      @Value("${weather.api.path}") final String metNoApiPath,
      ObjectMapper objectMapper,
      JsonUtil jsonUtil) {
    this.cacheService = cacheService;
    this.weatherDataTransformer = weatherDataTransformer;
    this.restClient = restClient;
    this.weatherTransformerQuery = weatherTransformerQuery;
    this.jsonUtil = jsonUtil;
    this.weatherApiUrl = metNoServerBaseUrl + "/" + metNoServerVersion + "/" + metNoApiPath;
  }

  public List<WeatherRecord> retrieveFromMetNoApi(Double latitude, Double longitude)
      throws IOException, InterruptedException {

    // TODO Implement a generic rest client service with builder and common request, response
    // handling.
    URL url = prepareMetApiUrl(latitude, longitude);
    log.debug("requesting weather data from the met.no api {}", url);
    String weatherData;
    try (Response weatherResponse = executeRequest(url)) {

      if (!weatherResponse.isSuccessful()) {
        log.error("failed to get weather data for location {} {}", latitude, longitude);
        return Collections.emptyList();
      }

      log.info("received weather data from the met.no api");

      if (weatherResponse.body() == null) {
        log.error("illegal weather api response, body is null");
        return Collections.emptyList();
      }

      weatherData = weatherResponse.body().string();
    }

    List<WeatherRecord> weatherRecords =
        weatherDataTransformer.transform(weatherData, weatherTransformerQuery);

    if (!weatherRecords.isEmpty()) {
      Location location = new Location(latitude.toString(), longitude.toString());
      cacheService.save(
          CacheService.keyForWeatherDataCache(location), jsonUtil.toJson(weatherRecords));
      log.info("Weather data saved for location: {}", location);
    }

    return weatherRecords;
  }

  @NotNull
  private Response executeRequest(URL url) throws IOException {
    return restClient
        .newCall(
            new Builder()
                .get()
                .url(url)
                .header("User-Agent", "testweatherapp.com support@testweatherapp.com")
                .build())
        .execute();
  }

  @NotNull
  private URL prepareMetApiUrl(Double latitude, Double longitude) throws MalformedURLException {
    return UriComponentsBuilder.fromHttpUrl(
            weatherApiUrl + "?lat=" + latitude + "&lon=" + longitude)
        .build()
        .encode()
        .toUri()
        .toURL();
  }
}
