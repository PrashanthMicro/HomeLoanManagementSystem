package com.capgemini.homeloanmanagementapp.controller;

import com.capgemini.homeloanmanagementapp.dto.LoanProductResponse;
import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import com.capgemini.homeloanmanagementapp.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                       // <-- added
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j                                                  // <-- added
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductService productService;

    @GetMapping
    public List<LoanProductResponse> list() {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/products requested");
        List<LoanProductResponse> result = productService.getAll()
                .stream()
                .map(this::toDto)
                .toList();
        log.info("GET /api/products delivered count={} ({} ms)", result.size(), (System.currentTimeMillis() - t0));
        return result;
    }

    @GetMapping("/{id}")
    public LoanProductResponse detail(@PathVariable("id") Long id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/products/{} requested", id);
        LoanProductResponse dto = toDto(productService.getById(id));
        log.info("GET /api/products/{} served ({} ms)", id, (System.currentTimeMillis() - t0));
        return dto;
    }

    private LoanProductResponse toDto(LoanProduct p) {
        // lightweight debug line to help trace mapping without logging entire entity
        log.debug("Mapping LoanProduct to DTO: id={}, code={}", p.getId(), p.getCode());
        return LoanProductResponse.builder()
                .id(p.getId()).code(p.getCode()).name(p.getName()).description(p.getDescription())
                .minAmount(p.getMinAmount()).maxAmount(p.getMaxAmount())
                .minTenureMonths(p.getMinTenureMonths()).maxTenureMonths(p.getMaxTenureMonths())
                .annualInterestRate(p.getAnnualInterestRate())
                .build();
    }
}