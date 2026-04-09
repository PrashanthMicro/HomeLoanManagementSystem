
package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
    List<LoanPayment> findByLoanId(Long loanId);
}
