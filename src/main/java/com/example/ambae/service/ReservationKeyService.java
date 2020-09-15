package com.example.ambae.service;

import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Component
public class ReservationKeyService
{
  public static final String CACHE_KEY = "ReservationKeyService";

  private final ReservationKeyRepository keyRepo;

  @Cacheable( CACHE_KEY )
  public Long findReservationId( String key ) {
    log.info( "lookup key in DB for date=" + key );
    Optional<ReservationKeyEntity> keyEntity = keyRepo.findByDateKey( key );
    return keyEntity.map( ReservationKeyEntity::getId ).orElse( null );
  }

  @Transactional
  @CacheEvict( CACHE_KEY )
  public void deleteByDateKey( String key )
  {
    keyRepo.deleteByDateKey( key );
  }

  public void save( ReservationKeyEntity buildKey )
  {
    keyRepo.save( buildKey );
  }

}
