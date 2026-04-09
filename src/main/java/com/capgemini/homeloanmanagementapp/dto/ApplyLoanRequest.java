
package com.capgemini.homeloanmanagementapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplyLoanRequest {
    @NotNull private Long productId;
    @NotNull @Positive private BigDecimal amount;
    @NotNull @Positive private Integer tenureMonths;
    @NotBlank private String nomineeName;
    @NotBlank private String nomineeRelation;
    @NotBlank private String nomineeContact;
}
