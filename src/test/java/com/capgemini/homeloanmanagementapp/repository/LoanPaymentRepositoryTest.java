package com.capgemini.homeloanmanagementapp.repository;

import com.capgemini.homeloanmanagementapp.model.Loan;
import com.capgemini.homeloanmanagementapp.model.LoanPayment;
import com.capgemini.homeloanmanagementapp.model.LoanProduct;
import com.capgemini.homeloanmanagementapp.model.LoanStatus;
import com.capgemini.homeloanmanagementapp.model.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.shadow.com.univocity.parsers.conversions.Conversions.toUpperCase;

@DataJpaTest
class LoanPaymentRepositoryTest {

    @Autowired
    private LoanPaymentRepository repo;

    @Autowired
    private TestEntityManager em;

    // ---------- Helpers ----------

    private User persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("pass");
        u.setEmail(username + "@mail.com");
       toUpperCase();
        return em.persistAndFlush(u);
    }

    private LoanProduct persistProduct() {
        LoanProduct p = new LoanProduct();
        p.setCode("HOME");
        p.setName("Home Loan");
        p.setAnnualInterestRate(new BigDecimal("8.50"));
        p.setMinAmount(new BigDecimal("100000"));
        p.setMaxAmount(new BigDecimal("10000000"));
        p.setMinTenureMonths(12);
        p.setMaxTenureMonths(360);
        return em.persistAndFlush(p);
    }

    private Loan persistLoan(User u, LoanProduct p, String acc) {
        Loan loan = Loan.builder()
                .accountNumber(acc)
                .customer(u)
                .product(p)
                .principalAmount(new BigDecimal("500000"))
                .tenureMonths(240)
                .annualInterestRate(new BigDecimal("8.5"))
                .monthlyEmiAmount(new BigDecimal("4500"))
                .outstandingPrincipal(new BigDecimal("490000"))
                .outstandingEmiCount(239)
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        return em.persistAndFlush(loan);
    }

    private LoanPayment persistPayment(Loan loan, LocalDate date, String principal, String interest, String emi, boolean paid) {
        LoanPayment p = LoanPayment.builder()
                .loan(loan)
                .dueDate(date)
                .principalComponent(new BigDecimal(principal))
                .interestComponent(new BigDecimal(interest))
                .totalEmi(new BigDecimal(emi))
                .paid(paid)
                .build();
        return em.persistAndFlush(p);
    }

    // ---------- TESTS ----------

    @Test
    void findByLoanId_returnsMatchingPayments() {
        User u = persistUser("alice");
        LoanProduct p = persistProduct();
        Loan loanA = persistLoan(u, p, "ACC1");
        Loan loanB = persistLoan(u, p, "ACC2");

        persistPayment(loanA, LocalDate.now(), "1000", "200", "1200", true);
        persistPayment(loanA, LocalDate.now().plusDays(1), "1000", "180", "1180", false);
        persistPayment(loanB, LocalDate.now(), "900", "100", "1000", false);

        List<LoanPayment> results = repo.findByLoanId(loanA.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(pmt -> pmt.getLoan().getId().equals(loanA.getId())));
    }

    @Test
    void findByLoanId_returnsEmptyList_whenNoPayments() {
        User u = persistUser("bob");
        LoanProduct p = persistProduct();
        Loan loan = persistLoan(u, p, "ACC3");

        List<LoanPayment> results = repo.findByLoanId(loan.getId());

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void save_and_findById_workCorrectly() {
        User u = persistUser("tom");
        LoanProduct p = persistProduct();
        Loan loan = persistLoan(u, p, "ACC500");

        LoanPayment payment = persistPayment(loan, LocalDate.now(), "500", "50", "550", true);

        LoanPayment found = repo.findById(payment.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(new BigDecimal("500"), found.getPrincipalComponent());
        assertEquals(new BigDecimal("50"), found.getInterestComponent());
        assertEquals(loan.getId(), found.getLoan().getId());
    }

    @Test
    void delete_removesPayment() {
        User u = persistUser("sam");
        LoanProduct p = persistProduct();
        Loan loan = persistLoan(u, p, "ACC800");

        LoanPayment payment = persistPayment(loan, LocalDate.now(), "200", "20", "220", false);

        repo.delete(payment);

        assertTrue(repo.findById(payment.getId()).isEmpty());
    }
}