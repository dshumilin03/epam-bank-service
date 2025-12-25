package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BankAccountMapperTest {

    private final BankAccountMapper mapper = new BankAccountMapperImpl();

    @Test
    void shouldMapEntityToDto() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setBankAccountNumber(1001L);
        account.setMoneyAmount(new BigDecimal("500.00"));
        account.setUser(user);
        account.setCards(Collections.emptyList());
        account.setOutgoingTransactions(Collections.emptyList());
        account.setIncomingTransactions(Collections.emptyList());

        BankAccountDto dto = mapper.toDto(account);

        assertThat(dto).isNotNull();
        assertThat(dto.moneyAmount()).isEqualTo(account.getMoneyAmount());

        assertThat(dto.bankAccountNumber()).isEqualTo(account.getBankAccountNumber());

        assertThat(dto.userId()).isEqualTo(userId);

        assertThat(dto.cards()).isEmpty();
    }

    @Test
    void shouldMapDtoToEntity() {
        Long accountNumber = 2002L;
        UUID userId = UUID.randomUUID();

        BankAccountDto dto = new BankAccountDto(
                accountNumber,
                new BigDecimal("1500.50"),
                userId,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        BankAccount entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getMoneyAmount()).isEqualTo(dto.moneyAmount());

        assertThat(entity.getBankAccountNumber()).isEqualTo(dto.bankAccountNumber());

        assertThat(entity.getUser()).isNull();

        assertThat(entity.getCards()).isNull();
    }
}