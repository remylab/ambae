package com.example.ambae.controller;

import com.example.ambae.dto.ReservationRequest;
import com.example.ambae.service.AvailabilityService;
import com.example.ambae.service.ReservationService;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Validated
@AllArgsConstructor
@Controller
public class MainController
{
  @Autowired
  private final AvailabilityService availabilityService;

  @Autowired
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
  public Long createReservation( @Valid @RequestBody ReservationRequest request ) {
    return reservationService.createReservation( request );
  }

}
