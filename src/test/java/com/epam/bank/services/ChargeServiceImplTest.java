package com.epam.bank.services;

import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.impl.ChargeServiceImpl;
import com.epam.bank.services.strategies.ChargeStrategy;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ChargeStrategy dailyStrategy;

    @Mock
    private ChargeStrategy monthlyStrategy;

    @Mock
    private Chargeable chargeable;

    @InjectMocks
    private ChargeServiceImpl chargeService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> dateCaptor;

    @BeforeEach
    void setUp() {
        Map<ChargeStrategyType, ChargeStrategy> mockStrategies = Map.of(
                ChargeStrategyType.DAILY, dailyStrategy,
                ChargeStrategyType.MONTHLY, monthlyStrategy
        );
        ReflectionTestUtils.setField(chargeService, "strategies", mockStrategies);
    }

    @Nested
    @DisplayName("Tests for applyCharge()")
    class ApplyChargeTests {

        @Test
        @DisplayName("Should apply DAILY charge correctly")
        void shouldApplyDailyCharge() {
            BigDecimal debt = BigDecimal.valueOf(1000);
            double percent = 10.0;
            BigDecimal calculatedCharge = BigDecimal.valueOf(100);
            BankAccount bankAccount = new BankAccount();
            bankAccount.setBankAccountNumber(1L);

            when(chargeable.getChargeStrategyType()).thenReturn(ChargeStrategyType.DAILY);
            when(chargeable.getDebt()).thenReturn(debt);
            when(chargeable.getPercent()).thenReturn(percent);
            when(chargeable.getBankAccount()).thenReturn(bankAccount);

            when(dailyStrategy.calculateCharge(debt, percent)).thenReturn(calculatedCharge);

            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            chargeService.applyCharge(chargeable);

            verify(dailyStrategy).calculateCharge(debt, percent);

            verify(chargeable).setLastChargeAt(dateCaptor.capture());
            LocalDateTime lastChargeDate = dateCaptor.getValue();
            assertThat(lastChargeDate).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

            verify(chargeable).setNextChargeAt(dateCaptor.capture());
            LocalDateTime nextChargeDate = dateCaptor.getValue();
            assertThat(nextChargeDate).isCloseTo(LocalDateTime.now().plusDays(1), within(1, ChronoUnit.SECONDS));

            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction savedTransaction = transactionCaptor.getValue();

            assertThat(savedTransaction.getSource()).isEqualTo(bankAccount);
            assertThat(savedTransaction.getMoneyAmount()).isEqualTo(calculatedCharge);
            assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.CHARGE);
            assertThat(savedTransaction.getDescription()).isEqualTo("This is charge");
        }

        @Test
        @DisplayName("Should apply MONTHLY charge correctly")
        void shouldApplyMonthlyCharge() {
            BigDecimal debt = BigDecimal.valueOf(5000);
            Double percent = 5.0;
            BigDecimal calculatedCharge = BigDecimal.valueOf(250);
            BankAccount bankAccount = new BankAccount();
            bankAccount.setBankAccountNumber(2L);

            when(chargeable.getChargeStrategyType()).thenReturn(ChargeStrategyType.MONTHLY);
            when(chargeable.getDebt()).thenReturn(debt);
            when(chargeable.getPercent()).thenReturn(percent);
            when(chargeable.getBankAccount()).thenReturn(bankAccount);

            when(monthlyStrategy.calculateCharge(debt, percent)).thenReturn(calculatedCharge);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            chargeService.applyCharge(chargeable);

            verify(monthlyStrategy).calculateCharge(debt, percent);

            verify(chargeable).setNextChargeAt(dateCaptor.capture());
            LocalDateTime nextChargeDate = dateCaptor.getValue();
            assertThat(nextChargeDate).isCloseTo(LocalDateTime.now().plusMonths(1), within(1, ChronoUnit.SECONDS));

            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction savedTransaction = transactionCaptor.getValue();
            assertThat(savedTransaction.getMoneyAmount()).isEqualTo(calculatedCharge);
            assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.CHARGE);

            verify(transactionMapper).toDto(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException if strategy not found for type")
        void shouldThrowExceptionWhenStrategyNotFound() {
            when(chargeable.getChargeStrategyType()).thenReturn(null);

            org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> chargeService.applyCharge(chargeable));

            verify(transactionRepository, never()).save(any());
        }
    }
}