package com.example.ambae.service;

import com.example.ambae.AmbaeApplication;
import com.example.ambae.dto.ReservationRequest;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;

import java.security.InvalidParameterException;
import java.time.LocalDate;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ContextConfiguration( classes = { AmbaeApplication.class, ReservationService.class, ReservationKeyService.class } )
class ReservationServiceTest
{
  private static final LocalDate nowDate = LocalDate.now();

  @Autowired
  private ReservationService service;

  @Autowired
  private ReservationKeyRepository keyRepo;

  @Test
  void testUpdateReservationOneDayDiff_shouldUpdateKeys() {

    LocalDate previousStartDate = nowDate.plusDays( 4 );
    LocalDate previousEndDate = nowDate.plusDays( 6 );

    LocalDate newStartDate = nowDate.plusDays( 3 );
    LocalDate newEndDate = nowDate.plusDays( 5 );

    ReservationRequest request = buildRequest( "SMITH",
                                               "smith@gmail.com",
                                               previousStartDate,
                                               previousEndDate );

    Long reservationId = service.createReservation( request );

    ReservationRequest updateRequest = buildRequest( "SMITH",
                                                     "new-smith@gmail.com",
                                                     newStartDate,
                                                     newEndDate );

    service.updateReservation( reservationId, updateRequest );

    // new dates should be there
    assertEquals( reservationId, keyRepo.findByDateKey( newStartDate.toString() ).orElseThrow().getReservationId() );
    assertEquals( reservationId, keyRepo.findByDateKey( newStartDate.plusDays( 1 ).toString() ).orElseThrow().getReservationId() );
    assertEquals( reservationId, keyRepo.findByDateKey( newEndDate.toString() ).orElseThrow().getReservationId() );

    // old end date should be removed
    assertTrue( keyRepo.findByDateKey( previousEndDate.toString() ).isEmpty() );
  }

  @Test
  void testCreateReservationOneDay() {

    LocalDate tripDay = nowDate.plusDays( 2 );

    ReservationRequest request = buildRequest( "SMITH",
                                               "smith@gmail.com",
                                               tripDay,
                                               tripDay );

    Long reservationId = service.createReservation( request );

    ReservationKeyEntity key1 = keyRepo.findByDateKey( tripDay.toString() ).orElseThrow();
    assertEquals( reservationId, key1.getReservationId() );
  }

  @Test
  void testCreateReservationTooFarAhead_shouldThrow() {

    ReservationRequest request = buildRequest( "SMITH",
                                               "smith@gmail.com",
                                               nowDate.plusDays( 31 ),
                                               nowDate.plusDays( 33 ) );

    assertThrows( InvalidParameterException.class, () -> service.createReservation( request ) );
  }

  @Test
  void testCreateReservationTooLate_shouldThrow() {

    ReservationRequest request = buildRequest( "SMITH",
                                               "smith@gmail.com",
                                               nowDate,
                                               nowDate.plusDays( 3 ) );

    assertThrows( InvalidParameterException.class, () -> service.createReservation( request ) );
  }

  @Test
  void testCreateReservationTooManyDays_shouldThrow() {

    ReservationRequest request = buildRequest( "SMITH",
                                               "smith@gmail.com",
                                               nowDate.plusDays( 1 ),
                                               nowDate.plusDays( 4 ) );

    assertThrows( InvalidParameterException.class, () -> service.createReservation( request ) );
  }

  @Test
  void testCreateReservation_endDateAlreadyBooked_shouldThrow() {

    LocalDate startDate = nowDate.plusDays( 1 );
    LocalDate endDate = nowDate.plusDays( 2 );

    // adding end date in keys
    ReservationKeyEntity existingKey = ReservationKeyEntity.builder()
      .dateKey( endDate.toString() )
      .reservationId( 22L )
      .build();
    keyRepo.save( existingKey );

    ReservationRequest request = buildRequest( "SMITH", "smith@gmail.com", startDate, endDate );

    assertThrows( DataIntegrityViolationException.class, () -> service.createReservation( request ) );
  }

  @Test
  void testCreateReservation() {

    LocalDate startDate = nowDate.plusDays( 1 );
    LocalDate endDate = startDate.plusDays( 2 );
    ReservationRequest request = buildRequest( "SMITH","smith@gmail.com", startDate, endDate );

    Long reservationId = service.createReservation( request );

    ReservationKeyEntity key1 = keyRepo.findByDateKey( startDate.toString() ).orElseThrow();
    ReservationKeyEntity key2 = keyRepo.findByDateKey( startDate.plusDays( 1 ).toString() ).orElseThrow();
    ReservationKeyEntity key3 = keyRepo.findByDateKey( startDate.plusDays( 2 ).toString() ).orElseThrow();

    // save all dates from start to end
    assertEquals( reservationId, key1.getReservationId() );
    assertEquals( reservationId, key2.getReservationId() );
    assertEquals( reservationId, key3.getReservationId() );
  }

  private ReservationRequest buildRequest( String email, String lastName, LocalDate startDate, LocalDate endDate  ) {
    return ReservationRequest.builder()
      .startDate( startDate )
      .endDate( endDate )
      .email( email )
      .lastName( lastName )
      .build();
  }
}