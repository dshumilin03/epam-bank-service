package com.epam.bank.repositories;

import com.epam.bank.entities.AbstractCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbstractCardRepository extends JpaRepository<AbstractCard, UUID> {
    Optional<AbstractCard> findByCardNumber(String cardNumber);

    List<AbstractCard> findByOwnerName(String ownerName);

    UUID id(UUID id);
}
