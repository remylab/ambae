package com.example.ambae.service;

import com.example.ambae.model.ReservationEntity;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import com.example.ambae.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service
public class ReservationService
{
  @Autowired
  private ReservationRepository repo;

  @Autowired
  private ReservationKeyRepository keyRepo;

  @Transactional
  public Long createReservation( ReservationEntity reservationEntity )
  {
    try
    {
      return doCreateReservation( reservationEntity );
    } catch ( DataIntegrityViolationException e )
    {
      return -1L;
    }
  }

  private Long doCreateReservation( ReservationEntity reservationEntity )
  {
    ReservationEntity savedEntity = repo.save( reservationEntity );

    Long id = reservationEntity.getId();

    keyRepo.save( buildKey( id, savedEntity.getStartDate() ) );
    keyRepo.save( buildKey( id, savedEntity.getEndDate() ) );

    return id;
  }

  private ReservationKeyEntity buildKey( Long id, LocalDate keyDate )
  {
    return ReservationKeyEntity.builder()
        .dateKey( keyDate.toString() )
        .reservationId( id )
        .build();
  }
}
