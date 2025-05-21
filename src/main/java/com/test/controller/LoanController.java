package com.test.controller;

import com.test.exception.InvalidLoanRequestException;
import com.test.request.LoanRequest;
import com.test.response.LoanResponse;
import com.test.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request,
                                                   HttpServletRequest httpRequest) {
        LoanResponse loan = loanService.createLoan(request);
        URI location = URI.create(httpRequest.getRequestURI() + "/" + loan.getId());
        return ResponseEntity.created(location).body(loan);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable UUID loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }
}
