
package com.capgemini.homeloanmanagementapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private Loan loan;

    private LocalDate dueDate;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal totalEmi;
    private boolean paid;
}
