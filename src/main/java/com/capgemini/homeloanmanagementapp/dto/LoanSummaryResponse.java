
package com.capgemini.homeloanmanagementapp.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanSummaryResponse {
    private String accountNumber;
    private String productCode;
    private String productName;
    private BigDecimal principalAmount;
    private Integer tenureMonths;
    private BigDecimal annualInterestRate;
    private BigDecimal monthlyEmiAmount;
    private String status;
    private LocalDateTime createdAt;
    private String trackerMessage;
}
