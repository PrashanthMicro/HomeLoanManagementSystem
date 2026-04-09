package com.capgemini.homeloanmanagementapp.service;

import com.capgemini.homeloanmanagementapp.dto.*;
import com.capgemini.homeloanmanagementapp.exception.ForbiddenException;
import com.capgemini.homeloanmanagementapp.exception.NotFoundException;
import com.capgemini.homeloanmanagementapp.model.*;
import com.capgemini.homeloanmanagementapp.repository.LoanRepository;
import com.capgemini.homeloanmanagementapp.util.EmiCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    private LoanRepository loanRepo;
    private LoanProductService productService;
    private UserService userService;
    private LoanService service;

    @BeforeEach
    void setup() {
        loanRepo = mock(LoanRepository.class);
        productService = mock(LoanProductService.class);
        userService = mock(UserService.class);
        service = new LoanService(loanRepo, productService, userService);
    }

    // -------------------------------------------------------------
    // calculateEmi
    // -------------------------------------------------------------
    @Test
    void calculateEmi_success() {
        EmiCalcRequest req = mock(EmiCalcRequest.class);
        when(req.getAmount()).thenReturn(new BigDecimal("100000"));
        when(req.getAnnualRatePercent()).thenReturn(new BigDecimal("10"));
        when(req.getTenureMonths()).thenReturn(12);

        try (MockedStatic<EmiCalculator> mocked = mockStatic(EmiCalculator.class)) {
            mocked.when(() -> EmiCalculator.monthlyEmi(new BigDecimal("100000"), new BigDecimal("10"), 12))
                    .thenReturn(new BigDecimal("8791.59"));

            EmiCalcResponse res = service.calculateEmi(req);

            assertEquals(new BigDecimal("8791.59"), res.getMonthlyEmi());
            assertEquals(new BigDecimal("105499.08"), res.getTotalPayable());
            assertEquals(new BigDecimal("5499.08"), res.getTotalInterest());
        }
    }

    // -------------------------------------------------------------
    // applyLoan
    // -------------------------------------------------------------
    @Test
    void applyLoan_success() {
        String username = "user";

        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        when(req.getProductId()).thenReturn(1L);
        when(req.getAmount()).thenReturn(new BigDecimal("500000"));
        when(req.getTenureMonths()).thenReturn(240);
        when(req.getNomineeName()).thenReturn("John");
        when(req.getNomineeRelation()).thenReturn("Brother");
        when(req.getNomineeContact()).thenReturn("9999");

        User u = new User();
        u.setUsername(username);

        LoanProduct product = new LoanProduct();
        product.setId(1L);
        product.setCode("HOME");
        product.setName("Home Loan");
        product.setMinAmount(new BigDecimal("100000"));
        product.setMaxAmount(new BigDecimal("900000"));
        product.setMinTenureMonths(12);
        product.setMaxTenureMonths(360);
        product.setAnnualInterestRate(new BigDecimal("8.5"));

        when(userService.findByUsername(username)).thenReturn(u);
        when(productService.getById(1L)).thenReturn(product);

        try (MockedStatic<EmiCalculator> mocked = mockStatic(EmiCalculator.class)) {
            mocked.when(() -> EmiCalculator.monthlyEmi(new BigDecimal("500000"), new BigDecimal("8.5"), 240))
                    .thenReturn(new BigDecimal("4000"));

            when(loanRepo.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

            Loan saved = service.applyLoan(username, req);

            assertEquals(u, saved.getCustomer());
            assertEquals(product, saved.getProduct());
            assertEquals(new BigDecimal("4000"), saved.getMonthlyEmiAmount());
            assertTrue(saved.getAccountNumber().startsWith("HL-"));
        }
    }

    @Test
    void applyLoan_invalidAmount_throwsForbidden() {
        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        when(req.getProductId()).thenReturn(1L);
        when(req.getAmount()).thenReturn(new BigDecimal("10"));
        when(req.getTenureMonths()).thenReturn(100);

        User user = new User();

        LoanProduct product = new LoanProduct();
        product.setMinAmount(new BigDecimal("100"));
        product.setMaxAmount(new BigDecimal("1000"));
        product.setMinTenureMonths(10);
        product.setMaxTenureMonths(200);

        when(userService.findByUsername(anyString())).thenReturn(user);
        when(productService.getById(1L)).thenReturn(product);

        assertThrows(ForbiddenException.class, () -> service.applyLoan("x", req));
        verify(loanRepo, never()).save(any());
    }

    @Test
    void applyLoan_invalidTenure_throwsForbidden() {
        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        when(req.getProductId()).thenReturn(1L);
        when(req.getAmount()).thenReturn(new BigDecimal("500"));
        when(req.getTenureMonths()).thenReturn(5);

        User user = new User();

        LoanProduct product = new LoanProduct();
        product.setMinAmount(new BigDecimal("100"));
        product.setMaxAmount(new BigDecimal("1000"));
        product.setMinTenureMonths(10);
        product.setMaxTenureMonths(200);

        when(userService.findByUsername(anyString())).thenReturn(user);
        when(productService.getById(1L)).thenReturn(product);

        assertThrows(ForbiddenException.class, () -> service.applyLoan("x", req));
    }

    // -------------------------------------------------------------
    // listLoansForUserOrAdmin
    // -------------------------------------------------------------
    @Test
    void listLoans_admin() {
        LoanProduct p = new LoanProduct();
        p.setCode("HOME");
        p.setName("Home Loan");

        User owner = new User();
        owner.setUsername("bob");

        Loan recent = Loan.builder()
                .accountNumber("A1")
                .customer(owner)
                .product(p)
                .principalAmount(BigDecimal.ONE)
                .tenureMonths(1)
                .annualInterestRate(BigDecimal.ONE)
                .monthlyEmiAmount(BigDecimal.ONE)
                .outstandingPrincipal(BigDecimal.ONE)
                .outstandingEmiCount(1)
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        Loan old = Loan.builder()
                .accountNumber("A2")
                .customer(owner)
                .product(p)
                .principalAmount(BigDecimal.ONE)
                .tenureMonths(1)
                .annualInterestRate(BigDecimal.ONE)
                .monthlyEmiAmount(BigDecimal.ONE)
                .outstandingPrincipal(BigDecimal.ONE)
                .outstandingEmiCount(1)
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(20))
                .build();

        when(loanRepo.findAll()).thenReturn(List.of(recent, old));

        List<LoanSummaryResponse> list = service.listLoansForUserOrAdmin("admin", true);

        assertEquals(2, list.size());
        assertNotNull(list.get(0).getTrackerMessage());
        assertNull(list.get(1).getTrackerMessage());
    }

    @Test
    void listLoans_user() {
        User u = new User();
        u.setUsername("alice");

        LoanProduct p = new LoanProduct();
        p.setCode("H");

        Loan loan = Loan.builder()
                .accountNumber("ACC")
                .customer(u)
                .product(p)
                .principalAmount(BigDecimal.ONE)
                .tenureMonths(10)
                .annualInterestRate(BigDecimal.ONE)
                .monthlyEmiAmount(BigDecimal.ONE)
                .outstandingPrincipal(BigDecimal.ONE)
                .outstandingEmiCount(10)
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.findByUsername("alice")).thenReturn(u);
        when(loanRepo.findByCustomer(u)).thenReturn(List.of(loan));

        List<LoanSummaryResponse> list = service.listLoansForUserOrAdmin("alice", false);

        assertEquals(1, list.size());
        assertEquals("ACC", list.get(0).getAccountNumber());
    }

    // -------------------------------------------------------------
    // getLoanDetailByAccount
    // -------------------------------------------------------------
    @Test
    void loanDetail_success() {
        User u = new User();
        u.setUsername("alice");

        LoanProduct p = new LoanProduct();
        p.setName("Home Loan");

        Nominee nominee = Nominee.builder()
                .name("John")
                .relation("Bro")
                .contactNumber("123")
                .build();

        Loan loan = Loan.builder()
                .accountNumber("ACC100")
                .customer(u)
                .product(p)
                .principalAmount(BigDecimal.ONE)
                .tenureMonths(12)
                .annualInterestRate(BigDecimal.ONE)
                .monthlyEmiAmount(BigDecimal.ONE)
                .outstandingPrincipal(BigDecimal.ONE)
                .outstandingEmiCount(1)
                .status(LoanStatus.ACTIVE)
                .nominee(nominee)
                .createdAt(LocalDateTime.now())
                .build();

        when(loanRepo.findByAccountNumber("ACC100")).thenReturn(Optional.of(loan));

        LoanDetailResponse detail = service.getLoanDetailByAccount("alice", false, "ACC100");

        assertEquals("ACC100", detail.getAccountNumber());
        assertEquals("Home Loan", detail.getLoanType());
        assertEquals("John", detail.getNomineeName());
    }

    @Test
    void loanDetail_notFound() {
        when(loanRepo.findByAccountNumber("X")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getLoanDetailByAccount("u", false, "X"));
    }

    @Test
    void loanDetail_forbidden() {
        User owner = new User();
        owner.setUsername("owner");

        Loan loan = Loan.builder()
                .accountNumber("A")
                .customer(owner)
                .product(new LoanProduct())
                .principalAmount(BigDecimal.ONE)
                .tenureMonths(1)
                .annualInterestRate(BigDecimal.ONE)
                .monthlyEmiAmount(BigDecimal.ONE)
                .outstandingPrincipal(BigDecimal.ONE)
                .outstandingEmiCount(1)
                .status(LoanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(loanRepo.findByAccountNumber("A")).thenReturn(Optional.of(loan));

        assertThrows(ForbiddenException.class, () -> service.getLoanDetailByAccount("notOwner", false, "A"));
    }

    // -------------------------------------------------------------
    // activateLoanAsAdmin
    // -------------------------------------------------------------
    @Test
    void activate_success() {
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.PENDING_APPROVAL);
        loan.setAccountNumber("A");

        when(loanRepo.findByAccountNumber("A")).thenReturn(Optional.of(loan));

        service.activateLoanAsAdmin("A");

        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        verify(loanRepo).save(loan);
    }

    @Test
    void activate_notFound() {
        when(loanRepo.findByAccountNumber("A")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.activateLoanAsAdmin("A"));
    }
}