package com.epam.bank.repositories;

import com.epam.bank.entities.AbstractCard;
import com.epam.bank.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByCard(AbstractCard card);

    Optional<BankAccount> findByCardId(UUID cardId);
}
