package com.weather.spond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpondWeatherApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpondWeatherApplication.class, args);
  }
}
