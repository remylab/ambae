package com.example.ambae.controller;

import com.example.ambae.AmbaeApplication;
import com.example.ambae.BaseIntegrationTest;
import com.example.ambae.dto.ReservationRequest;
import com.example.ambae.model.ReservationEntity;
import com.example.ambae.repository.ReservationKeyRepository;
import com.example.ambae.repository.ReservationRepository;
import com.example.ambae.service.ReservationKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.collect.Iterables;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Slf4j
@RunWith( SpringRunner.class )
@SpringBootTest( webEnvironment = DEFINED_PORT )
@DirtiesContext( classMode = ClassMode.AFTER_CLASS ) // to ensure the app is shutdown after the test suite
@AutoConfigureMockMvc
@ContextConfiguration( classes = { AmbaeApplication.class },
  initializers = { ReservationControllerIntegrationTest.Initializer.class } )
class ReservationControllerIntegrationTest
  extends BaseIntegrationTest
{
  private final static LocalDate TODAY = LocalDate.now();

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private ReservationKeyRepository keyRepo;

  @Autowired
  private ReservationRepository repo;

  @Autowired
  private CacheManager cacheManager;
  private Cache cacheKeys;

  @BeforeEach
  public void setup() {
    cacheKeys = cacheManager.getCache( ReservationKeyService.CACHE_KEY );
    cacheKeys.clear();
  }

  @Test
  void testCreateMissingEmail()
    throws Exception
  {
    ReservationRequest request = buildRequest( TODAY.plusDays( 2 ), TODAY.plusDays( 4 ) );
    request.setEmail( null );

    String content = mapper.writeValueAsString( request );
    MvcResult res = mockMvc.perform( post( "/reservations" )
                                       .contentType( APPLICATION_JSON )
                                       .content( content ) )
      .andReturn();

    assertEquals( HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus() );
  }

  @Test
  void testCreateOK()
    throws Exception
  {
    ReservationRequest request = buildRequest( TODAY.plusDays( 2 ), TODAY.plusDays( 4 ) );

    MvcResult res = mockMvc.perform( post( "/reservations" )
                       .contentType( APPLICATION_JSON )
                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.CREATED.value(), res.getResponse().getStatus() );
    assertNotNull( Long.valueOf( res.getResponse().getContentAsString() ) );
  }

  @Test
  void testCreateArrivalToday_shouldFail()
    throws Exception
  {
    ReservationRequest request = buildRequest( TODAY, TODAY.plusDays( 1 ) );

    MvcResult res = mockMvc.perform( post( "/reservations" )
                                       .contentType( APPLICATION_JSON )
                                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus() );
    assertEquals( "reservations must be done at least 1 day in advance", res.getResponse().getContentAsString() );
  }

  @Test
  void testCreateMoreThan3Days_shouldFail()
    throws Exception
  {
    ReservationRequest request = buildRequest( TODAY.plusDays( 1 ), TODAY.plusDays( 4 ) );

    MvcResult res = mockMvc.perform( post( "/reservations" )
                                       .contentType( APPLICATION_JSON )
                                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus() );
    assertEquals( "reservations are for maximum 3 days", res.getResponse().getContentAsString() );
  }

  @Test
  void testCreateArrivalInThePast_shouldFail()
    throws Exception
  {
    ReservationRequest request = buildRequest( TODAY.minusDays( 3 ), TODAY );

    MvcResult res = mockMvc.perform( post( "/reservations" )
                                       .contentType( APPLICATION_JSON )
                                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus() );
  }

  @Test
  void testCreateArrivalMoreThanOnMonth_shouldFail()
    throws Exception
  {
    ReservationRequest request = buildRequest( TODAY.plusDays( 31 ), TODAY.plusDays( 32 ) );

    MvcResult res = mockMvc.perform( post( "/reservations" )
                                       .contentType( APPLICATION_JSON )
                                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus() );
    assertEquals( "reservations cannot be done more than 1 month in advance",
                  res.getResponse().getContentAsString() );
  }

  @Test
  void testCancelUnknownReservation_shouldFail()
    throws Exception
  {
    MvcResult res = mockMvc.perform( delete( "/reservations/1" ) )
      .andReturn();
    assertEquals( HttpStatus.NOT_FOUND.value(), res.getResponse().getStatus() );
  }

  @Test
  void testUpdateOK()
    throws Exception
  {
    LocalDate startDate = TODAY.plusDays( 3 );
    LocalDate endDate = TODAY.plusDays( 5 );

    Long id = createReservation( startDate, endDate );

    LocalDate newStartDate = endDate.plusDays( 2 );
    LocalDate newEndDate = endDate.plusDays( 3 );

    ReservationRequest request = buildRequest( newStartDate, newEndDate );

    String newEmail = "new@mail.me";
    String newLastName = "newdoe";

    request.setEmail( newEmail );
    request.setLastName( newLastName );

    MvcResult res = mockMvc.perform( put( "/reservations/" + id  )
                       .contentType( APPLICATION_JSON )
                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.NO_CONTENT.value(), res.getResponse().getStatus() );

    // verify update in DB
    ReservationEntity updatedEntity = repo.findById( id ).orElseThrow();
    assertEquals( newEmail, updatedEntity.getEmail() );
    assertEquals( newLastName, updatedEntity.getLastName() );
    assertEquals( newStartDate, updatedEntity.getStartDate() );
    assertEquals( newEndDate, updatedEntity.getEndDate() );

    // also verify keys have been added
    assertEquals( id, keyRepo.findByDateKey( newStartDate.toString() ).orElseThrow().getReservationId() );
    assertEquals( id, keyRepo.findByDateKey( newEndDate.toString() ).orElseThrow().getReservationId() );
  }

  @Test
  void testCancelOK()
    throws Exception
  {
    Long reservationId = createReservation();
    assertNotNull( reservationId );

    MvcResult resDelete = mockMvc.perform( delete( "/reservations/" + reservationId ) ).andReturn();
    assertEquals( HttpStatus.NO_CONTENT.value(), resDelete.getResponse().getStatus() );
  }

  @Test
  void testGetSingleOK()
    throws Exception
  {
    Long reservationId = createReservation();
    assertNotNull( reservationId );

    MvcResult res = mockMvc.perform( get( "/reservations/" + reservationId ) ).andReturn();
    assertEquals( HttpStatus.OK.value(), res.getResponse().getStatus() );

    ReservationEntity entity = mapper.readValue( res.getResponse().getContentAsString(), ReservationEntity.class );
    assertEquals( "douda", entity.getLastName() );
  }

  @Test
  void testGetAvailabilityDefault()
    throws Exception
  {
    LocalDate startDate = TODAY.plusDays( 5 );
    createReservation( startDate, TODAY.plusDays( 6 ) );

    // before: no cache
    assertNull( cacheKeys.get( startDate.toString() ) );

    MvcResult res = mockMvc.perform( get( "/availability" ) ).andReturn();
    assertEquals( HttpStatus.OK.value(), res.getResponse().getStatus() );

    List<String> availableDates = Arrays.asList( mapper.readValue( res.getResponse().getContentAsString(),
                                                                   String[].class ) );

    // default is 30 days but we created one reservation for 2 DAYS already
    assertEquals( 28, availableDates.size() );

    // after : cache is populated
    assertNotNull( cacheKeys.get( startDate.toString() ) );
  }

  @Test
  void testGetAvailabilityByDates_moreThan90Days_shouldFail()
    throws Exception
  {
    LocalDate startDate = TODAY;
    LocalDate endDate = TODAY.plusDays( 90 );

    MvcResult res = mockMvc.perform( get( "/availability/" + startDate + "/" + endDate ) )
      .andReturn();
    assertEquals( HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus() );
    assertTrue( res.getResponse().getContentAsString().startsWith( "cannot fetch availability for more than") );
  }

  @Test
  void testGetAvailabilityByDates_startInPast()
    throws Exception
  {
    LocalDate startDate = TODAY.minusDays( 10 );
    LocalDate endDate = TODAY.plusDays( 2 );

    MvcResult res = mockMvc.perform( get( "/availability/" + startDate + "/" + endDate ) )
      .andReturn();
    assertEquals( HttpStatus.OK.value(), res.getResponse().getStatus() );

    List<String> availableDates = Arrays.asList( mapper.readValue( res.getResponse().getContentAsString(),
                                                                   String[].class ) );

    // startDate should default to today
    assertEquals( 3, availableDates.size() );
  }

  @Test
  void testGetAvailabilityByDates()
    throws Exception
  {
    LocalDate startDate = TODAY.plusDays( 1 );
    LocalDate endDate = startDate.plusDays( 2 );
    createReservation( startDate, endDate );

    MvcResult res = mockMvc.perform( get( "/availability/" + startDate + "/" + endDate.plusDays( 3 ) ) )
      .andReturn();
    assertEquals( HttpStatus.OK.value(), res.getResponse().getStatus() );

    List<String> availableDates = Arrays.asList( mapper.readValue( res.getResponse().getContentAsString(),
                                                                   String[].class ) );

    // 3 first days are booked with the reservation
    assertEquals( 3, availableDates.size() );
  }

  @Test
  void testConcurrentCreate()
  {
    IntStream.range( 0, 10 ).parallel().forEach( (i) -> {
      try {
        log.info( "try concurrent create timestamp=" + Instant.now().toString() );
        createReservationUnchecked();
      } catch ( Exception e ) {
        fail();
      }
    } );

    // should have managed to create only one reservation
    assertEquals( 1, Iterables.size( repo.findAll() ) );
    assertEquals( 1, Iterables.size( keyRepo.findAll() ) );
  }

  @Test
  void testConcurrentUpdate()
    throws Exception
  {
    Long id1 = createReservation( TODAY.plusDays( 2 ), TODAY.plusDays( 4 ) );
    Long id2 = createReservation( TODAY.plusDays( 10 ), TODAY.plusDays( 12 ) );

    LocalDate updateStartDate = TODAY.plusDays( 3 );
    LocalDate updateEndDate = TODAY.plusDays( 5 );

    IntStream.range( 0, 2 ).parallel().forEach( (i) -> {
      try {
        long id = i % 2 == 0 ? id1 : id2;
        log.info( "try concurrent update id=" + id + " time=" + Instant.now().toString() );
        updateReservationUnchecked( id, updateStartDate, updateEndDate );
      } catch ( Exception e ) {
        fail();
      }
    } );

    ReservationEntity entity1 = repo.findById( id1 ).orElseThrow();
    ReservationEntity entity2 = repo.findById( id1 ).orElseThrow();

    ReservationEntity winner = entity1.getStartDate().equals( updateStartDate ) ? entity1 : entity2;

    assertEquals( updateStartDate, winner.getStartDate() );
    assertEquals( updateEndDate, winner.getEndDate() );
  }

  private void updateReservationUnchecked( long id, LocalDate startDate, LocalDate endDate )
    throws Exception
  {
    ReservationRequest request = buildRequest( startDate, endDate );
    mockMvc.perform( put( "/reservations/" + id  )
                       .contentType( APPLICATION_JSON )
                       .content( mapper.writeValueAsString( request ) ) );
  }

  private void createReservationUnchecked( )
    throws Exception
  {
    LocalDate startDate = TODAY.plusDays( 2 );
    ReservationRequest request = buildRequest( startDate, startDate );

    mockMvc.perform( post( "/reservations" )
                       .contentType( APPLICATION_JSON )
                       .content( mapper.writeValueAsString( request ) ) );
  }

  private Long createReservation()
    throws Exception
  {
    return createReservation( TODAY.plusDays( 2 ), TODAY.plusDays( 4 ) );
  }

  private Long createReservation( LocalDate startDate, LocalDate endDate )
    throws Exception
  {
    ReservationRequest request = buildRequest( startDate, endDate );

    MvcResult res = mockMvc.perform( post( "/reservations" )
                                       .contentType( APPLICATION_JSON )
                                       .content( mapper.writeValueAsString( request ) ) )
      .andReturn();

    assertEquals( HttpStatus.CREATED.value(), res.getResponse().getStatus() );

    return Long.valueOf( res.getResponse().getContentAsString() );
  }

  private ReservationRequest buildRequest( LocalDate startDate, LocalDate endDate )
  {
    return ReservationRequest.builder()
      .email( "mail@me.com" )
      .firstName( "remy" )
      .lastName( "douda" )
      .startDate( startDate )
      .endDate( endDate )
      .build();
  }

  @AfterEach
  public void cleanup() {
    keyRepo.deleteAll();
    repo.deleteAll();
  }



}