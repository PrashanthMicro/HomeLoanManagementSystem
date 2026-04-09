
package com.capgemini.homeloanmanagementapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable=false)
    private String code;

    private String name;
    @Column(length=1000)
    private String description;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private Integer minTenureMonths;
    private Integer maxTenureMonths;

    private BigDecimal annualInterestRate;
}
