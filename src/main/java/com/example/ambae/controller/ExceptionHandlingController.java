package com.example.ambae.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandlingController
{
  @ResponseStatus( HttpStatus.CONFLICT )
  @ExceptionHandler( DataIntegrityViolationException.class)
  @ResponseBody String handleConflict() {
    // should be handled with custom exception to provide accurate details
    return "there was an error when saving this reservation";
  }

  @ResponseStatus( HttpStatus.BAD_REQUEST )
  @ExceptionHandler( IllegalArgumentException.class)
  @ResponseBody String handleBadRequest( Exception ex ) {
    return ex.getMessage();
  }
}
