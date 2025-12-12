package com.epam.bank.repositories;

import com.epam.bank.entities.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    @Query("SELECT l FROM Loan l " +
            "JOIN l.bankAccount ba " +
            "JOIN ba.user u " +
            "WHERE u.id = :userId")
    List<Loan> findByUserId(@Param("userId") UUID userId);
}
