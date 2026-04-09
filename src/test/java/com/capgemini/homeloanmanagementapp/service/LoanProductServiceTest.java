package com.capgemini.homeloanmanagementapp.service;

import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import com.capgemini.homeloanmanagementapp.repository.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanProductServiceTest {

    private LoanProductRepository productRepo;
    private LoanProductService service;

    @BeforeEach
    void setup() {
        productRepo = mock(LoanProductRepository.class);
        service = new LoanProductService(productRepo);
    }

    // ------------------------------------------------------------
    // 1. getAll() returns products successfully
    // ------------------------------------------------------------
    @Test
    void getAll_returnsList() {
        LoanProduct p1 = new LoanProduct();
        LoanProduct p2 = new LoanProduct();

        when(productRepo.findAll()).thenReturn(List.of(p1, p2));

        List<LoanProduct> list = service.getAll();

        assertNotNull(list);
        assertEquals(2, list.size());
        verify(productRepo, times(1)).findAll();
    }

    // ------------------------------------------------------------
    // 2. getById() returns product successfully
    // ------------------------------------------------------------
    @Test
    void getById_returnsProduct() {
        LoanProduct product = new LoanProduct();
        product.setId(10L);
        product.setCode("HOME_STD");

        when(productRepo.findById(10L)).thenReturn(Optional.of(product));

        LoanProduct result = service.getById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("HOME_STD", result.getCode());
        verify(productRepo, times(1)).findById(10L);
    }

    // ------------------------------------------------------------
    // 3. getById() throws when missing
    // ------------------------------------------------------------
    @Test
    void getById_whenNotFound_throwsException() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> service.getById(99L));

        assertEquals("Product not found", ex.getMessage());
        verify(productRepo, times(1)).findById(99L);
    }
}