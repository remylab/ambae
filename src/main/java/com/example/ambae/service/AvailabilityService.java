package com.example.ambae.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
public class AvailabilityService
{
  @Autowired
  private ReservationKeyCache keyCache;

  public List<LocalDate> getAvailability()
  {
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusDays( 30 );
    return getAvailability( startDate, endDate );
  }

  public List<LocalDate> getAvailability( LocalDate startDate, LocalDate endDate ){

    validateDates( startDate, endDate );
    List<LocalDate> availableDates = newArrayList();

    long nbDays = DAYS.between( startDate, endDate ) + 1;
    for ( var i = 0; i < nbDays; i++ )
    {
      LocalDate date = startDate.plusDays( i );
      if ( keyCache.findReservationId(  date.toString() ) == null ) {
        availableDates.add( date );
      }
    }
    return availableDates;
  }

  private void validateDates( LocalDate startDate, LocalDate endDate )
  {
    if ( startDate.isAfter( endDate ) )
    {
      throw new InvalidParameterException( "endDate must be after startDate" );
    }
  }

}
