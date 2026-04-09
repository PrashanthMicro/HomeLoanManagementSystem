
package com.capgemini.homeloanmanagementapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmiCalcRequest {
    @NotNull @Positive private BigDecimal amount;
    @NotNull @Positive private BigDecimal annualRatePercent;
    @NotNull @Positive private Integer tenureMonths;
}
