package com.weather.spond.forecast;

import static com.weather.spond.cache.CacheService.keyForWeatherDataCache;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.weather.spond.SpondWeatherApplicationBaseIntegrationTests;
import com.weather.spond.cache.CacheService;
import com.weather.spond.cache.Location;
import com.weather.spond.cache.LocationRegistry;
import com.weather.spond.util.JsonUtil;
import com.weatherspond.api.EventForecast;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

class WeatherApiIntegrationTest extends SpondWeatherApplicationBaseIntegrationTests {

  @Autowired private OkHttpClient restClient;
  @Autowired private JsonUtil jsonUtil;
  @Autowired private CacheService cacheService;
  @Autowired private MetNoWeatherApi metNoWeatherApi;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private LocationRegistry locationRegistry;

  @Test
  void should_report_event_forecast_for_all_new_events() throws IOException {
    stubForMetNoApi();
    Response response = callWeatherApi();
    assertThat(response.isSuccessful()).isTrue();
    List<EventForecast> weatherResponse =
        jsonUtil.fromJson(
            Objects.requireNonNull(response.body()).string(),
            new TypeReference<List<EventForecast>>() {});

    assertThat(weatherResponse).isNotEmpty().hasSize(2);
    for (EventForecast eventForecast : weatherResponse) {
      assertThat(eventForecast.getId()).isNotNull();
      assertThat(eventForecast.getDatetime()).isNotNull();
      assertThat(eventForecast.getAirTemperature()).isNotNull();
      assertThat(eventForecast.getWindSpeed()).isNotNull();
    }
  }

  @Test
  void should_update_latest_forecast_after_scheduler_run()
      throws IOException, InterruptedException {

    // given
    // Event id 101, time 2024-12-08T15:00:00Z, Old data air_temp -1.2  wind_speed 5.6, New data
    // after scheduler run air_temp -4.2 wind_speed2.6
    // Event id 102, time 2024-12-11T11:00:00Z, Old data air_temp 2.0, wind_speed 10.0, New data
    // after scheduler run air_temp -10.2 wind_speed0.6

    stubForMetNoApi();

    // Update old data in cache for event id 101 and 102
    WeatherDataTransformer weatherDataTransformer = new WeatherDataTransformer(objectMapper);
    String query =
        ".properties.timeseries"
            + "| map({dateTime: .time, airTemperature: .data.instant.details.air_temperature, windSpeed: .data.instant.details.wind_speed})";
    Resource resource = new ClassPathResource("__files/weather-data-old.json");
    String data = resource.getContentAsString(StandardCharsets.UTF_8);
    Location location = new Location("60.5", "11.59");
    locationRegistry.registerLocation(location);

    cacheService.save(
        keyForWeatherDataCache(location),
        jsonUtil.toJson(weatherDataTransformer.transform(data, query)));

    // Call weather api
    Response response = callWeatherApi();
    assertThat(response.isSuccessful()).isTrue();
    List<EventForecast> weatherResponse =
        jsonUtil.fromJson(
            Objects.requireNonNull(response.body()).string(),
            new TypeReference<List<EventForecast>>() {});
    response.close();

    // Assert old data
    assertThat(weatherResponse).isNotEmpty().hasSize(2);
    assertThat(weatherResponse.get(0).getId()).isNotNull().isEqualTo(101);
    assertThat(weatherResponse.get(0).getAirTemperature()).isNotNull().isEqualTo(-1.2f);
    assertThat(weatherResponse.get(0).getWindSpeed()).isNotNull().isEqualTo(5.6f);

    assertThat(weatherResponse.get(1).getId()).isNotNull().isEqualTo(102);
    assertThat(weatherResponse.get(1).getAirTemperature()).isNotNull().isEqualTo(2.0f);
    assertThat(weatherResponse.get(1).getWindSpeed()).isNotNull().isEqualTo(10.0f);

    // Wait for scheduler to run
    Awaitility.await()
        .atLeast(Duration.ofMillis(5000))
        .pollDelay(Duration.ofMillis(5000))
        .untilAsserted(() -> Assertions.assertTrue(true));

    // when
    // Call weather api again
    callWeatherApi();
    Response responseAfterScheduler = callWeatherApi();
    assertThat(response.isSuccessful()).isTrue();
    List<EventForecast> weatherResponseAfterSchedulerRun =
        jsonUtil.fromJson(
            Objects.requireNonNull(responseAfterScheduler.body()).string(),
            new TypeReference<List<EventForecast>>() {});
    responseAfterScheduler.close();

    // Assert new data
    assertThat(weatherResponseAfterSchedulerRun).isNotEmpty().hasSize(2);
    assertThat(weatherResponseAfterSchedulerRun.get(0).getId()).isNotNull().isEqualTo(101);
    assertThat(weatherResponseAfterSchedulerRun.get(0).getAirTemperature())
        .isNotNull()
        .isEqualTo(-4.2f);
    assertThat(weatherResponseAfterSchedulerRun.get(0).getWindSpeed()).isNotNull().isEqualTo(2.6f);

    assertThat(weatherResponseAfterSchedulerRun.get(1).getId()).isNotNull().isEqualTo(102);
    assertThat(weatherResponseAfterSchedulerRun.get(1).getAirTemperature())
        .isNotNull()
        .isEqualTo(-10.2f);
    assertThat(weatherResponseAfterSchedulerRun.get(1).getWindSpeed()).isNotNull().isEqualTo(0.6f);
  }

  private @NotNull Response callWeatherApi() throws IOException {
    return restClient
        .newCall(
            new Builder()
                .post(
                    RequestBody.create(
                        getWeatherApiRequestBody(), okhttp3.MediaType.parse("application/json")))
                .url(
                    "http://localhost:8080"
                        + "/api/weather/forecast"
                        + "?lat="
                        + "60.5"
                        + "&lon="
                        + "11.59")
                .build())
        .execute();
  }

  private static void stubForMetNoApi() {
    mockWeatherServer.stubFor(
        WireMock.get(WireMock.urlPathMatching("/2.0/complete"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("weather-data.json")));
  }

  private static @NotNull String getWeatherApiRequestBody() {
    return """
              {
                  "events": [
                      {
                          "id": 101,
                          "start": "2024-12-08T15:00:00Z",
                          "end": "2024-12-08T18:00:00Z"
                      },
                      {
                          "id": 102,
                          "start": "2024-12-11T11:00:00Z",
                          "end": "2024-12-11T18:00:00Z"
                      }
                  ]
              }
        """;
  }
}
