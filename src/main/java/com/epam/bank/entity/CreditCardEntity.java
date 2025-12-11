package com.epam.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_card")
@PrimaryKeyJoinColumn(name = "card_id")
@Getter
@Setter
public class CreditCardEntity extends AbstractCardEntity {

    @Column(name = "interest_free_period_days")
    private Integer interestFreePeriodDays;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "next_charge_date")
    private LocalDateTime nextChargeDate;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;
}