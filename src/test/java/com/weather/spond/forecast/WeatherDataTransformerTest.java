package com.weather.spond.forecast;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class WeatherDataTransformerTest {

  private static final ObjectMapper objectMapper = createObjectMapper();

  @Test
  void should_parse_data_with_jq_query_processor() throws IOException, InterruptedException {
    WeatherDataTransformer weatherDataTransformer = new WeatherDataTransformer(objectMapper);
    String query =
        ".properties.timeseries"
            + "| map({dateTime: .time, airTemperature: .data.instant.details.air_temperature, windSpeed: .data.instant.details.wind_speed})";
    Resource resource = new ClassPathResource("__files/weather-data.json");
    String data = resource.getContentAsString(StandardCharsets.UTF_8);
    List<WeatherRecord> weatherData = weatherDataTransformer.transform(data, query);
    Assertions.assertThat(weatherData).isNotEmpty();
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.registerModule(new Jdk8Module());
    return objectMapper;
  }
}
