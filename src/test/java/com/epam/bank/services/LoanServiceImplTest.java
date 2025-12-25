package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.LoanDto;
import com.epam.bank.dtos.LoanRequestDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.exceptions.UnknownStrategyTypeException;
import com.epam.bank.mappers.LoanMapper;
import com.epam.bank.repositories.LoanRepository;
import com.epam.bank.services.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private LoanMapper loanMapper;

    @Mock

    @InjectMocks
    private LoanServiceImpl loanService;

    @Captor
    private ArgumentCaptor<Loan> loanCaptor;

    private static final UUID LOAN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Long BANK_ACCOUNT_ID = 100L;
    private static final BigDecimal MONEY_LEFT = BigDecimal.valueOf(10000);
    private static final Double PERCENT = 5.0;
    private static final Long TERM_MONTHS = 12L;

    private Loan loan;
    private LoanDto loanDto;
    private LoanRequestDto loanRequestDto;
    private BankAccountDto bankAccountDto;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new BankAccount();
        bankAccount.setBankAccountNumber(BANK_ACCOUNT_ID);
        bankAccount.setMoneyAmount(BigDecimal.valueOf(5000));

        bankAccountDto = new BankAccountDto(
                BANK_ACCOUNT_ID,
                BigDecimal.valueOf(5000),
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        loan = new Loan();
        loan.setId(LOAN_ID);
        loan.setMoneyLeft(MONEY_LEFT);
        loan.setPercent(PERCENT);
        loan.setChargeStrategyType(ChargeStrategyType.MONTHLY);
        loan.setBankAccount(bankAccount);
        loan.setCreatedAt(LocalDateTime.now());
        loan.setNextChargeAt(LocalDateTime.now().plusMonths(1));
        loan.setTermMonths(TERM_MONTHS);

        loanDto = new LoanDto();
        loanDto.setId(LOAN_ID);
        loanDto.setMoneyLeft(MONEY_LEFT);
        loanDto.setPercent(PERCENT);
        loanDto.setChargeStrategyType(ChargeStrategyType.MONTHLY);
        loanDto.setBankAccount(bankAccountDto);
        loanDto.setCreatedAt(LocalDateTime.now());
        loanDto.setNextChargeAt(LocalDateTime.now().plusMonths(1));
        loanDto.setTermMonths(TERM_MONTHS);

        loanRequestDto = new LoanRequestDto(
                MONEY_LEFT,
                PERCENT,
                ChargeStrategyType.MONTHLY,
                BANK_ACCOUNT_ID,
                TERM_MONTHS
        );
    }

    @Nested
    @DisplayName("Tests for getUserLoansByUserId()")
    class GetUserLoansByUserIdTests {

        @Test
        @DisplayName("Should return list of user loans")
        void shouldReturnUserLoans() {
            List<Loan> loans = List.of(loan);
            when(loanRepository.findByUserId(USER_ID)).thenReturn(loans);
            when(loanMapper.toDto(loan)).thenReturn(loanDto);

            List<LoanDto> result = loanService.getUserLoansByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(loanDto);
            verify(loanRepository).findByUserId(USER_ID);
            verify(loanMapper).toDto(loan);
        }

        @Test
        @DisplayName("Should return empty list when user has no loans")
        void shouldReturnEmptyListWhenNoLoans() {
            when(loanRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            List<LoanDto> result = loanService.getUserLoansByUserId(USER_ID);

            assertThat(result).isEmpty();
            verify(loanRepository).findByUserId(USER_ID);
            verify(loanMapper, never()).toDto(loan);
        }

        @Test
        @DisplayName("Should return list with multiple loans")
        void shouldReturnMultipleLoans() {
            Loan loan2 = new Loan();
            loan2.setId(UUID.randomUUID());
            loan2.setMoneyLeft(BigDecimal.valueOf(5000));

            LoanDto loanDto2 = new LoanDto();
            loanDto2.setId(loan2.getId());
            loanDto2.setMoneyLeft(BigDecimal.valueOf(5000));

            List<Loan> loans = List.of(loan, loan2);
            when(loanRepository.findByUserId(USER_ID)).thenReturn(loans);
            when(loanMapper.toDto(loan)).thenReturn(loanDto);
            when(loanMapper.toDto(loan2)).thenReturn(loanDto2);

            List<LoanDto> result = loanService.getUserLoansByUserId(USER_ID);

            assertThat(result).hasSize(2);
            verify(loanMapper, times(2)).toDto(any(Loan.class));
        }
    }

    @Nested
    @DisplayName("Tests for close()")
    class CloseTests {

        @Test
        @DisplayName("Should close loan successfully")
        void shouldCloseLoanSuccessfully() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));

            loanService.close(LOAN_ID);

            verify(loanRepository).findById(LOAN_ID);
            verify(loanRepository).delete(loan);
        }

        @Test
        @DisplayName("Should throw NotFoundException when loan does not exist")
        void shouldThrowNotFoundExceptionWhenLoanNotFound() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.close(LOAN_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Loan not found by Id");

            verify(loanRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Tests for open()")
    class OpenTests {

        @Test
        @DisplayName("Should open MONTHLY loan successfully")
        void shouldOpenMonthlyLoanSuccessfully() {
            when(loanMapper.toDto(loanRequestDto)).thenReturn(loanDto);
            when(bankAccountService.getById(BANK_ACCOUNT_ID)).thenReturn(bankAccountDto);
            when(loanMapper.toEntity(loanDto)).thenReturn(loan);
            when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
            when(loanMapper.toDto(loan)).thenReturn(loanDto);

            LoanDto result = loanService.open(loanRequestDto);

            assertThat(result).isNotNull();
            verify(loanRepository).save(loanCaptor.capture());

            Loan savedLoan = loanCaptor.getValue();
            assertThat(savedLoan.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(savedLoan.getNextChargeAt()).isCloseTo(LocalDateTime.now().plusMonths(1), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Should open DAILY loan successfully")
        void shouldOpenDailyLoanSuccessfully() {
            LoanRequestDto dailyRequest = new LoanRequestDto(
                    MONEY_LEFT,
                    PERCENT,
                    ChargeStrategyType.DAILY,
                    BANK_ACCOUNT_ID,
                    TERM_MONTHS
            );

            loan.setChargeStrategyType(ChargeStrategyType.DAILY);
            loanDto.setChargeStrategyType(ChargeStrategyType.DAILY);

            when(loanMapper.toDto(dailyRequest)).thenReturn(loanDto);
            when(bankAccountService.getById(BANK_ACCOUNT_ID)).thenReturn(bankAccountDto);
            when(loanMapper.toEntity(loanDto)).thenReturn(loan);
            when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
            when(loanMapper.toDto(loan)).thenReturn(loanDto);

            LoanDto result = loanService.open(dailyRequest);

            assertThat(result).isNotNull();
            verify(loanRepository).save(loanCaptor.capture());

            Loan savedLoan = loanCaptor.getValue();
            assertThat(savedLoan.getNextChargeAt()).isCloseTo(LocalDateTime.now().plusDays(1), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Should throw NotFoundException when bank account not found")
        void shouldThrowNotFoundWhenBankAccountNotFound() {
            when(loanMapper.toDto(loanRequestDto)).thenReturn(loanDto);
            when(bankAccountService.getById(BANK_ACCOUNT_ID))
                    .thenThrow(new NotFoundException("Bank account not found"));

            assertThatThrownBy(() -> loanService.open(loanRequestDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Bank account not found");

            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnknownStrategyTypeException for unknown strategy type")
        void shouldThrowUnknownStrategyTypeForUnknownStrategy() {
            loan.setChargeStrategyType(null);

            when(loanMapper.toDto(loanRequestDto)).thenReturn(loanDto);
            when(bankAccountService.getById(BANK_ACCOUNT_ID)).thenReturn(bankAccountDto);
            when(loanMapper.toEntity(loanDto)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.open(loanRequestDto))
                    .isInstanceOf(UnknownStrategyTypeException.class)
                    .hasMessage("Unknown strategy type");

            verify(loanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for update()")
    class UpdateTests {

        @Test
        @DisplayName("Should update loan successfully")
        void shouldUpdateLoanSuccessfully() {
            Loan updatedLoan = new Loan();
            updatedLoan.setMoneyLeft(BigDecimal.valueOf(8000));
            updatedLoan.setPercent(6.0);
            updatedLoan.setChargeStrategyType(ChargeStrategyType.DAILY);
            updatedLoan.setBankAccount(bankAccount);
            updatedLoan.setCreatedAt(LocalDateTime.now());
            updatedLoan.setNextChargeAt(LocalDateTime.now().plusDays(1));
            updatedLoan.setLastChargeAt(LocalDateTime.now().minusDays(1));
            updatedLoan.setTermMonths(24L);

            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
            when(loanMapper.toEntity(loanDto)).thenReturn(updatedLoan);
            when(loanRepository.save(loan)).thenReturn(loan);
            when(loanMapper.toDto(loan)).thenReturn(loanDto);

            LoanDto result = loanService.update(LOAN_ID, loanDto);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(loanDto);

            verify(loanRepository).findById(LOAN_ID);
            verify(loanRepository).save(loan);

            assertThat(loan.getMoneyLeft()).isEqualTo(updatedLoan.getMoneyLeft());
            assertThat(loan.getPercent()).isEqualTo(updatedLoan.getPercent());
            assertThat(loan.getChargeStrategyType()).isEqualTo(updatedLoan.getChargeStrategyType());
        }

        @Test
        @DisplayName("Should throw NotFoundException when loan does not exist")
        void shouldThrowNotFoundExceptionWhenUpdatingNonExistentLoan() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.update(LOAN_ID, loanDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Loan not found by Id");

            verify(loanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return loan by ID")
        void shouldReturnLoanById() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
            when(loanMapper.toDto(loan)).thenReturn(loanDto);

            LoanDto result = loanService.getById(LOAN_ID);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(loanDto);
            verify(loanRepository).findById(LOAN_ID);
            verify(loanMapper).toDto(loan);
        }

        @Test
        @DisplayName("Should throw NotFoundException when loan does not exist")
        void shouldThrowNotFoundExceptionWhenLoanNotFound() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.getById(LOAN_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Not found Loan by Id");

            verify(loanMapper, never()).toDto(loan);
        }
    }

    @Nested
    @DisplayName("Tests for getEntityById()")
    class GetEntityByIdTests {

        @Test
        @DisplayName("Should return Chargeable entity by ID")
        void shouldReturnChargeableEntityById() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));

            Chargeable result = loanService.getEntityById(LOAN_ID);

            assertThat(result).isEqualTo(loan);
            verify(loanRepository).findById(LOAN_ID);
        }

        @Test
        @DisplayName("Should throw NotFoundException when loan entity does not exist")
        void shouldThrowNotFoundExceptionWhenEntityNotFound() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.getEntityById(LOAN_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Not found Loan by Id");
        }
    }
}