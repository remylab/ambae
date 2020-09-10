package com.example.ambae.config;

import com.example.ambae.service.ReservationKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig
{
  @Value( "${cache.ttl.keys:300}" )
  private long keysTtl;

  @Bean
  RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    return builder -> {
      RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
      Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
      configurationMap.put( ReservationKeyService.CACHE_KEY, redisCacheConfiguration.entryTtl( Duration.ofSeconds( keysTtl ) ));
      builder.withInitialCacheConfigurations( configurationMap );
    };
  }


}
