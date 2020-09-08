package com.example.ambae.repository;

import com.example.ambae.model.ReservationKeyEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ReservationKeyRepositoryTest
{

  @Autowired
  private ReservationKeyRepository repo;

  @Test
  void testCreate() {

    ReservationKeyEntity entity = ReservationKeyEntity.builder()
      .dateKey( "2020-09-07" )
      .reservationId( 123L )
      .build();

    repo.save( entity );

    ReservationKeyEntity savedEntity = repo.findByDateKey( "2020-09-07" ).orElseThrow();
    assertNotNull( savedEntity.getDateKey() );
    assertTrue( repo.findByDateKey( "2020-09-06" ).isEmpty() );
  }

}