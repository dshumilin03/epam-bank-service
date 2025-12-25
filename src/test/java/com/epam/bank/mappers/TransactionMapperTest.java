package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.mappers.impl.TransactionMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TransactionMapperTest {

    @InjectMocks
    private TransactionMapper mapper = new TransactionMapperImpl();

    @Test
    void shouldMapDtoToEntity() {
        BankAccountDto sourceDto = new BankAccountDto(10L, BigDecimal.TEN, UUID.randomUUID(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        BankAccountDto targetDto = new BankAccountDto(20L, BigDecimal.ZERO, UUID.randomUUID(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        TransactionDto dto = new TransactionDto();
        dto.setId(UUID.randomUUID());
        dto.setMoneyAmount(new BigDecimal("100.00"));
        dto.setStatus(TransactionStatus.COMPLETED);
        dto.setTransactionType(TransactionType.TRANSFER);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setDescription("Payment");
        dto.setSourceBankAccountNumber(sourceDto.bankAccountNumber());
        dto.setTargetBankAccountNumber(targetDto.bankAccountNumber());

        Transaction entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getMoneyAmount()).isEqualTo(dto.getMoneyAmount());
        assertThat(entity.getTransactionType()).isEqualTo(dto.getTransactionType());

        assertThat(entity.getSource()).isNull();
        assertThat(entity.getTarget()).isNull();
    }

    @Test
    void shouldMapRequestDtoToEntity() {
        TransactionRequestDto request = new TransactionRequestDto(
                new BigDecimal("500.00"),
                "Deposit test",
                TransactionType.DEPOSIT,
                111L,
                222L
        );

        Transaction entity = mapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getMoneyAmount()).isEqualTo(request.moneyAmount());
        assertThat(entity.getDescription()).isEqualTo(request.description());
        assertThat(entity.getTransactionType()).isEqualTo(request.transactionType());

        assertThat(entity.getSource()).isNull();
        assertThat(entity.getTarget()).isNull();
    }

    @Test
    void shouldMapEntityToDto() {
        BankAccount sourceAcc = new BankAccount();
        sourceAcc.setBankAccountNumber(1L);

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .moneyAmount(BigDecimal.TEN)
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.TRANSFER)
                .createdAt(LocalDateTime.now())
                .description("Test transfer")
                .source(sourceAcc)
                .target(new BankAccount())
                .build();


        TransactionDto dto = mapper.toDto(transaction);

        assertThat(dto).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(transaction.getStatus());
        assertThat(dto.getTransactionType()).isEqualTo(transaction.getTransactionType());

        assertThat(dto.getSourceBankAccountNumber()).isNotNull();
    }

    @Test
    void shouldMapRequestDtoToTransactionDto() {
        TransactionRequestDto request = new TransactionRequestDto(
                new BigDecimal("777.77"),
                "Quick transfer",
                TransactionType.TRANSFER,
                30L,
                40L
        );

        TransactionDto dto = mapper.toDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getMoneyAmount()).isEqualTo(request.moneyAmount());
        assertThat(dto.getDescription()).isEqualTo(request.description());
        assertThat(dto.getTransactionType()).isEqualTo(request.transactionType());

        assertThat(dto.getSourceBankAccountNumber()).isNull();
        assertThat(dto.getTargetBankAccountNumber()).isNull();
    }
}