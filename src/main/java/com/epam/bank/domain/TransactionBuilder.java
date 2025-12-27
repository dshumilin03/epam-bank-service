package com.epam.bank.domain;

import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionBuilder {
    private UUID id;
    private LocalDateTime createdAt;
    private BigDecimal moneyAmount;
    private String description;
    private TransactionStatus status;
    private TransactionType transactionType;
    private BankAccount source;
    private BankAccount target;
    private final BankAccountProvider bankAccountProvider;

    public TransactionBuilder id(final UUID id) {
        this.id = id;
        return this;
    }

    public TransactionBuilder createdAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TransactionBuilder moneyAmount(final BigDecimal moneyAmount) {
        this.moneyAmount = moneyAmount;
        return this;
    }

    public TransactionBuilder description(final String description) {
        this.description = description;
        return this;
    }

    public TransactionBuilder status(final TransactionStatus status) {
        this.status = status;
        return this;
    }

    public TransactionBuilder transactionType(final TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public TransactionBuilder source(final BankAccount source) {
        this.source = source;
        return this;
    }

    public TransactionBuilder target(final BankAccount target) {
        this.target = target;
        return this;
    }

    public TransactionBuilder source(final Long sourceNumber) {
        this.source = bankAccountProvider.getOrThrow(sourceNumber);
        return this;
    }

    public TransactionBuilder target(final Long targetNumber) {
        this.target = bankAccountProvider.getOrThrow(targetNumber);
        return this;
    }

    public Transaction build() {
        return new Transaction(this.id, this.createdAt, this.moneyAmount, this.description, this.status, this.transactionType, this.source, this.target);
    }

    public String toString() {
        String var10000 = String.valueOf(this.id);
        return "TransactionBuilder(id=" + var10000 +
                ", createdAt=" + this.createdAt + "," +
                " moneyAmount=" + this.moneyAmount + "," +
                " description=" + this.description + "," +
                " status=" + this.status + "," +
                " transactionType=" + this.transactionType + "," +
                " source=" + this.source + "," +
                " target=" + this.target + ")";
    }
}