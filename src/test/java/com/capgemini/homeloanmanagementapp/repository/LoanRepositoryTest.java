package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.Loan;
import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import com.capgemini.homeloanmanagementapp.model.LoanStatus;
import com.capgemini.homeloanmanagementapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepo;

    @Autowired
    private TestEntityManager em;

    // Helper: creates and persists a User
    private User persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("pass");
        u.setEmail(username + "@mail.com");
        u.setFullName(username.toUpperCase());
        return em.persistAndFlush(u);
    }

    // Helper: creates and persists a LoanProduct
    private LoanProduct persistProduct(String code) {
        LoanProduct p = new LoanProduct();
        p.setCode(code);
        p.setName("Product " + code);
        p.setAnnualInterestRate(new BigDecimal("8.5"));
        p.setMinAmount(new BigDecimal("100000"));
        p.setMaxAmount(new BigDecimal("10000000"));
        p.setMinTenureMonths(12);
        p.setMaxTenureMonths(360);
        return em.persistAndFlush(p);
    }

    // Helper: creates and persists a Loan
    private Loan persistLoan(User u, LoanProduct p, String acc) {
        Loan loan = Loan.builder()
                .accountNumber(acc)
                .customer(u)
                .product(p)
                .principalAmount(new BigDecimal("500000"))
                .tenureMonths(240)
                .annualInterestRate(new BigDecimal("8.5"))
                .monthlyEmiAmount(new BigDecimal("4000"))
                .outstandingPrincipal(new BigDecimal("480000"))
                .outstandingEmiCount(239)
                .status(LoanStatus.PENDING_APPROVAL)
                .createdAt(LocalDateTime.now())
                .build();

        return em.persistAndFlush(loan);
    }

    // ---------------------------------------------------------------
    // 1. findByCustomer() returns matching loans
    // ---------------------------------------------------------------
    @Test
    void findByCustomer_returnsCorrectLoans() {
        User alice = persistUser("alice");
        User bob = persistUser("bob");
        LoanProduct product = persistProduct("HOME");

        persistLoan(alice, product, "A1");
        persistLoan(alice, product, "A2");
        persistLoan(bob, product, "B1");

        List<Loan> aliceLoans = loanRepo.findByCustomer(alice);

        assertEquals(2, aliceLoans.size());
        assertTrue(aliceLoans.stream().allMatch(l -> l.getCustomer().getUsername().equals("alice")));
    }

    // ---------------------------------------------------------------
    // 2. findByCustomer() returns empty list when none exist
    // ---------------------------------------------------------------
    @Test
    void findByCustomer_returnsEmptyWhenNoLoansForUser() {
        User u = persistUser("charlie");
        List<Loan> res = loanRepo.findByCustomer(u);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    // ---------------------------------------------------------------
    // 3. findByAccountNumber() returns matching loan
    // ---------------------------------------------------------------
    @Test
    void findByAccountNumber_returnsLoan() {
        User u = persistUser("dave");
        LoanProduct p = persistProduct("AUTO");

        persistLoan(u, p, "ACC123");

        Optional<Loan> found = loanRepo.findByAccountNumber("ACC123");

        assertTrue(found.isPresent());
        assertEquals("ACC123", found.get().getAccountNumber());
    }

    // ---------------------------------------------------------------
    // 4. findByAccountNumber() returns empty when no match
    // ---------------------------------------------------------------
    @Test
    void findByAccountNumber_returnsEmptyForUnknownAcc() {
        Optional<Loan> res = loanRepo.findByAccountNumber("UNKNOWN");

        assertTrue(res.isEmpty());
    }
}