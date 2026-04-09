package com.capgemini.homeloanmanagementapp.controller;

import com.capgemini.homeloanmanagementapp.dto.LoanProductResponse;
import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import com.capgemini.homeloanmanagementapp.service.LoanProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanProductControllerTest {

    @Mock
    private LoanProductService productService;

    @InjectMocks
    private LoanProductController controller;

    private LoanProduct product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = LoanProduct.builder()
                .id(1L)
                .code("HL001")
                .name("Home Loan")
                .description("Test Home Loan")
                .minAmount(BigDecimal.valueOf(100000))
                .maxAmount(BigDecimal.valueOf(500000))
                .minTenureMonths(12)
                .maxTenureMonths(240)
                .annualInterestRate(BigDecimal.valueOf(8.5))
                .build();
    }

    @Test
    void testList() {
        when(productService.getAll()).thenReturn(List.of(product));

        List<LoanProductResponse> response = controller.list();

        assertEquals(1, response.size());
        LoanProductResponse dto = response.get(0);

        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getCode(), dto.getCode());
        assertEquals(product.getName(), dto.getName());
        assertEquals(product.getDescription(), dto.getDescription());
        assertEquals(product.getMinAmount(), dto.getMinAmount());
        assertEquals(product.getMaxAmount(), dto.getMaxAmount());
        assertEquals(product.getMinTenureMonths(), dto.getMinTenureMonths());
        assertEquals(product.getMaxTenureMonths(), dto.getMaxTenureMonths());
        assertEquals(product.getAnnualInterestRate(), dto.getAnnualInterestRate());

        verify(productService, times(1)).getAll();
    }

    @Test
    void testDetail() {
        when(productService.getById(1L)).thenReturn(product);

        LoanProductResponse dto = controller.detail(1L);

        assertNotNull(dto);
        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getCode(), dto.getCode());
        assertEquals(product.getName(), dto.getName());

        verify(productService, times(1)).getById(1L);
    }
}
