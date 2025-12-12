package com.epam.bank.repositories;

import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByCard(AbstractCard card);
}
