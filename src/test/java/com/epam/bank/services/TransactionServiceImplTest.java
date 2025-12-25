package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final UUID TRANSACTION_ID = UUID.randomUUID();
    private static final Long SOURCE_ACCOUNT_ID = 100L;
    private static final Long TARGET_ACCOUNT_ID = 200L;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(500);

    @Nested
    @DisplayName("Tests for create()")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create a PENDING transaction")
        void shouldCreatePendingTransactionSuccessfully() {
            TransactionRequestDto requestDto =
                    new TransactionRequestDto(AMOUNT, "Test", TransactionType.TRANSFER,
                            SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID);

            BankAccountDto sourceDto = mock(BankAccountDto.class);
            BankAccountDto targetDto = mock(BankAccountDto.class);

            TransactionDto partialDto = new TransactionDto();
            Transaction transactionEntity = new Transaction();
            transactionEntity.setId(TRANSACTION_ID);

            TransactionDto expectedDto = mock(TransactionDto.class);

            BankAccount sourceEntity = mock(BankAccount.class);
            BankAccount targetEntity = mock(BankAccount.class);

            when(bankAccountService.getById(SOURCE_ACCOUNT_ID)).thenReturn(sourceDto);
            when(bankAccountService.getById(TARGET_ACCOUNT_ID)).thenReturn(targetDto);

            when(sourceDto.bankAccountNumber()).thenReturn(SOURCE_ACCOUNT_ID);
            when(targetDto.bankAccountNumber()).thenReturn(TARGET_ACCOUNT_ID);

            when(bankAccountRepository.findById(SOURCE_ACCOUNT_ID))
                    .thenReturn(Optional.of(sourceEntity));
            when(bankAccountRepository.findById(TARGET_ACCOUNT_ID))
                    .thenReturn(Optional.of(targetEntity));

            when(transactionMapper.toDto(requestDto)).thenReturn(partialDto);
            when(transactionMapper.toEntity(partialDto)).thenReturn(transactionEntity);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(transactionMapper.toDto(transactionEntity)).thenReturn(expectedDto);

            TransactionDto result = transactionService.create(requestDto);

            assertThat(result).isEqualTo(expectedDto);
            assertThat(transactionEntity.getStatus()).isEqualTo(TransactionStatus.PENDING);
        }


        @Test
        @DisplayName("Should throw NotFoundException if source account is missing")
        void shouldThrowNotFoundIfSourceMissing() {
            TransactionRequestDto requestDto = new TransactionRequestDto(
                    AMOUNT, "Test transaction", TransactionType.TRANSFER, SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID
            );
            when(bankAccountService.getById(SOURCE_ACCOUNT_ID)).thenThrow(new NotFoundException("Not found bank account by bankAccountNumber (number of account)"));

            assertThatThrownBy(() -> transactionService.create(requestDto))
                    .isInstanceOf(NotFoundException.class);

            verify(bankAccountService, never()).getById(TARGET_ACCOUNT_ID);
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for getById, update, delete")
    class CrudTests {

        private final Transaction mockTransaction = mock(Transaction.class);
        private final TransactionDto mockTransactionDto = mock(TransactionDto.class);

        @Test
        @DisplayName("Should return TransactionDto when getById finds transaction")
        void shouldReturnTransactionById() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(mockTransaction));
            when(transactionMapper.toDto(mockTransaction)).thenReturn(mockTransactionDto);

            TransactionDto result = transactionService.getById(TRANSACTION_ID);

            assertThat(result).isEqualTo(mockTransactionDto);
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
            BankAccount sourceEntity = mock(BankAccount.class);
            BankAccount targetEntity = mock(BankAccount.class);

            when(mockTransactionDto.getId()).thenReturn(TRANSACTION_ID);
            when(mockTransactionDto.getSourceBankAccountNumber()).thenReturn(SOURCE_ACCOUNT_ID);
            when(mockTransactionDto.getTargetBankAccountNumber()).thenReturn(TARGET_ACCOUNT_ID);

            when(bankAccountRepository.findById(SOURCE_ACCOUNT_ID))
                    .thenReturn(Optional.of(sourceEntity));
            when(bankAccountRepository.findById(TARGET_ACCOUNT_ID))
                    .thenReturn(Optional.of(targetEntity));

            when(transactionRepository.findById(TRANSACTION_ID))
                    .thenReturn(Optional.of(mockTransaction));

            Transaction updatedEntity = new Transaction();

            when(transactionMapper.toEntity(mockTransactionDto))
                    .thenReturn(updatedEntity);
            when(transactionRepository.save(updatedEntity))
                    .thenReturn(updatedEntity);
            when(transactionMapper.toDto(updatedEntity))
                    .thenReturn(mockTransactionDto);

            transactionService.update(UUID.randomUUID(), mockTransactionDto);

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

        @BeforeEach
        void setupTransfer() {
            BankAccount source = new BankAccount();
            source.setMoneyAmount(INITIAL_BALANCE);
            source.setBankAccountNumber(SOURCE_ACCOUNT_ID);

            BankAccount target = new BankAccount();
            target.setMoneyAmount(INITIAL_BALANCE);
            target.setBankAccountNumber(TARGET_ACCOUNT_ID);

            Transaction transaction = new Transaction();
            transaction.setMoneyAmount(AMOUNT);
            transaction.setSource(source);
            transaction.setTarget(target);
        }

//        @Test
//        @DisplayName("Should transfer money between two accounts successfully")
//        void shouldTransferMoneySuccessfully() {
//            transactionService.doMoneyTransfer(transaction);
//
//            BigDecimal expectedSourceBalance = INITIAL_BALANCE.subtract(AMOUNT); // 500 - 100 = 400
//            BigDecimal expectedTargetBalance = INITIAL_BALANCE.add(AMOUNT);      // 500 + 100 = 600
//
//            assertThat(source.getMoneyAmount()).isEqualByComparingTo(expectedSourceBalance);
//            assertThat(target.getMoneyAmount()).isEqualByComparingTo(expectedTargetBalance);
//
//            verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
//            verify(bankAccountRepository).save(source);
//            verify(bankAccountRepository).save(target);
//        }
//
//        @Test
//        @DisplayName("Should apply charge (when target is null)")
//        void shouldApplyChargeWhenTargetIsNull() {
//            transaction.setTarget(null);
//
//            transactionService.doMoneyTransfer(transaction);
//
//            BigDecimal expectedSourceBalance = INITIAL_BALANCE.subtract(AMOUNT); // 400
//
//            assertThat(source.getMoneyAmount()).isEqualByComparingTo(expectedSourceBalance);
//
//            verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
//            verify(bankAccountRepository).save(source);
//            verify(bankAccountRepository, never()).save(target);
//        }

//        @Test
//        @DisplayName("Should throw InsufficientFundsException when source balance is too low")
//        void shouldThrowInsufficientFunds() {
//            transaction.setMoneyAmount(BigDecimal.valueOf(600)); // request for 600 when balance is 500
//
//            assertThatThrownBy(() -> transactionService.doMoneyTransfer(transaction))
//                    .isInstanceOf(InsufficientFundsException.class)
//                    .hasMessage("No money for paying");
//
//            assertThat(source.getMoneyAmount()).isEqualByComparingTo(INITIAL_BALANCE);
//            verify(bankAccountRepository, never()).save(any());
//        }

//        @Test
//        @DisplayName("Should throw InsufficientFundsException when balance becomes exactly negative (boundary)")
//        void shouldThrowInsufficientFundsBoundary() {
//            transaction.setMoneyAmount(BigDecimal.valueOf(500.01));
//
//            assertThatThrownBy(() -> transactionService.doMoneyTransfer(transaction))
//                    .isInstanceOf(InsufficientFundsException.class);
//        }
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

        }

        @Test
        @DisplayName("Should successfully create and process REFUND transaction")
        void shouldCreateAndProcessRefundSuccessfully() {
            UUID refundId = UUID.randomUUID();

            source = mock(BankAccount.class);
            target = mock(BankAccount.class);

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

            TransactionDto createdDto = mock(TransactionDto.class);
            when(createdDto.getId()).thenReturn(refundId);

            doReturn(createdDto)
                    .when(transactionService)
                    .create(any(TransactionRequestDto.class));

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

            verify(transactionMapper, never()).toDto(any(TransactionRequestDto.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException if original transaction is missing")
        void shouldThrowNotFoundForRefund() {
            when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.refund(TRANSACTION_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(transactionMapper, never()).toDto(any(TransactionRequestDto.class));
        }
    }
}