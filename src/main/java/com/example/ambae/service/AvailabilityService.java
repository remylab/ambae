package com.example.ambae.service;

import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AvailabilityService
{
  @Autowired
  private ReservationKeyRepository keyRepo;

  @Cacheable(value="keys", key="#key", unless="#result == null")
  public Long findReservationId( String key ) {
    Optional<ReservationKeyEntity> keyEntity = keyRepo.findByDateKey( key );
    return keyEntity.map( ReservationKeyEntity::getId ).orElse( null );
  }
}
