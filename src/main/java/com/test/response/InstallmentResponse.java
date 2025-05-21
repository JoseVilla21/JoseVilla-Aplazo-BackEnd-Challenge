package com.test.response;

import com.test.enums.InstallmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentResponse{
    private BigDecimal amount;
    private LocalDate scheduledPaymentDate;
    private InstallmentStatus status;
    
}
