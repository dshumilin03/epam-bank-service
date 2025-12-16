package com.epam.bank.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "card")
@Entity
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "pin_code")
    private String pinCode;

    @Column(name = "cvv_code")
    private String cvv;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "card_status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_number", referencedColumnName = "bank_account_number", unique = true)
    private BankAccount bankAccount;

}