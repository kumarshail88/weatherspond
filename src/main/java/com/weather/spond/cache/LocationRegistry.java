package com.weather.spond.cache;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LocationRegistry {

  // Local cache for scheduler only to keep the size of active location registry fixed.
  // This cache is not shared with other components.
  private final SimpleLRUCache<Location, OffsetDateTime> reigstry = new SimpleLRUCache<>(100);

  public boolean isLocationRegistered(Location location) {
    return reigstry.containsKey(location);
  }

  public void registerLocation(Location location) {
    OffsetDateTime now = OffsetDateTime.now();
    reigstry.put(location, now);
  }

  public Map<Location, OffsetDateTime> getRegisteredLocations() {
    return Map.copyOf(reigstry);
  }

  private static class SimpleLRUCache<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    public SimpleLRUCache(int capacity) {
      super(capacity, 0.75f, true);
      this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > capacity;
    }
  }
}
