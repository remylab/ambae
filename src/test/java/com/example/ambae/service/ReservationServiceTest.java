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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration( classes = { AmbaeApplication.class, ReservationService.class } )
class ReservationServiceTest
{
  private static final LocalDate nowDate = LocalDate.now();

  @Autowired
  private ReservationService service;

  @Autowired
  private ReservationKeyRepository keyRepo;

  @Test
  void testCreateReservationTooManyDays() {

    ReservationRequest request = buildRequest( "SMITH",
                                               "smith@gmail.com",
                                               nowDate,
                                               nowDate.plusDays( 3 ) );

    try {
      service.createReservation( request );
      fail();
    } catch ( InvalidParameterException e ) {
      //ignore
    }
  }

  @Test
  void testCreateReservation_alreadyBooked() {

    LocalDate endDate = nowDate.plusDays( 2 );

    Long existingReservationId = 22L;
    ReservationKeyEntity existingKey = ReservationKeyEntity.builder()
      .dateKey( endDate.toString() )
      .reservationId( existingReservationId )
      .build();

    keyRepo.save( existingKey );

    ReservationRequest request = buildRequest( "SMITH","smith@gmail.com", nowDate, endDate );

    try {
      service.createReservation( request );
      fail();
    } catch ( DataIntegrityViolationException e ) {
      //ignore
    }
  }

  @Test
  void testCreateReservation() {

    ReservationRequest request = buildRequest( "SMITH","smith@gmail.com", nowDate, nowDate.plusDays( 2 ) );

    Long reservationId = service.createReservation( request );

    ReservationKeyEntity key1 = keyRepo.findByDateKey( nowDate.toString() ).orElseThrow();
    ReservationKeyEntity key2 = keyRepo.findByDateKey( nowDate.plusDays( 1 ).toString() ).orElseThrow();
    ReservationKeyEntity key3 = keyRepo.findByDateKey( nowDate.plusDays( 2 ).toString() ).orElseThrow();

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