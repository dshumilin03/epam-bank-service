package com.epam.bank.mappers;

import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransaction(TransactionDTO dto);

    Transaction toTransaction(TransactionRequestDTO dto);

    TransactionDTO toTransactionDTO(Transaction transaction);

    TransactionDTO toTransactionDTO(TransactionRequestDTO transactionRequestDTO);

}
