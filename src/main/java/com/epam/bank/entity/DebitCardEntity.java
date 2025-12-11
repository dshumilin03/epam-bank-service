package com.epam.bank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "debit_card")
@PrimaryKeyJoinColumn(name = "card_id")
@Getter
@Setter
public class DebitCardEntity extends AbstractCardEntity {
}