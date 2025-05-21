package com.test.enums;

import java.time.temporal.ChronoUnit;

public enum PaymentScheme {
    SCHEME_1(5, ChronoUnit.WEEKS, 2, 0.13),  // 5 pagos, biweekly (2 semanas), 13%
    SCHEME_2(5, ChronoUnit.WEEKS, 2, 0.16);  // 5 pagos, biweekly (2 semanas), 16%

    private final int numberOfPayments;
    private final ChronoUnit frequencyUnit;
    private final int frequencyAmount; // 2 semanas para biweekly
    private final double interestRate;

    PaymentScheme(int numberOfPayments, ChronoUnit frequencyUnit, int frequencyAmount, double interestRate) {
        this.numberOfPayments = numberOfPayments;
        this.frequencyUnit = frequencyUnit;
        this.frequencyAmount = frequencyAmount;
        this.interestRate = interestRate;
    }

    public int getNumberOfPayments() {
        return numberOfPayments;
    }

    public ChronoUnit getFrequencyUnit() {
        return frequencyUnit;
    }

    public int getFrequencyAmount() {
        return frequencyAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }
}
