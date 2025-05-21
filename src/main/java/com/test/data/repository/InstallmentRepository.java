package com.test.data.repository;

import com.test.data.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    List<Installment> findByLoanId(UUID loanId);
}
