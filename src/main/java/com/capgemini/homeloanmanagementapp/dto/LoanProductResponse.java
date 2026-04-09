
package com.capgemini.homeloanmanagementapp.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTenureMonths;
    private Integer maxTenureMonths;
    private BigDecimal annualInterestRate;
}
