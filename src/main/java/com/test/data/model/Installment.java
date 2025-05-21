package com.test.data.model;

import com.test.enums.InstallmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Generated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "installments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "no_id", nullable = false)
    @Generated(GenerationTime.INSERT) // Indica que lo genera la BD al insertar
    private int noId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "scheduled_payment_date", nullable = false)
    private LocalDate scheduledPaymentDate;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;
}
