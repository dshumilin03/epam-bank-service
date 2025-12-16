package com.epam.bank.controllers;

import com.epam.bank.entities.Loan;
import com.epam.bank.exceptions.GlobalExceptionHandler;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.services.ChargeService;
import com.epam.bank.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChargeControllerTest {

    @Mock
    private ChargeService chargeService;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private ChargeController chargeController;

    private MockMvc mockMvc;

    private final UUID TEST_LOAN_ID = UUID.randomUUID();

    private Loan createMockLoanEntity() {
        Loan mockLoan = mock(Loan.class);
        when(mockLoan.getId()).thenReturn(TEST_LOAN_ID);
        return mockLoan;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(chargeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void applyCharge_ShouldReturnStatus200_WhenLoanIdIsValid() throws Exception {
        Loan mockLoan = createMockLoanEntity();

        when(loanService.getEntityById(TEST_LOAN_ID)).thenReturn(mockLoan);

        doNothing().when(chargeService).applyCharge(mockLoan);

        mockMvc.perform(post("/api/charges")
                        .param("loan_id", TEST_LOAN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk());

        verify(loanService).getEntityById(TEST_LOAN_ID);
        verify(chargeService).applyCharge(mockLoan);
        verifyNoMoreInteractions(loanService, chargeService);
    }

    @Test
    void applyCharge_ShouldHandleExceptions_IfLoanServiceFails() throws Exception {
        when(loanService.getEntityById(TEST_LOAN_ID))
                .thenThrow(new NotFoundException("Loan not found"));

        mockMvc.perform(post("/api/charges")
                        .param("loan_id", TEST_LOAN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound());

        verify(loanService).getEntityById(TEST_LOAN_ID);
        verify(chargeService, never()).applyCharge(any());
    }
}