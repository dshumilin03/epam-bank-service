package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanMapperTest {

    @InjectMocks
    private LoanMapper mapper = Mappers.getMapper(LoanMapper.class);

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Test
    void shouldMapEntityToDTO() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankAccountNumber(555L);

        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setMoneyLeft(new BigDecimal("10000.00"));
        loan.setPercent(12.5);
        loan.setTermMonths(12L);
        loan.setCreatedAt(LocalDateTime.now().minusDays(1));
        loan.setNextChargeAt(LocalDateTime.now().plusDays(30));
        loan.setLastChargeAt(LocalDateTime.now());
        loan.setChargeStrategyType(ChargeStrategyType.MONTHLY);
        loan.setBankAccount(bankAccount);

        when(bankAccountMapper.toDTO(any(BankAccount.class)))
                .thenAnswer(invocation -> {
                    BankAccount source = invocation.getArgument(0);
                    return new BankAccountDTO(
                            source.getBankAccountNumber(),
                            source.getMoneyAmount(),
                            null, null, null, null
                    );
                });


        LoanDTO dto = mapper.toDTO(loan);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(loan.getId());
        assertThat(dto.getMoneyLeft()).isEqualTo(loan.getMoneyLeft());
        assertThat(dto.getPercent()).isEqualTo(loan.getPercent());
        assertThat(dto.getTermMonths()).isEqualTo(loan.getTermMonths());
        assertThat(dto.getCreatedAt()).isEqualTo(loan.getCreatedAt());

        assertThat(dto.getNextChargeAt()).isEqualTo(loan.getNextChargeAt());
        assertThat(dto.getLastChargeAt()).isEqualTo(loan.getLastChargeAt());
        assertThat(dto.getChargeStrategyType()).isEqualTo(loan.getChargeStrategyType());

        assertThat(dto.getBankAccount()).isNotNull();
        assertThat(dto.getBankAccount().bankAccountNumber()).isEqualTo(bankAccount.getBankAccountNumber());
    }

    @Test
    void shouldMapDTOToEntity() {
        BankAccountDTO accountDTO = new BankAccountDTO(
                777L, BigDecimal.ZERO, UUID.randomUUID(), java.util.Collections.emptyList(), java.util.Collections.emptyList(), java.util.Collections.emptyList()
        );

        LoanDTO dto = new LoanDTO();
        dto.setId(UUID.randomUUID());
        dto.setMoneyLeft(new BigDecimal("5000.00"));
        dto.setPercent(5.0);
        dto.setChargeStrategyType(ChargeStrategyType.DAILY);
        dto.setTermMonths(24L);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setNextChargeAt(LocalDateTime.now().plusDays(1));
        dto.setLastChargeAt(LocalDateTime.now().minusDays(1));
        dto.setBankAccount(accountDTO);

        Loan entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getMoneyLeft()).isEqualTo(dto.getMoneyLeft());
        assertThat(entity.getPercent()).isEqualTo(dto.getPercent());
        assertThat(entity.getTermMonths()).isEqualTo(dto.getTermMonths());

        assertThat(entity.getChargeStrategyType()).isEqualTo(dto.getChargeStrategyType());
        assertThat(entity.getNextChargeAt()).isEqualTo(dto.getNextChargeAt());
    }

    @Test
    void shouldMapRequestToDTO() {
        LoanRequestDTO request = new LoanRequestDTO(
                new BigDecimal("20000.00"),
                15.0,
                ChargeStrategyType.MONTHLY,
                123456789L,
                36L
        );

        LoanDTO dto = mapper.toDTO(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getMoneyLeft()).isEqualTo(request.moneyLeft());
        assertThat(dto.getPercent()).isEqualTo(request.percent());
        assertThat(dto.getChargeStrategyType()).isEqualTo(request.chargeStrategyType());
        assertThat(dto.getTermMonths()).isEqualTo(request.termMonths());

        assertThat(dto.getBankAccount()).isNull();
    }
}