package com.example.ambae.repository;

import com.example.ambae.model.ReservationEntity;
import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository
  extends CrudRepository<ReservationEntity, Long>
{
}
