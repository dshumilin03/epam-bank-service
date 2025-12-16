package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.User;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.BankAccountMapper;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.services.impl.BankAccountServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private final UUID USER_ID = UUID.randomUUID();
    private final Long ACCOUNT_ID = 1L;
    private final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000);

    @Nested
    @DisplayName("Tests for create()")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create a bank account for existing user")
        void shouldCreateAccountSuccessfully() {
            User user = new User();
            user.setId(USER_ID);

            BankAccount savedAccount = new BankAccount();
            savedAccount.setBankAccountNumber(ACCOUNT_ID);
            savedAccount.setMoneyAmount(BigDecimal.ZERO);
            savedAccount.setUser(user);

            UserDTO userDTO = mock(UserDTO.class);
            BankAccountDTO expectedDTO = new BankAccountDTO(
                    ACCOUNT_ID, BigDecimal.ZERO, userDTO.getId(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(savedAccount);
            when(bankAccountMapper.toDTO(savedAccount)).thenReturn(expectedDTO);

            BankAccountDTO result = bankAccountService.create(USER_ID);

            assertThat(result).isEqualTo(expectedDTO);
            verify(bankAccountRepository).save(any(BankAccount.class));
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.create(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found by Id");

            verify(bankAccountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return BankAccountDTO when account exists")
        void shouldReturnAccountById() {
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);

            BankAccountDTO expectedDTO = mock(BankAccountDTO.class);

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(bankAccountMapper.toDTO(account)).thenReturn(expectedDTO);

            BankAccountDTO result = bankAccountService.getById(ACCOUNT_ID);

            assertThat(result).isEqualTo(expectedDTO);
        }

        @Test
        @DisplayName("Should throw NotFoundException when account not found")
        void shouldThrowWhenAccountNotFound() {
            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.getById(ACCOUNT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Not found bank account by bankAccountNumber (number of account)");
        }
    }

    @Nested
    @DisplayName("Tests for getTransactions()")
    class GetTransactionsTests {

        @Test
        @DisplayName("Should return list of outgoing transactions")
        void shouldReturnOutgoingTransactions() {
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);

            Transaction transaction = new Transaction();
            List<Transaction> outgoingList = List.of(transaction);
            account.setOutgoingTransactions(outgoingList);

            TransactionDTO transactionDTO = new TransactionDTO();

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(transactionMapper.toDTO(transaction)).thenReturn(transactionDTO);

            List<TransactionDTO> result = bankAccountService.getTransactions(ACCOUNT_ID, true);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(transactionDTO);
            verify(transactionMapper).toDTO(transaction);
        }

        @Test
        @DisplayName("Should return list of incoming transactions")
        void shouldReturnIncomingTransactions() {
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);

            Transaction transaction = new Transaction();
            List<Transaction> incomingList = List.of(transaction);
            account.setIncomingTransactions(incomingList);
            account.setOutgoingTransactions(new ArrayList<>());

            TransactionDTO transactionDTO = new TransactionDTO();

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(transactionMapper.toDTO(transaction)).thenReturn(transactionDTO);

            List<TransactionDTO> result = bankAccountService.getTransactions(ACCOUNT_ID, false);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(transactionDTO);
        }

        @Test
        @DisplayName("Should throw NotFoundException when account not found")
        void shouldThrowWhenAccountMissing() {
            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.getTransactions(ACCOUNT_ID, true))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Tests for deposit()")
    class DepositTests {

        @Test
        @DisplayName("Should successfully deposit money and return COMPLETED")
        void shouldDepositMoneySuccessfully() {
            BigDecimal depositAmount = BigDecimal.valueOf(500);
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);
            account.setMoneyAmount(INITIAL_BALANCE); // 1000

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(bankAccountRepository.save(account)).thenReturn(account);

            TransactionStatus status = bankAccountService.deposit(ACCOUNT_ID, depositAmount);

            assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(account.getMoneyAmount()).isEqualTo(INITIAL_BALANCE.add(depositAmount)); // 1500
            verify(bankAccountRepository).save(account);
        }

        @Test
        @DisplayName("Should throw NotFoundException on deposit if account missing")
        void shouldThrowWhenAccountMissingOnDeposit() {
            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.deposit(ACCOUNT_ID, BigDecimal.TEN))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Tests for withdraw()")
    class WithdrawTests {

        @Test
        @DisplayName("Should successfully withdraw money when balance is sufficient")
        void shouldWithdrawMoneySuccessfully() {
            BigDecimal withdrawAmount = BigDecimal.valueOf(500);
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);
            account.setMoneyAmount(INITIAL_BALANCE); // 1000

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(bankAccountRepository.save(account)).thenReturn(account);

            TransactionStatus status = bankAccountService.withdraw(ACCOUNT_ID, withdrawAmount);

            assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(account.getMoneyAmount()).isEqualTo(INITIAL_BALANCE.subtract(withdrawAmount)); // 500
            verify(bankAccountRepository).save(account);
        }

        @Test
        @DisplayName("Should return FAILED when balance is insufficient")
        void shouldFailWithdrawWhenInsufficientFunds() {
            BigDecimal withdrawAmount = BigDecimal.valueOf(2000); // more than 1000
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);
            account.setMoneyAmount(INITIAL_BALANCE);

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

            TransactionStatus status = bankAccountService.withdraw(ACCOUNT_ID, withdrawAmount);

            assertThat(status).isEqualTo(TransactionStatus.FAILED);
            assertThat(account.getMoneyAmount()).isEqualTo(INITIAL_BALANCE);
            verify(bankAccountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return FAILED when balance becomes exactly negative (boundary check)")
        void shouldFailWithdrawWhenBalanceWouldBeNegative() {
            BigDecimal current = BigDecimal.valueOf(100);
            BigDecimal withdraw = BigDecimal.valueOf(100.01);
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);
            account.setMoneyAmount(current);

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

            TransactionStatus status = bankAccountService.withdraw(ACCOUNT_ID, withdraw);

            assertThat(status).isEqualTo(TransactionStatus.FAILED);
        }

        @Test
        @DisplayName("Should succeed when balance becomes exactly zero")
        void shouldSucceedWhenBalanceBecomesZero() {
            BigDecimal amount = BigDecimal.valueOf(100);
            BankAccount account = new BankAccount();
            account.setBankAccountNumber(ACCOUNT_ID);
            account.setMoneyAmount(amount);

            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

            TransactionStatus status = bankAccountService.withdraw(ACCOUNT_ID, amount);

            assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(account.getMoneyAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(bankAccountRepository).save(account);
        }

        @Test
        @DisplayName("Should throw NotFoundException on withdraw if account missing")
        void shouldThrowWhenAccountMissingOnWithdraw() {
            when(bankAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.withdraw(ACCOUNT_ID, BigDecimal.TEN))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
