package com.test.service;

import com.test.data.model.Customer;
import com.test.data.repository.CustomerRepository;
import com.test.exception.CustomerNotFoundException;
import com.test.request.CustomerRequest;
import com.test.response.CustomerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("createCustomer start - FirstName: {}, LastName: {}, DOB: {}",
                request.getFirstName(), request.getLastName(), request.getDateOfBirth());

        LocalDate dob = request.getDateOfBirth() != null ? LocalDate.parse(request.getDateOfBirth()) : null;

        Optional<Customer> existingCustomer = customerRepository
                .findByFirstNameAndLastNameAndSecondLastNameAndDateOfBirth(
                        request.getFirstName(), request.getLastName(), request.getSecondLastNme(), dob
                );

        if (existingCustomer.isPresent()) {
            log.warn("Customer already exists with name {} {} {} and DOB {}",
                    request.getFirstName(), request.getLastName(), request.getSecondLastNme(), dob);
            throw new IllegalArgumentException("El cliente ya existe con el mismo nombre y fecha de nacimiento.");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName() != null ? request.getFirstName().toUpperCase() : null);
        customer.setLastName(request.getLastName() != null ? request.getLastName().toUpperCase() : null);
        customer.setDateOfBirth(dob);
        customer.setSecondLastName(request.getSecondLastNme() != null ? request.getSecondLastNme().toUpperCase() : null);

        BigDecimal assignedCredit = assignCreditLine(customer.getDateOfBirth());
        customer.setAssignedCreditAmount(assignedCredit);
        customer.setAvailableCreditAmount(assignedCredit);
        customer.setRegisterDate(LocalDateTime.now());

        Customer saved = customerRepository.save(customer);
        log.info("Customer saved - Id: {}, AssignedCredit: {}", saved.getId(), assignedCredit);

        CustomerResponse response = new CustomerResponse();
        response.setCreditAmount(saved.getAssignedCreditAmount());
        response.setIdClient(saved.getId());
        response.setAvailableCreditAmount(saved.getAvailableCreditAmount());
        response.setCreatedAt(saved.getRegisterDate().toString());

        log.info("createCustomer finished - IdClient: {}", saved.getId());
        return response;
    }

    public BigDecimal assignCreditLine(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        log.info("Assigning credit line - BirthDate: {}, Age: {}", birthDate, age);

        if (age < 18 || age > 65) {
            log.warn("Client age {} not accepted", age);
            throw new IllegalArgumentException("Client not accepted");
        } else if (age <= 25) {
            return BigDecimal.valueOf(3000);
        } else if (age <= 30) {
            return BigDecimal.valueOf(5000);
        } else {
            return BigDecimal.valueOf(8000);
        }
    }

    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");
        List<CustomerResponse> customers = customerRepository.findAll()
                .stream()
                .map(customer -> {
                    CustomerResponse dto = new CustomerResponse();
                    dto.setIdClient(customer.getId());
                    dto.setCreditAmount(customer.getAssignedCreditAmount());
                    dto.setAvailableCreditAmount(customer.getAvailableCreditAmount());
                    dto.setCreatedAt(customer.getRegisterDate().toString());
                    return dto;
                }).collect(Collectors.toList());
        log.info("Total customers fetched: {}", customers.size());
        return customers;
    }

    public CustomerResponse findCustomer(UUID customerId) {
        log.info("findCustomer start - CustomerId: {}", customerId);
        Optional<Customer> customer = customerRepository.findById(customerId);

        if (customer.isPresent()) {
            Customer c = customer.get();
            CustomerResponse customerResponse = new CustomerResponse();
            customerResponse.setCreditAmount(c.getAssignedCreditAmount());
            customerResponse.setIdClient(c.getId());
            customerResponse.setCreatedAt(c.getRegisterDate().toString());
            customerResponse.setAvailableCreditAmount(c.getAvailableCreditAmount());

            log.info("Customer found - CustomerId: {}", c.getId());
            return customerResponse;
        } else {
            log.warn("Customer not found - CustomerId: {}", customerId);
            throw new CustomerNotFoundException(customerId);
        }
    }

}
