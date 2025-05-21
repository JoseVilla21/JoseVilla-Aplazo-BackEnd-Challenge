package com.test.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {
    private UUID id;
    private UUID customerId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private PaymentPlanResponse paymentPlan;
}

