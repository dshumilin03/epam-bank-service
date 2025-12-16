package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.NotFoundException;
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

    @InjectMocks
    private LoanServiceImpl loanService;

    @Captor
    private ArgumentCaptor<Loan> loanCaptor;

    private final UUID LOAN_ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID();
    private final Long BANK_ACCOUNT_ID = 100L;
    private final BigDecimal MONEY_LEFT = BigDecimal.valueOf(10000);
    private final Double PERCENT = 5.0;
    private final Long TERM_MONTHS = 12L;

    private Loan loan;
    private LoanDTO loanDTO;
    private LoanRequestDTO loanRequestDTO;
    private BankAccountDTO bankAccountDTO;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new BankAccount();
        bankAccount.setBankAccountNumber(BANK_ACCOUNT_ID);
        bankAccount.setMoneyAmount(BigDecimal.valueOf(5000));

        bankAccountDTO = new BankAccountDTO(
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

        loanDTO = new LoanDTO();
        loanDTO.setId(LOAN_ID);
        loanDTO.setMoneyLeft(MONEY_LEFT);
        loanDTO.setPercent(PERCENT);
        loanDTO.setChargeStrategyType(ChargeStrategyType.MONTHLY);
        loanDTO.setBankAccount(bankAccountDTO);
        loanDTO.setCreatedAt(LocalDateTime.now());
        loanDTO.setNextChargeAt(LocalDateTime.now().plusMonths(1));
        loanDTO.setTermMonths(TERM_MONTHS);

        loanRequestDTO = new LoanRequestDTO(
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
            when(loanMapper.toDTO(loan)).thenReturn(loanDTO);

            List<LoanDTO> result = loanService.getUserLoansByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(loanDTO);
            verify(loanRepository).findByUserId(USER_ID);
            verify(loanMapper).toDTO(loan);
        }

        @Test
        @DisplayName("Should return empty list when user has no loans")
        void shouldReturnEmptyListWhenNoLoans() {
            when(loanRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            List<LoanDTO> result = loanService.getUserLoansByUserId(USER_ID);

            assertThat(result).isEmpty();
            verify(loanRepository).findByUserId(USER_ID);
            verify(loanMapper, never()).toDTO(loan);
        }

        @Test
        @DisplayName("Should return list with multiple loans")
        void shouldReturnMultipleLoans() {
            Loan loan2 = new Loan();
            loan2.setId(UUID.randomUUID());
            loan2.setMoneyLeft(BigDecimal.valueOf(5000));

            LoanDTO loanDTO2 = new LoanDTO();
            loanDTO2.setId(loan2.getId());
            loanDTO2.setMoneyLeft(BigDecimal.valueOf(5000));

            List<Loan> loans = List.of(loan, loan2);
            when(loanRepository.findByUserId(USER_ID)).thenReturn(loans);
            when(loanMapper.toDTO(loan)).thenReturn(loanDTO);
            when(loanMapper.toDTO(loan2)).thenReturn(loanDTO2);

            List<LoanDTO> result = loanService.getUserLoansByUserId(USER_ID);

            assertThat(result).hasSize(2);
            verify(loanMapper, times(2)).toDTO(any(Loan.class));
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
            when(loanMapper.toDTO(loanRequestDTO)).thenReturn(loanDTO);
            when(bankAccountService.getById(BANK_ACCOUNT_ID)).thenReturn(bankAccountDTO);
            when(loanMapper.toEntity(loanDTO)).thenReturn(loan);
            when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
            when(loanMapper.toDTO(loan)).thenReturn(loanDTO);

            LoanDTO result = loanService.open(loanRequestDTO);

            assertThat(result).isNotNull();
            verify(loanRepository).save(loanCaptor.capture());

            Loan savedLoan = loanCaptor.getValue();
            assertThat(savedLoan.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(savedLoan.getNextChargeAt()).isCloseTo(LocalDateTime.now().plusMonths(1), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Should open DAILY loan successfully")
        void shouldOpenDailyLoanSuccessfully() {
            LoanRequestDTO dailyRequest = new LoanRequestDTO(
                    MONEY_LEFT,
                    PERCENT,
                    ChargeStrategyType.DAILY,
                    BANK_ACCOUNT_ID,
                    TERM_MONTHS
            );

            loan.setChargeStrategyType(ChargeStrategyType.DAILY);
            loanDTO.setChargeStrategyType(ChargeStrategyType.DAILY);

            when(loanMapper.toDTO(dailyRequest)).thenReturn(loanDTO);
            when(bankAccountService.getById(BANK_ACCOUNT_ID)).thenReturn(bankAccountDTO);
            when(loanMapper.toEntity(loanDTO)).thenReturn(loan);
            when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
            when(loanMapper.toDTO(loan)).thenReturn(loanDTO);

            LoanDTO result = loanService.open(dailyRequest);

            assertThat(result).isNotNull();
            verify(loanRepository).save(loanCaptor.capture());

            Loan savedLoan = loanCaptor.getValue();
            assertThat(savedLoan.getNextChargeAt()).isCloseTo(LocalDateTime.now().plusDays(1), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Should throw NotFoundException when bank account not found")
        void shouldThrowNotFoundWhenBankAccountNotFound() {
            when(loanMapper.toDTO(loanRequestDTO)).thenReturn(loanDTO);
            when(bankAccountService.getById(BANK_ACCOUNT_ID))
                    .thenThrow(new NotFoundException("Bank account not found"));

            assertThatThrownBy(() -> loanService.open(loanRequestDTO))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Bank account not found");

            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for unknown strategy type")
        void shouldThrowIllegalArgumentForUnknownStrategy() {
            loan.setChargeStrategyType(null);

            when(loanMapper.toDTO(loanRequestDTO)).thenReturn(loanDTO);
            when(bankAccountService.getById(BANK_ACCOUNT_ID)).thenReturn(bankAccountDTO);
            when(loanMapper.toEntity(loanDTO)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.open(loanRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
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
            when(loanMapper.toEntity(loanDTO)).thenReturn(updatedLoan);
            when(loanRepository.save(loan)).thenReturn(loan);
            when(loanMapper.toDTO(loan)).thenReturn(loanDTO);

            LoanDTO result = loanService.update(LOAN_ID, loanDTO);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(loanDTO);

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

            assertThatThrownBy(() -> loanService.update(LOAN_ID, loanDTO))
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
            when(loanMapper.toDTO(loan)).thenReturn(loanDTO);

            LoanDTO result = loanService.getById(LOAN_ID);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(loanDTO);
            verify(loanRepository).findById(LOAN_ID);
            verify(loanMapper).toDTO(loan);
        }

        @Test
        @DisplayName("Should throw NotFoundException when loan does not exist")
        void shouldThrowNotFoundExceptionWhenLoanNotFound() {
            when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.getById(LOAN_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Not found Loan by Id");

            verify(loanMapper, never()).toDTO(loan);
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

            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Loan.class);
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