package com.test.controller;

import com.test.request.CustomerRequest;
import com.test.response.CustomerResponse;
import com.test.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        try {
            log.info("Create customer start");
            return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Duplicated data");
        } finally {
            log.info("Create customer end");
        }
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping (value = "/{customerId}")
    public ResponseEntity<CustomerResponse> getByCustomerId(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.findCustomer(customerId));
    }
}
