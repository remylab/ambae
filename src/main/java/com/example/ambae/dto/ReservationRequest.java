package com.example.ambae.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor( staticName = "of" )
@NoArgsConstructor( force = true, access = PRIVATE ) // for deserialization
public class ReservationRequest
{
  @NotBlank
  String email;
  @NotBlank
  String lastName;
  String firstName;

  @JsonFormat(pattern="yyyy-MM-dd")
  LocalDate startDate;
  @JsonFormat(pattern="yyyy-MM-dd")
  LocalDate endDate;
}