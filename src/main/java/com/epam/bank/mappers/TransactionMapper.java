package com.epam.bank.mappers;

import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.Transaction;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
public interface TransactionMapper {
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "target", ignore = true)
        // source and target from service
    Transaction toEntity(TransactionDto dto);

    // ignore - in service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "target", ignore = true)
    Transaction toEntity(TransactionRequestDto dto);

    TransactionDto toDto(Transaction transaction);

    //ignore - in service if needed
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "target", ignore = true)
    TransactionDto toDto(TransactionRequestDto transactionRequestDto);

}
