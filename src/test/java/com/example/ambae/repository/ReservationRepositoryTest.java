package com.example.ambae.repository;

import com.example.ambae.model.ReservationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReservationRepositoryTest {

  @Autowired
  private ReservationRepository repo;
  private static final LocalDate nowDate = LocalDate.now();

  @Test
  void testCreate() {

    ReservationEntity entity = ReservationEntity.builder()
      .startDate( nowDate )
      .endDate(  nowDate.plusDays( 2 ) )
      .email( "smith@gmail.com")
      .lastName( "SMITH" )
      .build();
    ReservationEntity savedEntity = repo.save( entity );

    assertNotNull( savedEntity.getId() );
    assertNotNull( savedEntity.getCreatedDate() );
  }

}