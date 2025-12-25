package com.epam.bank.controllers;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.TransactionDto;
import com.epam.bank.dtos.TransactionRequestDto;
import com.epam.bank.entities.TransactionStatus;
import com.epam.bank.entities.TransactionType;
import com.epam.bank.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final UUID TEST_TRANSACTION_ID = UUID.randomUUID();
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(50.00);
    private static final Long SOURCE_ACC = 1111L;
    private static final Long TARGET_ACC = 2222L;

    private TransactionDto createMockTransactionDto() {
        TransactionDto dto = new TransactionDto();
        dto.setId(TEST_TRANSACTION_ID);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setMoneyAmount(TEST_AMOUNT);
        dto.setDescription("Test transfer");
        dto.setStatus(TransactionStatus.PENDING);
        dto.setTransactionType(TransactionType.TRANSFER);

        BankAccountDto mockSource = new BankAccountDto(SOURCE_ACC, BigDecimal.ZERO, UUID.randomUUID(), List.of(), List.of(), List.of());
        BankAccountDto mockTarget = new BankAccountDto(TARGET_ACC, BigDecimal.ZERO, UUID.randomUUID(), List.of(), List.of(), List.of());
        dto.setSourceBankAccountNumber(mockSource.bankAccountNumber());
        dto.setTargetBankAccountNumber(mockTarget.bankAccountNumber());
        return dto;
    }

    private TransactionRequestDto createMockTransactionRequestDto() {
        return new TransactionRequestDto(
                TEST_AMOUNT,
                "Test request",
                TransactionType.TRANSFER,
                SOURCE_ACC,
                TARGET_ACC
        );
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionController)
                .build();
    }

    @Test
    void create_ShouldReturnNewTransaction_AndStatus201() throws Exception {
        TransactionRequestDto requestDto = createMockTransactionRequestDto();
        TransactionDto mockDto = createMockTransactionDto();
        when(transactionService.create(any(TransactionRequestDto.class))).thenReturn(mockDto);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_TRANSACTION_ID.toString()));

        verify(transactionService).create(any(TransactionRequestDto.class));
    }

    @Test
    void getById_ShouldReturnTransaction_AndStatus200() throws Exception {
        TransactionDto mockDto = createMockTransactionDto();
        when(transactionService.getById(TEST_TRANSACTION_ID)).thenReturn(mockDto);

        mockMvc.perform(get("/api/transactions/{transactionId}", TEST_TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_TRANSACTION_ID.toString()));

        verify(transactionService).getById(TEST_TRANSACTION_ID);
    }

    @Test
    void processTransaction_ShouldReturnStatusOk_AndTransactionStatus() throws Exception {
        when(transactionService.processTransaction(TEST_TRANSACTION_ID)).thenReturn(TransactionStatus.COMPLETED);

        mockMvc.perform(post("/api/transactions/{transactionId}", TEST_TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(TransactionStatus.COMPLETED.toString()));

        verify(transactionService).processTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    void refund_ShouldReturnStatusOk_AndTransactionStatus() throws Exception {
        when(transactionService.refund(TEST_TRANSACTION_ID)).thenReturn(TransactionStatus.REFUNDED);

        mockMvc.perform(patch("/api/transactions/{transactionId}", TEST_TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(TransactionStatus.REFUNDED.toString()));

        verify(transactionService).refund(TEST_TRANSACTION_ID);
    }

    @Test
    void update_ShouldReturnUpdatedTransaction_AndStatus200() throws Exception {
        TransactionDto updatedDto = createMockTransactionDto();
        updatedDto.setDescription("Updated description");

        TransactionDto requestBodyDto = createMockTransactionDto();
        requestBodyDto.setDescription("Updated description");

        when(transactionService.update(UUID.randomUUID(), any(TransactionDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/transactions/{transactionId}", TEST_TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBodyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(transactionService).update(UUID.randomUUID(), any(TransactionDto.class));
    }

    @Test
    void delete_ShouldReturnStatus204() throws Exception {
        doNothing().when(transactionService).delete(TEST_TRANSACTION_ID);

        mockMvc.perform(delete("/api/transactions/{transactionId}", TEST_TRANSACTION_ID))
                .andExpect(status().isNoContent());

        verify(transactionService).delete(TEST_TRANSACTION_ID);
    }
}