package com.epam.bank.entities;

import com.epam.bank.services.Chargeable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan")
@Getter
@Setter
public class Loan implements Chargeable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "money_left", nullable = false)
    private BigDecimal moneyLeft;

    @Column(name = "percent", nullable = false)
    private Double percent;

    @Column(name = "term_months", nullable = false)
    private Long termMonths;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "next_charge_at", nullable = false)
    private LocalDateTime nextChargeAt;

    @Column(name = "last_charge_at", nullable = false)
    private LocalDateTime lastChargeAt;

    @Column(name = "charge_strategy", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeStrategyType chargeStrategyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_number", referencedColumnName = "bank_account_number", nullable = false)
    private BankAccount bankAccount;

    @Override
    public BigDecimal getDebt() {
        return bankAccount.getMoneyAmount();
    }
}