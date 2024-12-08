package com.weather.spond;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.File;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext
public class SpondWeatherApplicationBaseIntegrationTests {

  public static final WireMockServer mockWeatherServer = startWireMockServer();

  public static ComposeContainer environment = getComposeContainer();

  @DynamicPropertySource
  static void dataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("MET_NO_BASE_URL", mockWeatherServer::baseUrl);
  }

  private static ComposeContainer getComposeContainer() {
    ComposeContainer composeContainer =
        new ComposeContainer(new File("docker-compose/compose-test.yaml"))
            .withExposedService("redis-1", 6379);

    composeContainer.start();
    return composeContainer;
  }

  private static WireMockServer startWireMockServer() {
    WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    server.start();
    return server;
  }
}
