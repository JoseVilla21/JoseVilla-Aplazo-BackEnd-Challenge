package com.test.service;

import com.test.data.model.Customer;
import com.test.data.model.Loan;
import com.test.data.repository.CustomerRepository;
import com.test.data.repository.InstallmentRepository;
import com.test.data.repository.LoanRepository;
import com.test.enums.PaymentScheme;
import com.test.exception.InvalidLoanRequestException;
import com.test.request.LoanRequest;
import com.test.response.LoanResponse;
import com.test.response.PurchaseCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assignPaymentScheme_shouldReturnScheme1_forNamesStartingWithC_L_H() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Carlos");
        customer.setNoId(10L);

        PaymentScheme scheme = loanService.assignPaymentScheme(customer);
        assertEquals(PaymentScheme.SCHEME_1, scheme);

        customer.setFirstName("Luis");
        scheme = loanService.assignPaymentScheme(customer);
        assertEquals(PaymentScheme.SCHEME_1, scheme);

        customer.setFirstName("Hector");
        scheme = loanService.assignPaymentScheme(customer);
        assertEquals(PaymentScheme.SCHEME_1, scheme);
    }

    @Test
    void assignPaymentScheme_shouldReturnScheme2_forOtherNames() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Andres");
        customer.setNoId(30L);

        PaymentScheme scheme = loanService.assignPaymentScheme(customer);
        assertEquals(PaymentScheme.SCHEME_2, scheme);

        customer.setNoId(20L);
        scheme = loanService.assignPaymentScheme(customer);
        assertEquals(PaymentScheme.SCHEME_2, scheme);
    }

    @Test
    void calculatePurchase_shouldCalculateCorrectly() {
        BigDecimal purchaseAmount = new BigDecimal("1000");
        PaymentScheme scheme = PaymentScheme.SCHEME_1; // Supón que tiene 3 pagos, 10% interés, frecuencia 1 mes

        PurchaseCalculationResult result = loanService.calculatePurchase(purchaseAmount, scheme);

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getPurchaseDate());
        assertNotNull(result.getDueDates());
        assertEquals(scheme.getNumberOfPayments(), result.getDueDates().size());

        BigDecimal expectedCommission = purchaseAmount.multiply(BigDecimal.valueOf(scheme.getInterestRate())).setScale(2, BigDecimal.ROUND_HALF_UP);
        assertEquals(expectedCommission, result.getCommissionAmount());

        BigDecimal expectedTotal = purchaseAmount.add(expectedCommission);
        assertEquals(expectedTotal, result.getTotalPurchaseAmount());

        BigDecimal expectedInstallment = expectedTotal.divide(BigDecimal.valueOf(scheme.getNumberOfPayments()), 2, BigDecimal.ROUND_HALF_UP);
        assertEquals(expectedInstallment, result.getInstallmentAmount());
    }

    @Test
    void createLoan_shouldThrowException_whenAmountIsZeroOrNegative() {
        LoanRequest request = new LoanRequest();
        request.setCustomerId(UUID.randomUUID());
        request.setAmount(BigDecimal.ZERO);

        assertThrows(InvalidLoanRequestException.class, () -> loanService.createLoan(request));

        request.setAmount(new BigDecimal("-10"));
        assertThrows(InvalidLoanRequestException.class, () -> loanService.createLoan(request));
    }

    @Test
    void createLoan_shouldThrowException_whenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        LoanRequest request = new LoanRequest();
        request.setCustomerId(customerId);
        request.setAmount(new BigDecimal("1000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(InvalidLoanRequestException.class, () -> loanService.createLoan(request));
    }

    @Test
    void createLoan_shouldThrowException_whenAmountExceedsAvailableCredit() {
        UUID customerId = UUID.randomUUID();
        LoanRequest request = new LoanRequest();
        request.setCustomerId(customerId);
        request.setAmount(new BigDecimal("1500"));

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setAvailableCreditAmount(new BigDecimal("1000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThrows(InvalidLoanRequestException.class, () -> loanService.createLoan(request));
    }

    @Test
    void getLoanById_shouldReturnLoanResponse_whenFound() {
        UUID loanId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Loan loan = new Loan();
        loan.setId(loanId);
        Customer customer = new Customer();
        customer.setId(customerId);
        loan.setCustomer(customer);
        loan.setAmount(new BigDecimal("1000"));
        loan.setCreatedAt(LocalDateTime.now());
        loan.setCommissionAmount(new BigDecimal("100"));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(installmentRepository.findByLoanId(loanId)).thenReturn(Collections.emptyList());

        LoanResponse response = loanService.getLoanById(loanId);

        assertNotNull(response);
        assertEquals(loanId, response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(loan.getAmount(), response.getAmount());
    }

    @Test
    void getLoanById_shouldThrowException_whenNotFound() {
        UUID loanId = UUID.randomUUID();

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> loanService.getLoanById(loanId)); // Cambia si usas LoanNotFoundException específico
    }

}
