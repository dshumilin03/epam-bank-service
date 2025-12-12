package com.epam.bank.entities;

import com.epam.bank.services.Chargeable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_card")
@PrimaryKeyJoinColumn(name = "card_id")
@Getter
@Setter
public class CreditCard extends AbstractCard implements Chargeable {

    @Column(name = "interest_free_period_days")
    private Integer interestFreePeriodDays;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "next_charge_at")
    private LocalDateTime nextChargeAt;

    @Column(name = "last_charge_at")
    private LocalDateTime lastChargeAt;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "charge_strategy", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeStrategyType chargeStrategyType;

    @Override
    public BigDecimal getDebt() {
        return creditLimit.subtract(getBankAccount().getMoneyAmount());
    }
}