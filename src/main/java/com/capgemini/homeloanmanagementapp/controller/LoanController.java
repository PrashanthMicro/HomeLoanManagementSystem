package com.capgemini.homeloanmanagementapp.controller;

import com.capgemini.homeloanmanagementapp.dto.*;
import com.capgemini.homeloanmanagementapp.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;                               // <-- added
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j                                                          // <-- added
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/emi")
    public EmiCalcResponse calculateEmi(@Valid @RequestBody EmiCalcRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("EMI calculation requested: amount={}, annualRatePercent={}, tenureMonths={}",
                req.getAmount(), req.getAnnualRatePercent(), req.getTenureMonths());
        EmiCalcResponse res = loanService.calculateEmi(req);
        log.info("EMI calculation success: monthlyEmi={}, totalPayable={}, totalInterest={} ({} ms)",
                res.getMonthlyEmi(), res.getTotalPayable(), res.getTotalInterest(),
                (System.currentTimeMillis() - t0));
        return res;
    }

    @PostMapping("/apply")
    public LoanSummaryResponse apply(@Valid @RequestBody ApplyLoanRequest req, Authentication auth) {
        long t0 = System.currentTimeMillis();
        String username = auth != null ? auth.getName() : "UNKNOWN";
        log.info("Apply loan requested by user={} for productId={}, amount={}, tenureMonths={}",
                username, req.getProductId(), req.getAmount(), req.getTenureMonths());

        var loan = loanService.applyLoan(username, req);
        log.debug("Loan created: accountNumber={}, status={}", loan.getAccountNumber(), loan.getStatus());

        LoanSummaryResponse summary = loanService
                .listLoansForUserOrAdmin(username, isAdmin(auth)).stream()
                .filter(l -> l.getAccountNumber().equals(loan.getAccountNumber()))
                .findFirst()
                .orElseThrow();

        log.info("Apply loan completed for user={} accountNumber={} status={} ({} ms)",
                username, summary.getAccountNumber(), summary.getStatus(),
                (System.currentTimeMillis() - t0));
        return summary;
    }

    @GetMapping
    public List<LoanSummaryResponse> list(Authentication auth) {
        long t0 = System.currentTimeMillis();
        String username = auth != null ? auth.getName() : "UNKNOWN";
        boolean admin = isAdmin(auth);
        log.info("List loans requested by user={} (isAdmin={})", username, admin);

        List<LoanSummaryResponse> list = loanService.listLoansForUserOrAdmin(username, admin);
        log.info("List loans delivered: count={} for user={} ({} ms)",
                list.size(), username, (System.currentTimeMillis() - t0));
        return list;
    }

    @GetMapping("/{accountNumber}")
    public LoanDetailResponse detail(@PathVariable("accountNumber") String accountNumber, Authentication auth) {
        long t0 = System.currentTimeMillis();
        String username = auth != null ? auth.getName() : "UNKNOWN";
        boolean admin = isAdmin(auth);
        log.info("Loan detail requested: user={}, accountNumber={}, isAdmin={}", username, accountNumber, admin);

        LoanDetailResponse detail = loanService.getLoanDetailByAccount(username, admin, accountNumber);
        log.info("Loan detail served: accountNumber={}, status={} ({} ms)",
                detail.getAccountNumber(), detail.getStatus(), (System.currentTimeMillis() - t0));
        return detail;
    }

    @PostMapping("/admin/{accountNumber}/activate")
    public void activate(@PathVariable("accountNumber") String accountNumber, Authentication auth) {
        String username = auth != null ? auth.getName() : "UNKNOWN";
        if (!isAdmin(auth)) {
            log.warn("Forbidden activation attempt by user={} on accountNumber={}", username, accountNumber);
            throw new RuntimeException("Forbidden");
        }
        log.info("Admin activation requested by user={} for accountNumber={}", username, accountNumber);
        loanService.activateLoanAsAdmin(accountNumber);
        log.info("Admin activation completed for accountNumber={}", accountNumber);
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}