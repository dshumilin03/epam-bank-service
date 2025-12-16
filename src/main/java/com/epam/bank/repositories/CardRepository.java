package com.epam.bank.repositories;

import com.epam.bank.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByOwnerName(String ownerName);

    @Query("SELECT c FROM Card c " +
            "JOIN FETCH c.bankAccount ba " +
            "JOIN FETCH ba.user u " +
            "WHERE u.id = :userId")
    List<Card> findByUserId(@Param("userId") UUID id);

    UUID id(UUID id);
}
