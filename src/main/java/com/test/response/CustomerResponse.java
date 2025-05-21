package com.test.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CustomerResponse {
    private UUID idClient;
    private BigDecimal creditAmount;
    private BigDecimal availableCreditAmount;
    private String createdAt;

}
