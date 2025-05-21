package com.test.service;

import com.test.data.model.Customer;
import com.test.data.model.Installment;
import com.test.data.model.Loan;
import com.test.data.repository.CustomerRepository;
import com.test.data.repository.InstallmentRepository;
import com.test.data.repository.LoanRepository;
import com.test.enums.InstallmentStatus;
import com.test.enums.LoanStatus;
import com.test.enums.PaymentScheme;
import com.test.exception.InvalidLoanRequestException;
import com.test.exception.LoanNotFoundException;
import com.test.request.LoanRequest;
import com.test.response.InstallmentResponse;
import com.test.response.LoanResponse;
import com.test.response.PaymentPlanResponse;
import com.test.response.PurchaseCalculationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public LoanResponse createLoan(LoanRequest request) {
        log.info("CreateLoan begin - CustomerId: {}, Amount: {}", request.getCustomerId(), request.getAmount());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid loan amount {} for CustomerId: {}", request.getAmount(), request.getCustomerId());
            throw new InvalidLoanRequestException("Amount must be greater than 0");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new InvalidLoanRequestException("Customer not found"));

        log.info("Customer fetched with Id: {}", customer.getId());

        if (customer.getAvailableCreditAmount().compareTo(request.getAmount()) < 0) {
            log.warn("Purchase amount {} exceeds available credit {}", request.getAmount(), customer.getAvailableCreditAmount());
            throw new InvalidLoanRequestException("Purchase amount exceeds the assigned credit line");
        }

        PaymentScheme scheme = assignPaymentScheme(customer);
        log.info("Payment scheme {} assigned to CustomerId: {}", scheme.name(), customer.getId());

        PurchaseCalculationResult calc = calculatePurchase(request.getAmount(), scheme);
        log.info("Purchase calculation done for CustomerId: {} - Commission: {}, Total: {}",
                customer.getId(), calc.getCommissionAmount(), calc.getTotalPurchaseAmount());

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setAmount(request.getAmount());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCreatedAt(LocalDateTime.now());
        loan.setCommissionAmount(calc.getCommissionAmount());
        loanRepository.save(loan);
        log.info("Loan saved with Id: {} for CustomerId: {}", loan.getId(), customer.getId());

        for (LocalDate dueDate : calc.getDueDates()) {
            Installment installment = new Installment();
            installment.setLoan(loan);
            installment.setAmount(calc.getInstallmentAmount());
            installment.setScheduledPaymentDate(dueDate);
            installment.setStatus(InstallmentStatus.PENDING);
            installmentRepository.save(installment);
            log.info("Installment created - LoanId: {}, DueDate: {}, Amount: {}",
                    loan.getId(), dueDate, calc.getInstallmentAmount());
        }

        BigDecimal newAvailableCredit = customer.getAvailableCreditAmount().subtract(request.getAmount());
        customer.setAvailableCreditAmount(newAvailableCredit);
        customerRepository.save(customer);
        log.info("Updated available credit for CustomerId: {} - New available credit: {}", customer.getId(), newAvailableCredit);

        log.info("CreateLoan finished - LoanId: {}", loan.getId());
        return toLoanResponse(loan);
    }

    public LoanResponse getLoanById(UUID loanId) {
        log.info("Fetching Loan by id: {}", loanId);

        Optional<Loan> loan = loanRepository.findById(loanId);
        if (loan.isPresent()) {
            log.info("Loan found with id: {}", loanId);
            return toLoanResponse(loan.get());
        } else {
            log.warn("Loan not found with id: {}", loanId);
            throw new LoanNotFoundException(loanId.toString());
        }
    }


    public PaymentScheme assignPaymentScheme(Customer customer) {
        log.info("CustomerId {} - Assign payment scheme start", customer.getId());

        String firstNameInitial = customer.getFirstName().substring(0, 1).toUpperCase();
        if ("C".equals(firstNameInitial) || "L".equals(firstNameInitial) || "H".equals(firstNameInitial)) {
            log.info("CustomerId {} - Scheme 1 assigned", customer.getId());
            return PaymentScheme.SCHEME_1;
        }

        if (customer.getNoId() > 25) {
            log.info("CustomerId {} - Scheme 2 assigned", customer.getId());
            return PaymentScheme.SCHEME_2;
        }

        log.info("CustomerId {} - Default Scheme 2 assigned", customer.getId());
        return PaymentScheme.SCHEME_2;
    }

    public PurchaseCalculationResult calculatePurchase(BigDecimal purchaseAmount, PaymentScheme scheme) {
        log.info("Starting purchase calculation. Purchase amount: {}, PaymentScheme: {}", purchaseAmount, scheme);

        PurchaseCalculationResult result = new PurchaseCalculationResult();

        LocalDate today = LocalDate.now();
        result.setPurchaseDate(today);
        log.info("Set purchase date: {}", today);

        BigDecimal interestRate = BigDecimal.valueOf(scheme.getInterestRate());
        BigDecimal commissionAmount = purchaseAmount.multiply(interestRate).setScale(2, RoundingMode.HALF_UP);
        result.setCommissionAmount(commissionAmount);
        log.info("Calculated commission amount: {} (purchaseAmount {} * interestRate {})", commissionAmount, purchaseAmount, interestRate);

        BigDecimal total = purchaseAmount.add(commissionAmount);
        result.setTotalPurchaseAmount(total);
        log.info("Calculated total purchase amount (purchaseAmount + commission): {}", total);

        BigDecimal installmentAmount = total.divide(BigDecimal.valueOf(scheme.getNumberOfPayments()), 2, RoundingMode.HALF_UP);
        result.setInstallmentAmount(installmentAmount);
        log.info("Calculated installment amount: {} (total {} / numberOfPayments {})", installmentAmount, total, scheme.getNumberOfPayments());

        List<LocalDate> dueDates = new ArrayList<>();
        LocalDate dueDate = today;
        for (int i = 1; i <= scheme.getNumberOfPayments(); i++) {
            dueDate = dueDate.plus(scheme.getFrequencyAmount(), scheme.getFrequencyUnit());
            dueDates.add(dueDate);
            log.info("Calculated due date {}: {}", i, dueDate);
        }
        result.setDueDates(dueDates);

        log.info("Purchase calculation completed successfully for purchase amount: {}", purchaseAmount);
        return result;
    }


    public LoanResponse toLoanResponse(Loan loan) {
        log.info("Mapping Loan (id: {}) to LoanResponse", loan.getId());

        List<Installment> installments = installmentRepository.findByLoanId(loan.getId());
        log.info("Found {} installments for Loan id: {}", installments.size(), loan.getId());

        List<InstallmentResponse> installmentResponses = installments.stream()
                .map(installment -> {
                    log.info("Mapping Installment scheduled for {} with amount {} and status {}",
                            installment.getScheduledPaymentDate(), installment.getAmount(), installment.getStatus());
                    return InstallmentResponse.builder()
                            .scheduledPaymentDate(installment.getScheduledPaymentDate())
                            .amount(installment.getAmount())
                            .status(InstallmentStatus.valueOf(installment.getStatus().name()))
                            .build();
                })
                .collect(Collectors.toList());

        PaymentPlanResponse paymentPlan = PaymentPlanResponse.builder()
                .commissionAmount(loan.getCommissionAmount())
                .installments(installmentResponses)
                .build();
        log.info("Created PaymentPlanResponse with commission amount {}", loan.getCommissionAmount());

        LoanResponse loanResponse = LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .amount(loan.getAmount())
                .createdAt(loan.getCreatedAt())
                .paymentPlan(paymentPlan)
                .build();

        log.info("LoanResponse created successfully for Loan id: {}", loan.getId());

        return loanResponse;
    }

}
