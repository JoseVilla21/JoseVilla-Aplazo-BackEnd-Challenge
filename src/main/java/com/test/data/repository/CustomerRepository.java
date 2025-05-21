package com.test.data.repository;

import com.test.data.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByFirstNameAndLastNameAndSecondLastNameAndDateOfBirth(
            String firstName, String lastName, String secondLastName, LocalDate dateOfBirth
    );
}