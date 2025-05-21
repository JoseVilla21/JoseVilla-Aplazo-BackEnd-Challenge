package com.test.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Generated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "no_id", nullable = false)
    @Generated(GenerationTime.INSERT) // Indica que lo genera la BD al insertar
    private Long noId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "second_last_name", nullable = false)
    private String secondLastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "assigned_credit_amount", precision = 12, scale = 2)
    private BigDecimal assignedCreditAmount;

    @Column (name = "register_date")
    private LocalDateTime registerDate;

    @Column(name = "available_credit_amount", precision = 12, scale = 2)
    private BigDecimal availableCreditAmount;

}
