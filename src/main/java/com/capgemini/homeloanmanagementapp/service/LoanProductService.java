package com.capgemini.homeloanmanagementapp.service;

import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import com.capgemini.homeloanmanagementapp.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                    // <-- added
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j                                              // <-- added
@Service
@RequiredArgsConstructor
public class LoanProductService {
    private final LoanProductRepository productRepo;

    public List<LoanProduct> getAll() {
        long t0 = System.currentTimeMillis();
        log.info("Fetching all loan products");
        List<LoanProduct> products = productRepo.findAll();
        log.info("Fetched loan products: count={} ({} ms)", products.size(), (System.currentTimeMillis() - t0));
        return products;
    }

    public LoanProduct getById(Long id) {
        long t0 = System.currentTimeMillis();
        log.info("Fetching loan product by id={}", id);
        LoanProduct product = productRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Loan product not found for id={}", id);
                    return new RuntimeException("Product not found");
                });
        log.debug("Found loan product: id={}, code={}", product.getId(), product.getCode());
        log.info("Fetched loan product id={} in {} ms", id, (System.currentTimeMillis() - t0));
        return product;
    }
}