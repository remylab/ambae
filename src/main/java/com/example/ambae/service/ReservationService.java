package com.example.ambae.service;

import com.example.ambae.dto.ReservationRequest;
import com.example.ambae.model.ReservationEntity;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService
{
  private static final String RESERVATION_NOT_FOUND = "reservation not found";

  @Autowired
  private ReservationRepository repo;

  @Autowired
  private ReservationKeyService keyService;

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
      keyService.save( buildKey( id, date ) );
    }

    return id;
  }

  @Transactional
  public void updateReservation( long reservationId, ReservationRequest request )
  {
    LocalDate newStartDate = request.getStartDate();
    LocalDate newEndDate = request.getEndDate();

    // validate new dates
    validateDates( newStartDate, newEndDate );

    ReservationEntity entity = repo.findById( reservationId )
      .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND, RESERVATION_NOT_FOUND ) );

    Set<LocalDate> previousDates = buildDateSet( entity.getStartDate(), entity.getEndDate() );
    Set<LocalDate> newsDates = buildDateSet( newStartDate, newEndDate );
    newsDates.removeAll( previousDates );
    previousDates.removeAll( buildDateSet( newStartDate, newEndDate ) );

    // removing previous dates from keys
    for ( LocalDate date : previousDates ) {
      keyService.deleteByDateKey( date.toString() );
    }
    // adding new dates from keys
    for ( LocalDate date : newsDates ) {
      keyService.save( buildKey( entity.getId(), date ) );
    }

    // update reservation details
    entity.setStartDate( newStartDate );
    entity.setEndDate( newEndDate );
    entity.setEmail( request.getEmail() );
    entity.setLastName( request.getLastName() );
    entity.setFirstName( request.getFirstName() );
    repo.save( entity );
  }

  public ReservationEntity getSingle( long id ){
    return repo.findById( id )
      .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND, RESERVATION_NOT_FOUND ) );
  }

  private Set<LocalDate> buildDateSet( LocalDate startDate, LocalDate endDate )
  {
    Set<LocalDate> dates = new HashSet<>();
    long nbDays = DAYS.between( startDate, endDate ) + 1;
    for ( var i = 0; i < nbDays; i++ )
    {
      dates.add( startDate.plusDays( i ) );
    }
    return dates;
  }

  @Transactional
  public void deleteReservation( long reservationId )
  {
    ReservationEntity entity = repo.findById( reservationId )
      .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND, RESERVATION_NOT_FOUND ) );

    LocalDate startDate = entity.getStartDate();
    LocalDate endDate = entity.getEndDate();

    long nbDays = DAYS.between( startDate, endDate ) + 1;
    for ( var i = 0; i < nbDays; i++ )
    {
      LocalDate date = startDate.plusDays( i );
      keyService.deleteByDateKey( date.toString() );
    }
    repo.delete( entity );
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
    LocalDate today = LocalDate.now();

    if ( startDate.isBefore( today ) ) {
      throw new InvalidParameterException( "do we have flying cars yet ???" );
    }
    if ( startDate.isAfter( endDate ) ) {
      throw new InvalidParameterException( "departure date must after arrival date" );
    }

    long daysInAdvance = DAYS.between( today, startDate );
    if ( daysInAdvance < 1 ) {
      throw new InvalidParameterException( "reservations must be done at least 1 day in advance" );
    }

    if ( daysInAdvance > 30 ) {
      throw new InvalidParameterException( "reservations cannot be done more than 1 month in advance" );
    }

    long nbDays = DAYS.between( startDate, endDate ) + 1;
    if ( nbDays > 3 ) {
      throw new InvalidParameterException( "reservations are for maximum 3 days" );
    }
  }
}
