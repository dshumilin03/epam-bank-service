package com.epam.bank.repositories;

import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySourceBankAccountNumber(Long sourceBankAccountNumber);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.transactionType = :type AND t.status = :status AND " +
            "(t.source.user.id = :userId OR t.target.user.id = :userId)")
    List<Transaction> findAllByUserIdAndTypeAndStatus(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status
    );
}
