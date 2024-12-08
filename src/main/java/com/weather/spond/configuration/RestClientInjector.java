package com.weather.spond.configuration;

import java.time.Duration;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientInjector {

  @Value("30000")
  private int readTimeout;

  @Value("1000")
  private int connectionTimeout;

  @Value("NONE")
  private HttpLoggingInterceptor.Level requestLoggingLevel;

  @Bean
  public OkHttpClient restClient() {
    return createOkHttpClient();
  }

  private OkHttpClient createOkHttpClient() {

    var loggingInterceptor = new HttpLoggingInterceptor();
    return new OkHttpClient.Builder()
        .followRedirects(false)
        .addInterceptor(loggingInterceptor)
        .readTimeout(Duration.ofMillis(readTimeout))
        .connectTimeout(Duration.ofMillis(connectionTimeout))
        .build();
  }
}
