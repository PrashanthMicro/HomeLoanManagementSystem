
package com.capgemini.homeloanmanagementapp.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanDetailResponse {
    private String accountNumber;
    private String loanType;
    private String nomineeName;
    private String nomineeRelation;
    private String nomineeContact;
    private BigDecimal totalLoanAmount;
    private Integer loanTenureMonths;
    private BigDecimal currentRateOfInterest;
    private BigDecimal principalOutstandingAmount;
    private Integer outstandingEmiCount;
    private BigDecimal monthlyEmiAmount;
    private String status;
    private LocalDateTime createdAt;
}
