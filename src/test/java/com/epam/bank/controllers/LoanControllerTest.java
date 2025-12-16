package com.epam.bank.controllers;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.LoanDTO;
import com.epam.bank.dtos.LoanRequestDTO;
import com.epam.bank.entities.ChargeStrategyType;
import com.epam.bank.services.LoanService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID TEST_LOAN_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final Long TEST_BANK_NUMBER = 123456789L;
    private final BigDecimal LOAN_AMOUNT = BigDecimal.valueOf(10000.00);

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

    private LoanDTO createMockLoanDTO() {
        LoanDTO dto = new LoanDTO();
        dto.setId(TEST_LOAN_ID);
        dto.setMoneyLeft(LOAN_AMOUNT);
        dto.setPercent(5.0);
        dto.setChargeStrategyType(ChargeStrategyType.MONTHLY);
        dto.setBankAccount(createMockBankAccountDTO());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setNextChargeAt(LocalDateTime.now().plusMonths(1));
        dto.setLastChargeAt(LocalDateTime.now());
        dto.setTermMonths(12L);
        return dto;
    }

    private LoanRequestDTO createMockLoanRequestDTO() {
        return new LoanRequestDTO(
                LOAN_AMOUNT,
                5.0,
                ChargeStrategyType.MONTHLY,
                TEST_BANK_NUMBER,
                12L
        );
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(loanController)
                .build();
    }

    @Test
    void open_ShouldReturnNewLoan_AndStatus201() throws Exception {
        LoanRequestDTO requestDTO = createMockLoanRequestDTO();
        LoanDTO mockLoan = createMockLoanDTO();
        when(loanService.open(any(LoanRequestDTO.class))).thenReturn(mockLoan);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_LOAN_ID.toString()));

        verify(loanService).open(any(LoanRequestDTO.class));
    }

    @Test
    void getById_ShouldReturnLoan_AndStatus200() throws Exception {
        LoanDTO mockLoan = createMockLoanDTO();
        when(loanService.getById(TEST_LOAN_ID)).thenReturn(mockLoan);

        mockMvc.perform(get("/api/loans/{loanId}", TEST_LOAN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_LOAN_ID.toString()));

        verify(loanService).getById(TEST_LOAN_ID);
    }

    @Test
    void getUserLoansByUserId_ShouldReturnLoanList_AndStatus200() throws Exception {
        List<LoanDTO> mockLoans = List.of(createMockLoanDTO());
        when(loanService.getUserLoansByUserId(TEST_USER_ID)).thenReturn(mockLoans);

        mockMvc.perform(get("/api/loans/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_LOAN_ID.toString()));

        verify(loanService).getUserLoansByUserId(TEST_USER_ID);
    }

    @Test
    void close_ShouldReturnStatus200() throws Exception {
        doNothing().when(loanService).close(TEST_LOAN_ID);

        mockMvc.perform(delete("/api/loans/{loanId}", TEST_LOAN_ID))
                .andExpect(status().isOk());

        verify(loanService).close(TEST_LOAN_ID);
    }

    @Test
    void update_ShouldReturnUpdatedLoan_AndStatus200() throws Exception {
        LoanDTO updatedDTO = createMockLoanDTO();
        updatedDTO.setPercent(7.5);

        LoanDTO requestBodyDTO = createMockLoanDTO();
        requestBodyDTO.setPercent(7.5);

        when(loanService.update(eq(TEST_LOAN_ID), any(LoanDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/loans/{loanId}", TEST_LOAN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBodyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percent").value(7.5));

        verify(loanService).update(eq(TEST_LOAN_ID), any(LoanDTO.class));
    }
}