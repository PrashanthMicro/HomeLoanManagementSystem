
package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.Loan;
import com.capgemini.homeloanmanagementapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomer(User customer);
    Optional<Loan> findByAccountNumber(String accountNumber);
}
