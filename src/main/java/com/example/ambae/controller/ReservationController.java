package com.example.ambae.controller;

import com.example.ambae.dto.ReservationRequest;
import com.example.ambae.model.ReservationEntity;
import com.example.ambae.service.AvailabilityService;
import com.example.ambae.service.ReservationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Controller
public class ReservationController
{
  private final AvailabilityService availabilityService;
  private final ReservationService reservationService;

  @GetMapping( path = "/availability" )
  @ResponseBody
  public List<LocalDate> getAvailability( ) {
    return availabilityService.getAvailability();
  }

  @GetMapping( path = "/availability/{startDate}/{endDate}" )
  @ResponseBody
  public List<LocalDate> getAvailabilityWithDates( @PathVariable( name = "startDate" ) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                   @PathVariable( name = "endDate" ) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate ) {
    return availabilityService.getAvailability( startDate, endDate );
  }

  @PostMapping( path = "/reservations" )
  @ResponseBody
  public ResponseEntity<Long> createReservation( @Valid @RequestBody ReservationRequest request ) {
    return ResponseEntity.status( HttpStatus.CREATED ).body( reservationService.createReservation( request ) );
  }

  @GetMapping( path = "/reservations/{id}" )
  @ResponseBody
  public ReservationEntity getReservation( @PathVariable( name = "id" ) long id  ) {
    return reservationService.getSingle( id );
  }

  @DeleteMapping( path = "/reservations/{id}" )
  public ResponseEntity<Void> deleteReservation( @PathVariable( name = "id" ) long id ) {
    reservationService.deleteReservation( id );
    return ResponseEntity.status( HttpStatus.NO_CONTENT ).build();
  }

  @PutMapping( path = "/reservations/{id}" )
  public ResponseEntity<Void> updateReservation( @PathVariable( name = "id" ) long id,
                                 @Valid @RequestBody ReservationRequest request ) {
    reservationService.updateReservation( id, request );
    return ResponseEntity.status( HttpStatus.NO_CONTENT ).build();
  }

}
