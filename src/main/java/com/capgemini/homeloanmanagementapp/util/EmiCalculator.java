
package com.capgemini.homeloanmanagementapp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EmiCalculator {

    /**
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     */
    public static BigDecimal monthlyEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(12 * 100.0), 12, RoundingMode.HALF_UP);
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyRate)).pow(tenureMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
