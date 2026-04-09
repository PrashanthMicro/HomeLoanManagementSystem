
package com.capgemini.homeloanmanagementapp.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmiCalcResponse {
    private BigDecimal monthlyEmi;
    private BigDecimal totalPayable;
    private BigDecimal totalInterest;
}
