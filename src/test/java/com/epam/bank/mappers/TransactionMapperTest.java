package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.mappers.impl.TransactionMapperImpl;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionMapperTest {

    @InjectMocks
    private TransactionMapper mapper = new TransactionMapperImpl();

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Test
    void shouldMapDTOToEntity() {
        BankAccountDTO sourceDTO = new BankAccountDTO(10L, BigDecimal.TEN, UUID.randomUUID(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        BankAccountDTO targetDTO = new BankAccountDTO(20L, BigDecimal.ZERO, UUID.randomUUID(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        TransactionDTO dto = new TransactionDTO();
        dto.setId(UUID.randomUUID());
        dto.setMoneyAmount(new BigDecimal("100.00"));
        dto.setStatus(TransactionStatus.COMPLETED);
        dto.setTransactionType(TransactionType.TRANSFER);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setDescription("Payment");
        dto.setSourceBankAccountNumber(sourceDTO.bankAccountNumber());
        dto.setTargetBankAccountNumber(targetDTO.bankAccountNumber());

        Transaction entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getMoneyAmount()).isEqualTo(dto.getMoneyAmount());
        assertThat(entity.getTransactionType()).isEqualTo(dto.getTransactionType());

        assertThat(entity.getSource()).isNull();
        assertThat(entity.getTarget()).isNull();
    }

    @Test
    void shouldMapRequestDTOToEntity() {
        TransactionRequestDTO request = new TransactionRequestDTO(
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
    void shouldMapEntityToDTO() {
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

        when(bankAccountMapper.toDTO(any(BankAccount.class)))
                .thenAnswer(invocation -> {
                    BankAccount source = invocation.getArgument(0);
                    return new BankAccountDTO(
                            source.getBankAccountNumber(),
                            source.getMoneyAmount(),
                            null, null, null, null
                    );
                });


        TransactionDTO dto = mapper.toDTO(transaction);

        assertThat(dto).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(transaction.getStatus());
        assertThat(dto.getTransactionType()).isEqualTo(transaction.getTransactionType());

        assertThat(dto.getSourceBankAccountNumber()).isNotNull();
    }

    @Test
    void shouldMapRequestDTOToTransactionDTO() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                new BigDecimal("777.77"),
                "Quick transfer",
                TransactionType.TRANSFER,
                30L,
                40L
        );

        TransactionDTO dto = mapper.toDTO(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getMoneyAmount()).isEqualTo(request.moneyAmount());
        assertThat(dto.getDescription()).isEqualTo(request.description());
        assertThat(dto.getTransactionType()).isEqualTo(request.transactionType());

        assertThat(dto.getSourceBankAccountNumber()).isNull();
        assertThat(dto.getTargetBankAccountNumber()).isNull();
    }
}