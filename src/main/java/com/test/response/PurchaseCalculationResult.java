package com.test.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseCalculationResult {
    private LocalDate purchaseDate;
    private BigDecimal commissionAmount;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal installmentAmount;
    private List<LocalDate> dueDates;
}
