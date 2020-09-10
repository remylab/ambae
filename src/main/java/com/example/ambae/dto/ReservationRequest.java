package com.example.ambae.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Data // @Value is immutable
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