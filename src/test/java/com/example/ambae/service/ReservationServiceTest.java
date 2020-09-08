package com.example.ambae.service;

import com.example.ambae.AmbaeApplication;
import com.example.ambae.model.ReservationEntity;
import com.example.ambae.model.ReservationKeyEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration( classes = { AmbaeApplication.class, ReservationService.class } )
class ReservationServiceTest
{
  @Autowired
  private ReservationService service;

  @Autowired
  private ReservationKeyRepository keyRepo;

  private static final LocalDate nowDate = LocalDate.now();

  @Test
  void testCreateReservation_alreadyBooked() {

    LocalDate endDate = nowDate.plusDays( 2 );

    Long existingReservationId = 22L;
    ReservationKeyEntity existingKey = ReservationKeyEntity.builder()
      .dateKey( endDate.toString() )
      .reservationId( existingReservationId )
      .build();

    keyRepo.save( existingKey );

    ReservationEntity entity = ReservationEntity.builder()
      .startDate( nowDate )
      .endDate( endDate )
      .email( "smith@gmail.com")
      .lastName( "SMITH" )
      .build();

    assertEquals( -1L, service.createReservation( entity ) );
  }

  @Test
  void testCreateReservation() {

    ReservationEntity entity = ReservationEntity.builder()
      .startDate( nowDate )
      .endDate(  nowDate.plusDays( 2 ) )
      .email( "smith@gmail.com")
      .lastName( "SMITH" )
      .build();

    Long reservationId = service.createReservation( entity );

    ReservationKeyEntity keyStart = keyRepo.findByDateKey( entity.getStartDate().toString() ).orElseThrow();
    ReservationKeyEntity keyEnd = keyRepo.findByDateKey( entity.getEndDate().toString() ).orElseThrow();

    assertEquals( reservationId, keyStart.getReservationId() );
    assertEquals( reservationId, keyEnd.getReservationId() );
  }

}