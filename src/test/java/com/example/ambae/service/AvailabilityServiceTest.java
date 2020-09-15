package com.example.ambae.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class AvailabilityServiceTest
{
  private AvailabilityService service;

  @Mock
  private ReservationKeyService mockKeyService;

  @BeforeEach
  public void setup() {
    service = new AvailabilityService( mockKeyService );
    ReflectionTestUtils.setField( service, "keyService", mockKeyService );
  }

  @Test
  void availabilityOneDay()
  {
    LocalDate tripDay = LocalDate.now().plusDays( 1 );

    // no reservation found, means it's available
    when( mockKeyService.findReservationId( tripDay.toString() ) ).thenReturn( null );

    List<LocalDate> availableDates = service.getAvailability( tripDay, tripDay );
    assertEquals( 1, availableDates.size() );
  }

  @Test
  void invalidDates()
  {
    LocalDate now = LocalDate.now();
    try {
      service.getAvailability( now.plusDays( 2 ), now.plusDays( 1 ) );
      fail();
    } catch ( IllegalArgumentException e ) {
      // ignore
    }
  }

}