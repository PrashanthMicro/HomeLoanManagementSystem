package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LoanProductRepositoryTest {

    @Autowired
    private LoanProductRepository repository;

    @Test
    void testFindByCode_found() {

        // Given
        LoanProduct product = LoanProduct.builder()

                .code("HL001")
                .name("Home Loan Basic")
                .description("Test product")
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(500000))
                .minTenureMonths(12)
                .maxTenureMonths(240)
                .annualInterestRate(BigDecimal.valueOf(7.5))
                .build();

        repository.save(product);

        // When
        Optional<LoanProduct> result = repository.findByCode("HL001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("HL001", result.get().getCode());
        assertEquals("Home Loan Basic", result.get().getName());
    }

    @Test
    void testFindByCode_notFound() {
        // When
        Optional<LoanProduct> result = repository.findByCode("DOES_NOT_EXIST");

        // Then
        assertTrue(result.isEmpty());
    }
}