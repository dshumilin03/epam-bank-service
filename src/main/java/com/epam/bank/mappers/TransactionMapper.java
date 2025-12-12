package com.epam.bank.mappers;

import com.epam.bank.dto.TransactionDTO;
import com.epam.bank.entities.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransaction(TransactionDTO dto);

    TransactionDTO toTransactionDTO(Transaction transaction);
}
