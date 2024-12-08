package com.weather.spond;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SpondWeatherAppTest extends SpondWeatherApplicationBaseIntegrationTests {

  @Test
  public void test_setup() {
    Assertions.assertThat(environment.getServiceHost("redis-1", 7379)).isNotBlank();
  }
}
