
package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
    Optional<LoanProduct> findByCode(String code);
}
