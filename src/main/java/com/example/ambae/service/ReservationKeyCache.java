package com.example.ambae.service;

import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@CacheConfig( cacheNames = "ReservationKeyCache" )
public class ReservationKeyCache
{
  @Autowired
  private ReservationKeyRepository keyRepo;

  @Cacheable
  public Long findReservationId( String key ) {
    log.info( "lookup key in DB for date=" + key );
    Optional<ReservationKeyEntity> keyEntity = keyRepo.findByDateKey( key );
    return keyEntity.map( ReservationKeyEntity::getId ).orElse( null );
  }
}
