package com.epam.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "bank_account")
@Getter
@Setter
public class BankAccountEntity {

    @Column(name = "bank_account_number")
    @Id
    private Long bankAccountNumber;

    @Column(name = "money_amount")
    private BigDecimal moneyAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @OneToOne(mappedBy = "bankAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AbstractCardEntity card;

    @OneToMany(mappedBy = "source",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionEntity> outgoingTransactions;

    @OneToMany(mappedBy = "target",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionEntity> incomingTransactions;
}
