package com.epam.bank.repository;

import com.epam.bank.entities.AbstractCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AbstractCardRepository extends JpaRepository<AbstractCard, UUID> {
}
