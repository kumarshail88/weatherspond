package com.weather.spond.forecast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WeatherDataTransformer {

  private final ProcessBuilder processBuilder;

  private final ObjectMapper objectMapper;

  public WeatherDataTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.processBuilder = new ProcessBuilder();
  }

  public List<WeatherRecord> transform(String json, String query)
      throws IOException, InterruptedException {
    processBuilder.command("jq", query);
    try {
      Process process = startJQProcess();
      sendCommand(json, process);
      return result(process);
    } catch (IOException | InterruptedException e) {
      log.error("failed to process JSON query", e);
      throw e;
    }
  }

  private List<WeatherRecord> result(Process process)
      throws InterruptedException, JsonProcessingException {
    try {
      InputStream inputStream = process.getInputStream();
      String output =
          new BufferedReader(new InputStreamReader(inputStream))
              .lines()
              .collect(Collectors.joining("\n"));
      process.waitFor();
      // log.debug("jq output: {}", output);
      endJQProcess(process);
      return objectMapper.readValue(output, new TypeReference<List<WeatherRecord>>() {});
    } catch (JsonProcessingException | InterruptedException e) {
      log.error("jq process interrupted", e);
      throw e;
    }
  }

  private static void sendCommand(String json, Process process) throws IOException {
    try (OutputStream outputStream = process.getOutputStream()) {
      outputStream.write(json.getBytes());
      outputStream.flush();
    } catch (IOException e) {
      log.error("failed to write json to jq process", e);
      throw e;
    }
  }

  private Process startJQProcess() throws IOException {
    Process process;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      log.error("failed to start jq process", e);
      throw e;
    }
    return process;
  }

  private void endJQProcess(Process process) {
    process.destroy();
  }
}
