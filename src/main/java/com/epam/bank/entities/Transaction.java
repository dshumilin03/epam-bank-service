package com.epam.bank.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "money_amount")
    private BigDecimal moneyAmount;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_bank_account_number", referencedColumnName = "bank_account_number")
    private BankAccount source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_bank_account_number", referencedColumnName = "bank_account_number")
    private BankAccount target;
}
