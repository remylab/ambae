package com.example.ambae.service;

import com.example.ambae.AmbaeApplication;
import com.example.ambae.BaseIntegrationTest;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest( webEnvironment = DEFINED_PORT )
@ContextConfiguration( classes = { AmbaeApplication.class },
  initializers = { ReservationKeyServiceIntegrationTest.Initializer.class } )
class ReservationKeyServiceIntegrationTest
  extends BaseIntegrationTest
{
  @Autowired
  ReservationKeyRepository keyRepository;

  @Autowired
  private ReservationKeyService keyService;

  @Autowired
  CacheManager cacheManager;
  private Cache cacheKeys;

  @BeforeEach
  public void setup() {
    cacheKeys = cacheManager.getCache( ReservationKeyService.CACHE_KEY );
    cacheKeys.clear();
  }

  @Test
  void testSaveAndDelete_shouldRemoveFromCache()
  {
    String dateKey = "2020-09-07";

    ReservationKeyEntity entity = ReservationKeyEntity.builder()
      .dateKey( dateKey )
      .reservationId( 123L )
      .build();
    keyRepository.save( entity );

    assertNull( cacheKeys.get( dateKey ) );

    keyService.findReservationId( dateKey );

    // verify key is in cache
    assertNotNull( cacheKeys.get( dateKey ) );
    keyService.deleteByDateKey( dateKey );

    // verify key is remove from cache directly after a delete
    assertNull( cacheKeys.get( dateKey ) );
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

    keyService.findReservationId( dateKey );

    // verify key is cached for 1s only
    assertNotNull( cacheKeys.get( dateKey ) );
    Thread.sleep( 500L );
    assertNotNull( cacheKeys.get( dateKey ) );
    assertNotNull( cacheKeys.get( dateKey ) );

    Thread.sleep( 505L );
    assertNull( cacheKeys.get( dateKey ) );
  }
}