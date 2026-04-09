package com.capgemini.homeloanmanagementapp.controller;

import com.capgemini.homeloanmanagementapp.dto.ApplyLoanRequest;
import com.capgemini.homeloanmanagementapp.dto.EmiCalcRequest;
import com.capgemini.homeloanmanagementapp.dto.EmiCalcResponse;
import com.capgemini.homeloanmanagementapp.dto.LoanDetailResponse;
import com.capgemini.homeloanmanagementapp.dto.LoanSummaryResponse;
import com.capgemini.homeloanmanagementapp.model.LoanStatus;
import com.capgemini.homeloanmanagementapp.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanController with 100% line/branch coverage.
 * These tests call the controller methods directly (no MockMvc) and mock the LoanService.
 */
@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    private LoanController controller;

    @BeforeEach
    void setUp() {
        controller = new LoanController(loanService);
    }

    // ----- helpers -----

    private Authentication auth(String username, String... roles) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "N/A",
                java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList()
        );
    }

    // ----- tests -----

    @Test
    void calculateEmi_success_callsService_andReturnsResponse() {
        // Arrange
        EmiCalcRequest req = mock(EmiCalcRequest.class);
        when(req.getAmount()).thenReturn(new BigDecimal("100000"));
        when(req.getAnnualRatePercent()).thenReturn(new BigDecimal("8.5"));
        when(req.getTenureMonths()).thenReturn(240);
        EmiCalcResponse expected = mock(EmiCalcResponse.class);
        when(expected.getMonthlyEmi()).thenReturn(new BigDecimal("8678.23"));
        when(expected.getTotalPayable()).thenReturn(new BigDecimal("2082775.20"));
        when(expected.getTotalInterest()).thenReturn(new BigDecimal("1082775.20"));
        when(loanService.calculateEmi(req)).thenReturn(expected);

        // Act
        EmiCalcResponse actual = controller.calculateEmi(req);

        // Assert
        assertSame(expected, actual);
        verify(loanService, times(1)).calculateEmi(req);
        // also verify we accessed the request getters for logs (ensures those lines run)
        verify(req, atLeastOnce()).getAmount();
        verify(req, atLeastOnce()).getAnnualRatePercent();
        verify(req, atLeastOnce()).getTenureMonths();
    }

    @Test
    void list_whenAuthIsNull_returnsListForUnknownUser_nonAdmin() {
        // Arrange
        List<LoanSummaryResponse> list = emptyList();
        when(loanService.listLoansForUserOrAdmin("UNKNOWN", false)).thenReturn(list);

        // Act
        List<LoanSummaryResponse> result = controller.list(null);

        // Assert
        assertSame(list, result);
        verify(loanService).listLoansForUserOrAdmin("UNKNOWN", false);
    }

    @Test
    void list_whenAdmin_returnsAdminList() {
        // Arrange
        Authentication admin = auth("adminUser", "ROLE_ADMIN");
        List<LoanSummaryResponse> list = List.of(mock(LoanSummaryResponse.class));
        when(loanService.listLoansForUserOrAdmin("adminUser", true)).thenReturn(list);

        // Act
        List<LoanSummaryResponse> result = controller.list(admin);

        // Assert
        assertSame(list, result);
        verify(loanService).listLoansForUserOrAdmin("adminUser", true);
    }

    @Test
    void detail_whenUser_nonAdmin() {
        // Arrange
        Authentication user = auth("alice", "ROLE_USER");
        LoanDetailResponse detail = mock(LoanDetailResponse.class);
        when(detail.getAccountNumber()).thenReturn("ACC123");
        when(detail.getStatus()).thenReturn("ACTIVE");
        when(loanService.getLoanDetailByAccount("alice", false, "ACC123")).thenReturn(detail);

        // Act
        LoanDetailResponse result = controller.detail("ACC123", user);

        // Assert
        assertSame(detail, result);
        verify(loanService).getLoanDetailByAccount("alice", false, "ACC123");
    }

    @Test
    void activate_whenAdmin_callsService() {
        // Arrange
        Authentication admin = auth("sachin", "ROLE_ADMIN");

        // Act
        controller.activate("ACC999", admin);

        // Assert
        verify(loanService, times(1)).activateLoanAsAdmin("ACC999");
    }

    @Test
    void activate_whenNotAdmin_throwsForbidden() {
        // Arrange
        Authentication user = auth("bob", "ROLE_USER");

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.activate("ACC777", user));
        assertEquals("Forbidden", ex.getMessage());
        verify(loanService, never()).activateLoanAsAdmin(anyString());
    }

    @Test
    void apply_whenUser_findsCreatedLoanInList_andReturnsSummary() {
        Authentication user = auth("alice", "ROLE_USER");

        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        when(req.getProductId()).thenReturn(101L); // FIX: return Long, not String
        when(req.getAmount()).thenReturn(new BigDecimal("500000"));
        when(req.getTenureMonths()).thenReturn(240);

        // Replace the type below with your actual applyLoan return type if different
        com.capgemini.homeloanmanagementapp.model.Loan created =
                mock(com.capgemini.homeloanmanagementapp.model.Loan.class);
        when(created.getAccountNumber()).thenReturn("ACC123");
        // Do not stub getStatus() to avoid enum/string mismatch issues
        when(loanService.applyLoan(eq("alice"), same(req))).thenReturn(created);

        LoanSummaryResponse s1 = mock(LoanSummaryResponse.class);
        when(s1.getAccountNumber()).thenReturn("ACC000");

        LoanSummaryResponse s2 = mock(LoanSummaryResponse.class);
        when(s2.getAccountNumber()).thenReturn("ACC123");
        when(s2.getStatus()).thenReturn("PENDING");

        when(loanService.listLoansForUserOrAdmin("alice", false)).thenReturn(List.of(s1, s2));

        LoanSummaryResponse result = controller.apply(req, user);

        assertSame(s2, result);
        verify(loanService).applyLoan("alice", req);
        verify(loanService).listLoansForUserOrAdmin("alice", false);
        verify(created, atLeastOnce()).getAccountNumber();
    }


    @Test
    void apply_whenAdmin_usesAdminListFlag() {
        // Arrange
        Authentication admin = auth("admin1", "ROLE_ADMIN");

        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        // FIX: productId should be a Long (numeric), not a String
        when(req.getProductId()).thenReturn(202L);
        when(req.getAmount()).thenReturn(new BigDecimal("750000"));
        when(req.getTenureMonths()).thenReturn(180);

        // Replace the type below with the actual return type of LoanService.applyLoan(...) if different
        com.capgemini.homeloanmanagementapp.model.Loan created =
                mock(com.capgemini.homeloanmanagementapp.model.Loan.class);
        when(created.getAccountNumber()).thenReturn("ACC888");
        // Do NOT stub getStatus(); controller only logs it and your model may return an enum
        when(loanService.applyLoan(eq("admin1"), same(req))).thenReturn(created);

        LoanSummaryResponse adminVisible = mock(LoanSummaryResponse.class);
        when(adminVisible.getAccountNumber()).thenReturn("ACC888");
        when(adminVisible.getStatus()).thenReturn("PENDING");

        when(loanService.listLoansForUserOrAdmin("admin1", true)).thenReturn(List.of(adminVisible));

        // Act
        LoanSummaryResponse result = controller.apply(req, admin);

        // Assert
        assertSame(adminVisible, result);
        verify(loanService).applyLoan("admin1", req);
        verify(loanService).listLoansForUserOrAdmin("admin1", true);
    }

    @Test
    void apply_whenSummaryMissing_throwsNoSuchElementException() {
        // Arrange
        Authentication user = auth("alice", "ROLE_USER");

        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        // FIX: productId must be a Long, not a String
        when(req.getProductId()).thenReturn(101L);
        when(req.getAmount()).thenReturn(new BigDecimal("300000"));
        when(req.getTenureMonths()).thenReturn(120);

        // --- IMPORTANT ---
        // Replace with the ACTUAL type returned by LoanService.applyLoan(...) if different
        com.capgemini.homeloanmanagementapp.model.Loan created =
                mock(com.capgemini.homeloanmanagementapp.model.Loan.class);
        when(created.getAccountNumber()).thenReturn("ACC404");
        // Do NOT stub getStatus(); the controller only logs it
        when(loanService.applyLoan(eq("alice"), same(req))).thenReturn(created);

        // list does NOT contain ACC404 -> triggers orElseThrow()
        when(loanService.listLoansForUserOrAdmin("alice", false)).thenReturn(List.of());

        // Act + Assert
        assertThrows(java.util.NoSuchElementException.class, () -> controller.apply(req, user));
    }

    @Test
    void apply_whenAuthIsNull_usesUNKNOWN_andNonAdminPath() {
        LoanService loanService = mock(LoanService.class);
        LoanController controller = new LoanController(loanService);

        ApplyLoanRequest req = mock(ApplyLoanRequest.class);
        when(req.getProductId()).thenReturn(101L);
        when(req.getAmount()).thenReturn(new BigDecimal("300000"));
        when(req.getTenureMonths()).thenReturn(120);

        // service.applyLoan("UNKNOWN", req) returns created loan
        com.capgemini.homeloanmanagementapp.model.Loan created =
                mock(com.capgemini.homeloanmanagementapp.model.Loan.class);
        when(created.getAccountNumber()).thenReturn("ACC-NULL");
        when(created.getStatus()).thenReturn(LoanStatus.PENDING_APPROVAL);
        when(loanService.applyLoan(eq("UNKNOWN"), same(req))).thenReturn(created);

        // listLoansForUserOrAdmin("UNKNOWN", false) must contain created account
        LoanSummaryResponse visible = mock(LoanSummaryResponse.class);
        when(visible.getAccountNumber()).thenReturn("ACC-NULL");
        when(visible.getStatus()).thenReturn("PENDING_APPROVAL");
        when(loanService.listLoansForUserOrAdmin("UNKNOWN", false)).thenReturn(List.of(visible));

        var result = controller.apply(req, null);

        assertSame(visible, result);
        verify(loanService).applyLoan("UNKNOWN", req);
        verify(loanService).listLoansForUserOrAdmin("UNKNOWN", false);
    }

    // 2) detail(...) with admin Authentication → isAdmin=true branch
    @Test
    void detail_whenAdmin_trueBranch() {
        LoanService loanService = mock(LoanService.class);
        LoanController controller = new LoanController(loanService);

        var adminAuth = new UsernamePasswordAuthenticationToken(
                "adminUser", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        LoanDetailResponse detail = LoanDetailResponse.builder()
                .accountNumber("ACC-ADMIN")
                .status("ACTIVE")
                .build();

        when(loanService.getLoanDetailByAccount("adminUser", true, "ACC-ADMIN"))
                .thenReturn(detail);

        var res = controller.detail("ACC-ADMIN", adminAuth);

        assertSame(detail, res);
        verify(loanService).getLoanDetailByAccount("adminUser", true, "ACC-ADMIN");
    }

    // 3) activate(...) with null Authentication → forbidden branch (not admin)
    @Test
    void activate_whenAuthIsNull_forbiddenBranch() {
        LoanService loanService = mock(LoanService.class);
        LoanController controller = new LoanController(loanService);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.activate("ACC-FORBID", null));

        assertEquals("Forbidden", ex.getMessage());
        verify(loanService, never()).activateLoanAsAdmin(anyString());
    }

}