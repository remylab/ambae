package com.example.ambae.service;

import com.example.ambae.dto.ReservationRequest;
import com.example.ambae.model.ReservationEntity;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import com.example.ambae.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService
{
  @Autowired
  private ReservationRepository repo;

  @Autowired
  private ReservationKeyRepository keyRepo;

  @Transactional
  public Long createReservation( ReservationRequest request )
  {
    LocalDate startDate = request.getStartDate();
    LocalDate endDate = request.getEndDate();

    validateDates( startDate, endDate );

    ReservationEntity savedEntity = repo.save( buildEntity( request) );
    Long id = savedEntity.getId();

    long nbDays = DAYS.between( startDate, endDate ) + 1;
    for ( var i = 0; i < nbDays; i++ )
    {
      LocalDate date = startDate.plusDays( i );
      keyRepo.save( buildKey( id, date ) );
    }

    return id;
  }

  private ReservationKeyEntity buildKey( Long id, LocalDate date )
  {
    return ReservationKeyEntity.builder()
        .dateKey( date.toString() )
        .reservationId( id )
        .build();
  }

  private ReservationEntity buildEntity( ReservationRequest request )
  {
    return ReservationEntity.builder()
      .email( request.getEmail() )
      .lastName( request.getLastName() )
      .firstName( request.getFirstName() )
      .startDate( request.getStartDate() )
      .endDate( request.getEndDate() )
      .build();
  }

  public void validateDates( LocalDate startDate, LocalDate endDate )
  {
    if ( startDate.isAfter( endDate ) || startDate.equals( endDate ) ) {
      throw new InvalidParameterException( "endDate must be after startDate" );
    }

    long nbDays = DAYS.between( startDate, endDate ) + 1;
    if ( nbDays > 3 ) {
      throw new InvalidParameterException( "reservations are for maximum 3 days" );
    }
  }
}
