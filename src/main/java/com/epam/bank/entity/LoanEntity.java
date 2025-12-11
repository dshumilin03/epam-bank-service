package com.epam.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "loan")
@Getter
@Setter
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "money_left", nullable = false)
    private BigDecimal moneyLeft;

    @Column(name = "percent", nullable = false)
    private Double percent;

    @Column(name = "charge_strategy", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeStrategyType chargeStrategy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_number", referencedColumnName = "bank_account_number", nullable = false)
    private BankAccountEntity bankAccount;
}