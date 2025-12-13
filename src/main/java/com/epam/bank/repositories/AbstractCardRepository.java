package com.epam.bank.repositories;

import com.epam.bank.entities.AbstractCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbstractCardRepository extends JpaRepository<AbstractCard, UUID> {
    Optional<AbstractCard> findByCardNumber(String cardNumber);

    List<AbstractCard> findByOwnerName(String ownerName);

    @Query("SELECT c FROM AbstractCard c " +
            "JOIN FETCH c.bankAccount ba " +
            "JOIN FETCH ba.user u " +
            "WHERE u.id = :userId")
    List<AbstractCard> findByUserId(UUID id);

    UUID id(UUID id);
}
