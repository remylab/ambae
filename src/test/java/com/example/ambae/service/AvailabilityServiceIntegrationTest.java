package com.example.ambae.service;

import com.example.ambae.AmbaeApplication;
import com.example.ambae.CacheableTest;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest( webEnvironment = DEFINED_PORT )
@ContextConfiguration( classes = { AmbaeApplication.class },
  initializers = { AvailabilityServiceIntegrationTest.Initializer.class } )
class AvailabilityServiceIntegrationTest
  extends CacheableTest
{
  @Autowired
  CacheManager cacheManager;

  @Autowired
  ReservationKeyRepository keyRepository;

  @Autowired
  private AvailabilityService service;

  private Cache cacheKeys;

  @BeforeEach
  public void setup()
  {
    cacheKeys = cacheManager.getCache( "keys" );
  }
  @Test
  void testFindReservationId()
    throws InterruptedException
  {
    String dateKey = "2020-09-07";

    ReservationKeyEntity entity = ReservationKeyEntity.builder()
      .dateKey( dateKey )
      .reservationId( 123L )
      .build();
    keyRepository.save( entity );

    assertNull( cacheKeys.get( dateKey ) );

    service.findReservationId( dateKey );

    // verify key is cached for 1s only
    assertNotNull( cacheKeys.get( dateKey ) );
    Thread.sleep( 500L );
    assertNotNull( cacheKeys.get( dateKey ) );
    assertNotNull( cacheKeys.get( dateKey ) );

    Thread.sleep( 500L );
    assertNull( cacheKeys.get( dateKey ) );
  }
}