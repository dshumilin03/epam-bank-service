package com.epam.bank.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "abstract_card")
@Entity
@Getter
@Setter
public abstract class AbstractCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "pin_code")
    private Integer pinCode;

    @Column(name = "cvv_code")
    private Integer cvv;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "card_status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(name = "card_type")
    @Enumerated(EnumType.STRING)
    private CardType type;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_number", referencedColumnName = "bank_account_number", unique = true)
    private BankAccount bankAccount;

}