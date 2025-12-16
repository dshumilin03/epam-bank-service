package com.epam.bank.controllers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.TransactionDTO;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.services.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountControllerTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController bankAccountController;

    private MockMvc mockMvc;

    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final Long TEST_BANK_NUMBER = 123456789L;
    private final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(100.00);

    private BankAccountDTO createMockBankAccountDTO() {
        return new BankAccountDTO(
                TEST_BANK_NUMBER,
                BigDecimal.valueOf(500.00),
                TEST_USER_ID,
                List.of(),
                List.of(),
                List.of()
        );
    }

    private TransactionDTO createMockTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(UUID.randomUUID());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setMoneyAmount(TEST_AMOUNT);
        dto.setDescription("Test transaction");
        dto.setStatus(TransactionStatus.PENDING);

        BankAccountDTO mockSource = new BankAccountDTO(111L, BigDecimal.ZERO, UUID.randomUUID(), List.of(), List.of(), List.of());
        BankAccountDTO mockTarget = new BankAccountDTO(222L, BigDecimal.ZERO, UUID.randomUUID(), List.of(), List.of(), List.of());
        dto.setSource(mockSource);
        dto.setTarget(mockTarget);
        return dto;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bankAccountController)
                .build();
    }

    @Test
    void createBankAccount_ShouldReturnCreatedAccount_AndStatus200() throws Exception {
        BankAccountDTO mockDTO = createMockBankAccountDTO();
        when(bankAccountService.create(TEST_USER_ID)).thenReturn(mockDTO);

        mockMvc.perform(post("/api/bank-accounts/users/{userId}", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankAccountNumber").value(TEST_BANK_NUMBER));

        verify(bankAccountService).create(TEST_USER_ID);
    }

    @Test
    void getById_ShouldReturnAccount_AndStatus200() throws Exception {
        BankAccountDTO mockDTO = createMockBankAccountDTO();
        when(bankAccountService.getById(TEST_BANK_NUMBER)).thenReturn(mockDTO);

        mockMvc.perform(get("/api/bank-accounts/{id}", TEST_BANK_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankAccountNumber").value(TEST_BANK_NUMBER));

        verify(bankAccountService).getById(TEST_BANK_NUMBER);
    }

    @Test
    void withdraw_ShouldReturnStatusOk_AndTransactionStatus() throws Exception {
        when(bankAccountService.withdraw(TEST_BANK_NUMBER, TEST_AMOUNT)).thenReturn(TransactionStatus.PENDING);

        mockMvc.perform(patch("/api/bank-accounts/{bankNumber}", TEST_BANK_NUMBER)
                        .param("action", "withdraw")
                        .param("moneyAmount", TEST_AMOUNT.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(TransactionStatus.PENDING.name()));

        verify(bankAccountService).withdraw(TEST_BANK_NUMBER, TEST_AMOUNT);
    }

    @Test
    void deposit_ShouldReturnStatusOk_AndTransactionStatus() throws Exception {
        when(bankAccountService.deposit(TEST_BANK_NUMBER, TEST_AMOUNT)).thenReturn(TransactionStatus.COMPLETED);

        mockMvc.perform(patch("/api/bank-accounts/{bankNumber}", TEST_BANK_NUMBER)
                        .param("action", "deposit")
                        .param("moneyAmount", TEST_AMOUNT.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(TransactionStatus.COMPLETED.name()));

        verify(bankAccountService).deposit(TEST_BANK_NUMBER, TEST_AMOUNT);
    }

    @Test
    void getTransactions_OutgoingTrue_ShouldReturnTransactions_AndStatus200() throws Exception {
        List<TransactionDTO> mockTransactions = List.of(createMockTransactionDTO());
        when(bankAccountService.getTransactions(TEST_BANK_NUMBER, true)).thenReturn(mockTransactions);

        mockMvc.perform(get("/api/bank-accounts/transactions/{bankNumber}", TEST_BANK_NUMBER)
                        .param("outgoing", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moneyAmount").value(TEST_AMOUNT));

        verify(bankAccountService).getTransactions(TEST_BANK_NUMBER, true);
    }

    @Test
    void getChargesByUserId_ShouldReturnCharges_AndStatus200() throws Exception {
        List<TransactionDTO> mockCharges = List.of(createMockTransactionDTO());
        when(bankAccountService.getChargesByUserId(TEST_USER_ID)).thenReturn(mockCharges);

        mockMvc.perform(get("/api/bank-accounts/transactions/charges/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(TransactionStatus.PENDING.toString()));

        verify(bankAccountService).getChargesByUserId(TEST_USER_ID);
    }
}