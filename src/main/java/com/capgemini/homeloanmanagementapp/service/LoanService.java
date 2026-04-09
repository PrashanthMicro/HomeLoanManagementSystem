package com.capgemini.homeloanmanagementapp.service;

import com.capgemini.homeloanmanagementapp.dto.*;
import com.capgemini.homeloanmanagementapp.exception.ForbiddenException;
import com.capgemini.homeloanmanagementapp.exception.NotFoundException;
import com.capgemini.homeloanmanagementapp.model.*;
import com.capgemini.homeloanmanagementapp.repository.LoanRepository;
import com.capgemini.homeloanmanagementapp.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- added
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // <-- added
@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepo;
    private final LoanProductService productService;
    private final UserService userService;

    public EmiCalcResponse calculateEmi(EmiCalcRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("EMI calc: amount={}, annualRatePercent={}, tenureMonths={}",
                req.getAmount(), req.getAnnualRatePercent(), req.getTenureMonths());

        BigDecimal emi = EmiCalculator.monthlyEmi(req.getAmount(), req.getAnnualRatePercent(), req.getTenureMonths());
        BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(req.getTenureMonths()));
        BigDecimal totalInterest = totalPayable.subtract(req.getAmount());

        EmiCalcResponse resp = EmiCalcResponse.builder()
                .monthlyEmi(emi)
                .totalPayable(totalPayable)
                .totalInterest(totalInterest)
                .build();

        log.info("EMI calc done: monthlyEmi={}, totalPayable={}, totalInterest={} ({} ms)",
                resp.getMonthlyEmi(), resp.getTotalPayable(), resp.getTotalInterest(),
                (System.currentTimeMillis() - t0));
        return resp;
    }

    public Loan applyLoan(String username, ApplyLoanRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("Apply loan: user={}, productId={}, amount={}, tenureMonths={}",
                username, req.getProductId(), req.getAmount(), req.getTenureMonths());

        User user = userService.findByUsername(username);
        LoanProduct product = productService.getById(req.getProductId());

        if (req.getAmount().compareTo(product.getMinAmount()) < 0 ||
                req.getAmount().compareTo(product.getMaxAmount()) > 0) {
            log.warn("Apply loan validation failed: amount {} is out of bounds [{}, {}] for productId={}",
                    req.getAmount(), product.getMinAmount(), product.getMaxAmount(), product.getId());
            throw new ForbiddenException("Requested amount out of product bounds");
        }
        if (req.getTenureMonths() < product.getMinTenureMonths() ||
                req.getTenureMonths() > product.getMaxTenureMonths()) {
            log.warn("Apply loan validation failed: tenure {} is out of bounds [{}, {}] for productId={}",
                    req.getTenureMonths(), product.getMinTenureMonths(), product.getMaxTenureMonths(), product.getId());
            throw new ForbiddenException("Requested tenure out of product bounds");
        }

        BigDecimal emi = EmiCalculator.monthlyEmi(req.getAmount(), product.getAnnualInterestRate(), req.getTenureMonths());

        Loan loan = Loan.builder()
                .accountNumber(generateAccountNumber())
                .customer(user)
                .product(product)
                .principalAmount(req.getAmount())
                .tenureMonths(req.getTenureMonths())
                .annualInterestRate(product.getAnnualInterestRate())
                .monthlyEmiAmount(emi)
                .outstandingPrincipal(req.getAmount())
                .outstandingEmiCount(req.getTenureMonths())
                .nominee(Nominee.builder()
                        .name(req.getNomineeName())
                        .relation(req.getNomineeRelation())
                        .contactNumber(req.getNomineeContact())
                        .build())
                .status(LoanStatus.PENDING_APPROVAL)
                .createdAt(LocalDateTime.now())
                .build();

        Loan saved = loanRepo.save(loan);
        log.info("Apply loan success: user={}, accountNumber={}, status={}, productCode={} ({} ms)",
                username, saved.getAccountNumber(), saved.getStatus(),
                saved.getProduct() != null ? saved.getProduct().getCode() : "N/A",
                (System.currentTimeMillis() - t0));
        return saved;
    }

    private String generateAccountNumber() {
        // lightweight debug to trace account generation, no PII
        String acc = "HL-" + System.currentTimeMillis();
        log.debug("Generated loan accountNumber={}", acc);
        return acc;
    }

    public List<LoanSummaryResponse> listLoansForUserOrAdmin(String username, boolean isAdmin) {
        long t0 = System.currentTimeMillis();
        log.info("List loans: user={}, isAdmin={}", username, isAdmin);

        List<LoanSummaryResponse> list;
        if (isAdmin) {
            list = loanRepo.findAll().stream().map(this::toSummary).collect(Collectors.toList());
        } else {
            User user = userService.findByUsername(username);
            list = loanRepo.findByCustomer(user).stream().map(this::toSummary).collect(Collectors.toList());
        }

        log.info("List loans delivered: count={} ({} ms) for user={}, isAdmin={}",
                list.size(), (System.currentTimeMillis() - t0), username, isAdmin);
        return list;
    }

    public LoanDetailResponse getLoanDetailByAccount(String username, boolean isAdmin, String accountNumber) {
        long t0 = System.currentTimeMillis();
        log.info("Loan detail: user={}, isAdmin={}, accountNumber={}", username, isAdmin, accountNumber);

        Loan loan = loanRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Loan not found: accountNumber={}", accountNumber);
                    return new NotFoundException("Loan not found");
                });

        if (!isAdmin && !loan.getCustomer().getUsername().equals(username)) {
            log.warn("Loan detail forbidden: user={} is not owner of accountNumber={}", username, accountNumber);
            throw new ForbiddenException("You are not allowed to view this loan");
        }

        LoanDetailResponse resp = LoanDetailResponse.builder()
                .accountNumber(loan.getAccountNumber())
                .loanType(loan.getProduct().getName())
                .nomineeName(loan.getNominee() != null ? loan.getNominee().getName() : null)
                .nomineeRelation(loan.getNominee() != null ? loan.getNominee().getRelation() : null)
                .nomineeContact(loan.getNominee() != null ? loan.getNominee().getContactNumber() : null)
                .totalLoanAmount(loan.getPrincipalAmount())
                .loanTenureMonths(loan.getTenureMonths())
                .currentRateOfInterest(loan.getAnnualInterestRate())
                .principalOutstandingAmount(loan.getOutstandingPrincipal())
                .outstandingEmiCount(loan.getOutstandingEmiCount())
                .monthlyEmiAmount(loan.getMonthlyEmiAmount())
                .status(loan.getStatus().name())
                .createdAt(loan.getCreatedAt())
                .build();

        log.info("Loan detail served: accountNumber={}, status={} ({} ms)",
                accountNumber, resp.getStatus(), (System.currentTimeMillis() - t0));
        return resp;
    }

    private LoanSummaryResponse toSummary(Loan loan) {
        LoanSummaryResponse dto = LoanSummaryResponse.builder()
                .accountNumber(loan.getAccountNumber())
                .productCode(loan.getProduct().getCode())
                .productName(loan.getProduct().getName())
                .principalAmount(loan.getPrincipalAmount())
                .tenureMonths(loan.getTenureMonths())
                .annualInterestRate(loan.getAnnualInterestRate())
                .monthlyEmiAmount(loan.getMonthlyEmiAmount())
                .status(loan.getStatus().name())
                .createdAt(loan.getCreatedAt())
                .trackerMessage(buildTrackerMessage(loan))
                .build();
        log.debug("Mapped Loan -> LoanSummaryResponse: accountNumber={}, status={}", dto.getAccountNumber(), dto.getStatus());
        return dto;
    }

    private String buildTrackerMessage(Loan loan) {
        long days = ChronoUnit.DAYS.between(loan.getCreatedAt(), LocalDateTime.now());
        if (days < 14) {
            return "Home loan creation will be completed after required approvals";
        }
        return null;
    }

    public void activateLoanAsAdmin(String accountNumber) {
        long t0 = System.currentTimeMillis();
        log.info("Activate loan (admin): accountNumber={}", accountNumber);

        Loan loan = loanRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Activate failed: loan not found for accountNumber={}", accountNumber);
                    return new NotFoundException("Not found");
                });

        loan.setStatus(LoanStatus.ACTIVE);
        loanRepo.save(loan);

        log.info("Activate loan success: accountNumber={}, newStatus=ACTIVE ({} ms)",
                accountNumber, (System.currentTimeMillis() - t0));
    }
}