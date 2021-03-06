package com.example.ambae.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@AllArgsConstructor
@Service
public class AvailabilityService
{
  private static final long DEFAULT_NB_DAYS = 30;
  private static final long MAX_TOTAL_DAYS = 90;

  private final ReservationKeyService keyService;

  public List<LocalDate> getAvailability()
  {
    LocalDate startDate = LocalDate.now();
    // including today it will make a total of 30 days to evaluate
    LocalDate endDate = startDate.plusDays( DEFAULT_NB_DAYS - 1L );
    return getAvailability( startDate, endDate );
  }

  public List<LocalDate> getAvailability( LocalDate startDate, LocalDate endDate ){

    LocalDate today = LocalDate.now();
    if ( startDate.isBefore( today ) ) {
      startDate = today;
    }

    validateDates( startDate, endDate );
    List<LocalDate> availableDates = newArrayList();

    for (LocalDate date = startDate; date.compareTo( endDate ) <= 0; date = date.plusDays(1)) {
      if ( keyService.findReservationId( date.toString() ) == null ) {
        availableDates.add( date );
      }
    }
    return availableDates;
  }

  private void validateDates( LocalDate startDate, LocalDate endDate )
  {
    LocalDate today = LocalDate.now();
    long daysInAdvance = DAYS.between( today, endDate )+1;

    if ( daysInAdvance > MAX_TOTAL_DAYS ) {
      throw new InvalidParameterException( "cannot fetch availability for more than " + MAX_TOTAL_DAYS + " days" );
    }

    if ( endDate.compareTo( startDate ) < 0 )
    {
      throw new InvalidParameterException( "endDate must be after startDate" );
    }
  }

}
