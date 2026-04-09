
package com.capgemini.homeloanmanagementapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(indexes = {
    @Index(name="idx_loan_accountNumber", columnList = "accountNumber", unique = true),
    @Index(name="idx_loan_user", columnList = "customer_id")
})
public class Loan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String accountNumber;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    private User customer;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    private LoanProduct product;

    private BigDecimal principalAmount;
    private Integer tenureMonths;
    private BigDecimal annualInterestRate;

    private BigDecimal monthlyEmiAmount;
    private BigDecimal outstandingPrincipal;
    private Integer outstandingEmiCount;

    @Embedded
    private Nominee nominee;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private LocalDateTime createdAt;
}
