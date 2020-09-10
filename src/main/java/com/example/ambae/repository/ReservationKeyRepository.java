package com.example.ambae.repository;

import com.example.ambae.model.ReservationKeyEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ReservationKeyRepository
  extends CrudRepository<ReservationKeyEntity, String>
{
  Optional<ReservationKeyEntity> findByDateKey( String dateKey );
  void deleteByDateKey( String dateKey );
}
