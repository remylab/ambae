package com.example.ambae.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners( AuditingEntityListener.class )
@Table( name = "reservation" )
public class ReservationEntity {
  @Id
  @GeneratedValue( strategy = GenerationType.IDENTITY )
  private Long id;

  private LocalDate startDate;
  private LocalDate endDate;

  private String email;
  private String lastName;
  private String firstName;

  @CreatedDate
  @Column(name = "created")
  private LocalDateTime createdDate;

  @LastModifiedDate
  @Column(name = "updated")
  private LocalDateTime updateDate;
}
