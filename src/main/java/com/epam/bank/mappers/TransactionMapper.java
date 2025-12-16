package com.epam.bank.mappers;

import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.Transaction;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
public interface TransactionMapper {
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "target", ignore = true)
        // source and target from service
    Transaction toEntity(TransactionDTO dto);

    // ignore - in service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "target", ignore = true)
    Transaction toEntity(TransactionRequestDTO dto);

    TransactionDTO toDTO(Transaction transaction);

    //ignore - in service if needed
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "target", ignore = true)
    TransactionDTO toDTO(TransactionRequestDTO transactionRequestDTO);

}
