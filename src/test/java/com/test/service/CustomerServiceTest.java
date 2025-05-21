package com.test.service;

import com.test.data.model.Customer;
import com.test.data.repository.CustomerRepository;
import com.test.exception.CustomerNotFoundException;
import com.test.request.CustomerRequest;
import com.test.response.CustomerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = CustomerRequest.builder()
                .firstName("Juan")
                .lastName("Perez")
                .secondLastNme("Lopez")
                .dateOfBirth("1995-05-20")
                .build();
    }

    @Test
    void createCustomer_shouldCreateAndReturnCustomerResponse() {
        when(customerRepository.findByFirstNameAndLastNameAndSecondLastNameAndDateOfBirth(
                anyString(), anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

        Customer savedCustomer = new Customer();
        savedCustomer.setId(UUID.randomUUID());
        savedCustomer.setFirstName("JUAN");
        savedCustomer.setLastName("PEREZ");
        savedCustomer.setSecondLastName("LOPEZ");
        savedCustomer.setDateOfBirth(LocalDate.parse(validRequest.getDateOfBirth()));
        savedCustomer.setAssignedCreditAmount(BigDecimal.valueOf(5000));
        savedCustomer.setAvailableCreditAmount(BigDecimal.valueOf(5000));
        savedCustomer.setRegisterDate(LocalDateTime.now());

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.createCustomer(validRequest);

        verify(customerRepository).save(captor.capture());
        Customer capturedCustomer = captor.getValue();

        assertEquals("JUAN", capturedCustomer.getFirstName());
        assertEquals("PEREZ", capturedCustomer.getLastName());
        assertEquals("LOPEZ", capturedCustomer.getSecondLastName());
        assertEquals(LocalDate.parse(validRequest.getDateOfBirth()), capturedCustomer.getDateOfBirth());

        assertNotNull(response);
        assertEquals(savedCustomer.getId(), response.getIdClient());
        assertEquals(savedCustomer.getAssignedCreditAmount(), response.getCreditAmount());
        assertEquals(savedCustomer.getAvailableCreditAmount(), response.getAvailableCreditAmount());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void createCustomer_shouldThrowExceptionIfCustomerExists() {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(UUID.randomUUID());
        existingCustomer.setFirstName("JUAN");

        when(customerRepository.findByFirstNameAndLastNameAndSecondLastNameAndDateOfBirth(
                anyString(), anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(Optional.of(existingCustomer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(validRequest));

        assertEquals("El cliente ya existe con el mismo nombre y fecha de nacimiento.", exception.getMessage());
    }

    @Test
    void assignCreditLine_shouldReturnCorrectCreditBasedOnAge() {
        LocalDate under18 = LocalDate.now().minusYears(18).plusDays(1); // menos de 18 años
        LocalDate exactly18 = LocalDate.now().minusYears(18); // justo 18 años
        LocalDate dob24 = LocalDate.now().minusYears(24);
        LocalDate dob28 = LocalDate.now().minusYears(28);
        LocalDate dob40 = LocalDate.now().minusYears(40);
        LocalDate over65 = LocalDate.now().minusYears(66);

        // Edad menor que 18 lanza excepción
        assertThrows(IllegalArgumentException.class, () -> customerService.assignCreditLine(under18));

        // Edad igual a 18 NO lanza excepción
        assertDoesNotThrow(() -> customerService.assignCreditLine(exactly18));

        // Edad mayor que 65 lanza excepción
        assertThrows(IllegalArgumentException.class, () -> customerService.assignCreditLine(over65));

        // Pruebas normales
        assertEquals(BigDecimal.valueOf(3000), customerService.assignCreditLine(dob24));
        assertEquals(BigDecimal.valueOf(5000), customerService.assignCreditLine(dob28));
        assertEquals(BigDecimal.valueOf(8000), customerService.assignCreditLine(dob40));
    }

    @Test
    void getAllCustomers_shouldReturnListOfCustomerResponses() {
        Customer c1 = new Customer();
        c1.setId(UUID.randomUUID());
        c1.setAssignedCreditAmount(BigDecimal.valueOf(5000));
        c1.setAvailableCreditAmount(BigDecimal.valueOf(3000));
        c1.setRegisterDate(LocalDateTime.now());

        Customer c2 = new Customer();
        c2.setId(UUID.randomUUID());
        c2.setAssignedCreditAmount(BigDecimal.valueOf(8000));
        c2.setAvailableCreditAmount(BigDecimal.valueOf(7000));
        c2.setRegisterDate(LocalDateTime.now());

        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CustomerResponse> responses = customerService.getAllCustomers();

        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getIdClient().equals(c1.getId())));
        assertTrue(responses.stream().anyMatch(r -> r.getIdClient().equals(c2.getId())));
    }

    @Test
    void findCustomer_shouldReturnCustomerResponse_whenCustomerExists() {
        UUID id = UUID.randomUUID();
        Customer c = new Customer();
        c.setId(id);
        c.setAssignedCreditAmount(BigDecimal.valueOf(5000));
        c.setAvailableCreditAmount(BigDecimal.valueOf(4000));
        c.setRegisterDate(LocalDateTime.now());

        when(customerRepository.findById(id)).thenReturn(Optional.of(c));

        CustomerResponse response = customerService.findCustomer(id);

        assertEquals(id, response.getIdClient());
        assertEquals(c.getAssignedCreditAmount(), response.getCreditAmount());
        assertEquals(c.getAvailableCreditAmount(), response.getAvailableCreditAmount());
        assertEquals(c.getRegisterDate().toString(), response.getCreatedAt());
    }

    @Test
    void findCustomer_shouldThrowException_whenCustomerDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.findCustomer(id));
    }
}
