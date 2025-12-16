package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.dtos.TransactionRequestDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Transaction;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.exceptions.InsufficientFundsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.TransactionMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.TransactionRepository;
import com.epam.bank.services.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    @Spy
    private TransactionServiceImpl transactionService;

    private final UUID TRANSACTION_ID = UUID.randomUUID();
    private final Long SOURCE_ACCOUNT_ID = 100L;
    private final Long TARGET_ACCOUNT_ID = 200L;
    private final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(500);

    @Nested
    @DisplayName("Tests for create()")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create a PENDING transaction")
        void shouldCreatePendingTransactionSuccessfully() {
            TransactionRequestDTO requestDTO =
                    new TransactionRequestDTO(AMOUNT, "Test", TransactionType.TRANSFER,
                            SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID);

            BankAccountDTO sourceDTO = mock(BankAccountDTO.class);
            BankAccountDTO targetDTO = mock(BankAccountDTO.class);

            TransactionDTO partialDTO = new TransactionDTO();
            Transaction transactionEntity = new Transaction();
            transactionEntity.setId(TRANSACTION_ID);

            TransactionDTO expectedDTO = mock(TransactionDTO.class);

            BankAccount sourceEntity = mock(BankAccount.class);
            BankAccount targetEntity = mock(BankAccount.class);

            when(bankAccountService.getById(SOURCE_ACCOUNT_ID)).thenReturn(sourceDTO);
            when(bankAccountService.getById(TARGET_ACCOUNT_ID)).thenReturn(targetDTO);

            when(sourceDTO.bankAccountNumber()).thenReturn(SOURCE_ACCOUNT_ID);
            when(targetDTO.bankAccountNumber()).thenReturn(TARGET_ACCOUNT_ID);

            when(bankAccountRepository.findById(SOURCE_ACCOUNT_ID))
                    .thenReturn(Optional.of(sourceEntity));
            when(bankAccountRepository.findById(TARGET_ACCOUNT_ID))
                    .thenReturn(Optional.of(targetEntity));

            when(transactionMapper.toDTO(requestDTO)).thenReturn(partialDTO);
            when(transactionMapper.toEntity(partialDTO)).thenReturn(transactionEntity);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionMapper.toDTO(transactionEntity)).thenReturn(expectedDTO);

            TransactionDTO result = transactionService.create(requestDTO);

            assertThat(result).isEqualTo(expectedDTO);
            assertThat(transactionEntity.getStatus()).isEqualTo(TransactionStatus.PENDING);
        }


        @Test
        @DisplayName("Should throw NotFoundException if source account is missing")
        void shouldThrowNotFoundIfSourceMissing() {
            TransactionRequestDTO requestDTO = new TransactionRequestDTO(
                    AMOUNT, "Test transaction", TransactionType.TRANSFER, SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID
            );
            when(bankAccountService.getById(SOURCE_ACCOUNT_ID)).thenThrow(new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));

            assertThatThrownBy(() -> transactionService.create(requestDTO))
                    .isInstanceOf(NotFoundException.class);

            verify(bankAccountService, never()).getById(TARGET_ACCOUNT_ID);
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for getById, update, delete")
    class CrudTests {

        private final Transaction mockTransaction = mock(Transaction.class);
        private final TransactionDTO mockTransactionDTO = mock(TransactionDTO.class);

        @Test
        @DisplayName("Should return TransactionDTO when getById finds transaction")
        void shouldReturnTransactionById() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(mockTransaction));
            when(transactionMapper.toDTO(mockTransaction)).thenReturn(mockTransactionDTO);

            TransactionDTO result = transactionService.getById(TRANSACTION_ID);

            assertThat(result).isEqualTo(mockTransactionDTO);
        }

        @Test
        @DisplayName("Should throw NotFoundException when getById misses transaction")
        void shouldThrowNotFoundForGetById() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.getById(TRANSACTION_ID))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should update transaction successfully")
        void shouldUpdateTransaction() {
            BankAccountDTO sourceDTO = mock(BankAccountDTO.class);
            BankAccountDTO targetDTO = mock(BankAccountDTO.class);

            BankAccount sourceEntity = mock(BankAccount.class);
            BankAccount targetEntity = mock(BankAccount.class);

            when(sourceDTO.bankAccountNumber()).thenReturn(SOURCE_ACCOUNT_ID);
            when(targetDTO.bankAccountNumber()).thenReturn(TARGET_ACCOUNT_ID);

            when(mockTransactionDTO.getId()).thenReturn(TRANSACTION_ID);
            when(mockTransactionDTO.getSource()).thenReturn(sourceDTO);
            when(mockTransactionDTO.getTarget()).thenReturn(targetDTO);

            when(bankAccountRepository.findById(SOURCE_ACCOUNT_ID))
                    .thenReturn(Optional.of(sourceEntity));
            when(bankAccountRepository.findById(TARGET_ACCOUNT_ID))
                    .thenReturn(Optional.of(targetEntity));

            when(transactionRepository.findById(TRANSACTION_ID))
                    .thenReturn(Optional.of(mockTransaction));

            Transaction updatedEntity = new Transaction();

            when(transactionMapper.toEntity(mockTransactionDTO))
                    .thenReturn(updatedEntity);
            when(transactionRepository.save(updatedEntity))
                    .thenReturn(updatedEntity);
            when(transactionMapper.toDTO(updatedEntity))
                    .thenReturn(mockTransactionDTO);

            transactionService.update(mockTransactionDTO);

            verify(transactionRepository).save(updatedEntity);
        }


        @Test
        @DisplayName("Should delete transaction successfully")
        void shouldDeleteTransaction() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(mockTransaction));

            transactionService.delete(TRANSACTION_ID);

            verify(transactionRepository).delete(mockTransaction);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non-existent transaction")
        void shouldThrowNotFoundForDelete() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.delete(TRANSACTION_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }


    @Nested
    @DisplayName("Tests for doMoneyTransfer()")
    class DoMoneyTransferTests {

        private BankAccount source;
        private BankAccount target;
        private Transaction transaction;

        @BeforeEach
        void setupTransfer() {
            source = new BankAccount();
            source.setMoneyAmount(INITIAL_BALANCE);
            source.setBankAccountNumber(SOURCE_ACCOUNT_ID);

            target = new BankAccount();
            target.setMoneyAmount(INITIAL_BALANCE);
            target.setBankAccountNumber(TARGET_ACCOUNT_ID);

            transaction = new Transaction();
            transaction.setMoneyAmount(AMOUNT);
            transaction.setSource(source);
            transaction.setTarget(target);
        }

        @Test
        @DisplayName("Should transfer money between two accounts successfully")
        void shouldTransferMoneySuccessfully() {
            transactionService.doMoneyTransfer(transaction);

            BigDecimal expectedSourceBalance = INITIAL_BALANCE.subtract(AMOUNT); // 500 - 100 = 400
            BigDecimal expectedTargetBalance = INITIAL_BALANCE.add(AMOUNT);      // 500 + 100 = 600

            assertThat(source.getMoneyAmount()).isEqualByComparingTo(expectedSourceBalance);
            assertThat(target.getMoneyAmount()).isEqualByComparingTo(expectedTargetBalance);

            verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
            verify(bankAccountRepository).save(source);
            verify(bankAccountRepository).save(target);
        }

        @Test
        @DisplayName("Should apply charge (when target is null)")
        void shouldApplyChargeWhenTargetIsNull() {
            transaction.setTarget(null);

            transactionService.doMoneyTransfer(transaction);

            BigDecimal expectedSourceBalance = INITIAL_BALANCE.subtract(AMOUNT); // 400

            assertThat(source.getMoneyAmount()).isEqualByComparingTo(expectedSourceBalance);

            verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
            verify(bankAccountRepository).save(source);
            verify(bankAccountRepository, never()).save(target);
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when source balance is too low")
        void shouldThrowInsufficientFunds() {
            transaction.setMoneyAmount(BigDecimal.valueOf(600)); // request for 600 when balance is 500

            assertThatThrownBy(() -> transactionService.doMoneyTransfer(transaction))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessage("No money for paying");

            assertThat(source.getMoneyAmount()).isEqualByComparingTo(INITIAL_BALANCE);
            verify(bankAccountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when balance becomes exactly negative (boundary)")
        void shouldThrowInsufficientFundsBoundary() {
            transaction.setMoneyAmount(BigDecimal.valueOf(500.01));

            assertThatThrownBy(() -> transactionService.doMoneyTransfer(transaction))
                    .isInstanceOf(InsufficientFundsException.class);
        }
    }


    @Nested
    @DisplayName("Tests for processTransaction()")
    class ProcessTransactionTests {

        private Transaction mockTransaction;
        private BankAccount source;
        private BankAccount target;

        @BeforeEach
        void setupProcess() {
            source = new BankAccount();
            source.setMoneyAmount(INITIAL_BALANCE);
            source.setBankAccountNumber(SOURCE_ACCOUNT_ID);

            target = new BankAccount();
            target.setMoneyAmount(INITIAL_BALANCE);
            target.setBankAccountNumber(TARGET_ACCOUNT_ID);

            mockTransaction = new Transaction();
            mockTransaction.setId(TRANSACTION_ID);
            mockTransaction.setMoneyAmount(AMOUNT);
            mockTransaction.setSource(source);
            mockTransaction.setTarget(target);
            mockTransaction.setStatus(TransactionStatus.PENDING);
        }

        @Test
        @DisplayName("Should complete transaction and return COMPLETED status on success")
        void shouldCompleteTransactionOnSuccess() {
            when(transactionRepository.findById(TRANSACTION_ID))
                    .thenReturn(Optional.of(mockTransaction))
                    .thenReturn(Optional.of(mockTransaction));
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

            TransactionStatus status = transactionService.processTransaction(TRANSACTION_ID);

            assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
            verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
        }

        @Test
        @DisplayName("Should mark transaction FAILED and re-throw exception on transfer failure")
        void shouldFailTransactionOnTransferFailure() {
            mockTransaction.setMoneyAmount(BigDecimal.valueOf(600));
            when(transactionRepository.findById(TRANSACTION_ID))
                    .thenReturn(Optional.of(mockTransaction))
                    .thenReturn(Optional.of(mockTransaction));

            assertThatThrownBy(() -> transactionService.processTransaction(TRANSACTION_ID))
                    .isInstanceOf(InsufficientFundsException.class);

            verify(bankAccountRepository, never()).save(any());
            verify(transactionRepository).save(argThat(transaction -> transaction.getStatus() == TransactionStatus.FAILED));
        }
    }

    @Nested
    @DisplayName("Tests for refund()")
    class RefundTests {

        private Transaction completedTransaction;
        private BankAccount source;
        private BankAccount target;

        @BeforeEach
        void setupRefund() {
            source = new BankAccount();
            source.setBankAccountNumber(SOURCE_ACCOUNT_ID);
            source.setMoneyAmount(INITIAL_BALANCE);

            target = new BankAccount();
            target.setBankAccountNumber(TARGET_ACCOUNT_ID);
            target.setMoneyAmount(INITIAL_BALANCE);

            completedTransaction = new Transaction();
            completedTransaction.setId(TRANSACTION_ID);
            completedTransaction.setMoneyAmount(AMOUNT);
            completedTransaction.setDescription("Original payment");
            completedTransaction.setTransactionType(TransactionType.TRANSFER);
            completedTransaction.setSource(source);
            completedTransaction.setTarget(target);
            completedTransaction.setStatus(TransactionStatus.COMPLETED);

            //            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(completedTransaction));
        }

        @Test
        @DisplayName("Should successfully create and process REFUND transaction")
        void shouldCreateAndProcessRefundSuccessfully() {
            UUID refundId = UUID.randomUUID();

            BankAccount source = mock(BankAccount.class);
            BankAccount target = mock(BankAccount.class);

            when(source.getMoneyAmount()).thenReturn(AMOUNT);
            when(target.getMoneyAmount()).thenReturn(AMOUNT);
            when(source.getBankAccountNumber()).thenReturn(SOURCE_ACCOUNT_ID);
            when(target.getBankAccountNumber()).thenReturn(TARGET_ACCOUNT_ID);

            Transaction transaction = new Transaction();
            transaction.setId(refundId);
            transaction.setMoneyAmount(AMOUNT);
            transaction.setTransactionType(TransactionType.TRANSFER);
            transaction.setSource(source);
            transaction.setTarget(target);
            transaction.setStatus(TransactionStatus.PENDING);

            when(transactionRepository.findById(refundId))
                    .thenReturn(Optional.of(transaction));


            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));


            when(bankAccountRepository.save(any(BankAccount.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TransactionDTO createdDTO = mock(TransactionDTO.class);
            when(createdDTO.getId()).thenReturn(refundId);

            doReturn(createdDTO)
                    .when(transactionService)
                    .create(any(TransactionRequestDTO.class));

            TransactionStatus status = transactionService.refund(refundId);

            assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
        }



        @Test
        @DisplayName("Should throw IllegalArgumentException when attempting to refund a CHARGE")
        void shouldThrowExceptionOnChargeRefund() {
            completedTransaction.setTransactionType(TransactionType.CHARGE);

            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(completedTransaction));
            assertThatThrownBy(() -> transactionService.refund(TRANSACTION_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Can't refund charges");

            verify(transactionMapper, never()).toDTO(any(TransactionRequestDTO.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException if original transaction is missing")
        void shouldThrowNotFoundForRefund() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.refund(TRANSACTION_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(transactionMapper, never()).toDTO(any(TransactionRequestDTO.class));
        }
    }
}