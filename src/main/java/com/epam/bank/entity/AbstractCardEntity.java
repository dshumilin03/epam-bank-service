package com.epam.bank.entity;

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
public abstract class AbstractCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "pin_code")
    private String pinCode;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "card_status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_number", referencedColumnName = "bank_account_number", unique = true)
    private BankAccountEntity bankAccount;

}