package com.example.ambae;

import org.junit.ClassRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public class BaseIntegrationTest
{
  @ClassRule
  public static GenericContainer redisContainer;
  private static final Integer REDIS_PORT = 6379;

  static
  {
    redisContainer = new GenericContainer( "redis:5.0.3-alpine" )
      .withExposedPorts( REDIS_PORT );
    redisContainer.start();
  }

  public static class Initializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext>
  {

    public void initialize( ConfigurableApplicationContext configurableApplicationContext )
    {
      String serverPort = "16999";

      // overrides application configuration with values from the docker container
      TestPropertyValues.of(
        "server.port=" + serverPort,
        "spring.redis.host="+ redisContainer.getContainerIpAddress(),
        "spring.redis.port="+ redisContainer.getMappedPort( REDIS_PORT ),
        "cache.ttl.keys=1"
      ).applyTo( configurableApplicationContext.getEnvironment() );
    }
  }
}
