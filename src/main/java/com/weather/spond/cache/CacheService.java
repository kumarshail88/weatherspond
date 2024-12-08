package com.weather.spond.cache;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {

  private static final String WEATHER_DATA_CACHE_KEY_PREFIX = "weatherDataCache";

  private long ttl;

  private TimeUnit timeUnit;

  private final RedisTemplate<String, Object> redisTemplate;

  public CacheService(
      RedisTemplate<String, Object> redisTemplate, @Value("${spring.redis.ttl}") long ttl) {
    this.redisTemplate = redisTemplate;
    this.ttl = ttl;
    this.timeUnit = TimeUnit.MILLISECONDS;
  }

  public void save(String key, Object value) {
    redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
  }

  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public static String keyForWeatherDataCache(Location location) {
    return WEATHER_DATA_CACHE_KEY_PREFIX + "_" + location.latitude() + "_" + location.longitude();
  }
}
