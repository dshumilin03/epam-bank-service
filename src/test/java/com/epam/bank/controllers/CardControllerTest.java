package com.epam.bank.controllers;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.services.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private MockMvc mockMvc;

    private final UUID TEST_CARD_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_CARD_NUMBER = "4111222233334444";
    private final Long TEST_BANK_ACCOUNT_NUMBER = 987654321L;
    private final String NEW_PIN = "5555";

    private CardDTO createMockCardDTO() {
        return new CardDTO(
                TEST_CARD_ID,
                TEST_CARD_NUMBER,
                "Test User",
                LocalDate.now().plusYears(3),
                "123",
                CardStatus.ACTIVE,
                TEST_BANK_ACCOUNT_NUMBER,
                "1111"
        );
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cardController)
                .build();
    }

    @Test
    void getByNumber_ShouldReturnCard_AndStatus200() throws Exception {
        CardDTO mockCard = createMockCardDTO();
        when(cardService.getByNumber(TEST_CARD_NUMBER)).thenReturn(mockCard);

        mockMvc.perform(get("/api/cards")
                        .param("cardNumber", TEST_CARD_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(TEST_CARD_NUMBER));

        verify(cardService).getByNumber(TEST_CARD_NUMBER);
    }

    @Test
    void getByUser_ShouldReturnCardList_AndStatus200() throws Exception {
        List<CardDTO> mockCards = List.of(createMockCardDTO());
        when(cardService.getByUserId(TEST_USER_ID)).thenReturn(mockCards);

        mockMvc.perform(get("/api/cards/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_CARD_ID.toString()));

        verify(cardService).getByUserId(TEST_USER_ID);
    }

    @Test
    void create_ShouldReturnNewCard_AndStatus201() throws Exception {
        CardDTO mockCard = createMockCardDTO();
        when(cardService.create(TEST_USER_ID, TEST_BANK_ACCOUNT_NUMBER)).thenReturn(mockCard);

        mockMvc.perform(post("/api/cards/users/{userId}", TEST_USER_ID)
                        .param("card_type", TEST_BANK_ACCOUNT_NUMBER.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_CARD_ID.toString()));

        verify(cardService).create(TEST_USER_ID, TEST_BANK_ACCOUNT_NUMBER);
    }

    @Test
    void changePin_ShouldReturnStatus200() throws Exception {
        doNothing().when(cardService).changePin(TEST_CARD_ID, NEW_PIN);

        mockMvc.perform(patch("/api/cards/{cardId}", TEST_CARD_ID)
                        .param("action", "change-pin")
                        .content(NEW_PIN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cardService).changePin(TEST_CARD_ID, NEW_PIN);
    }

    @Test
    void block_ShouldReturnStatus200() throws Exception {
        doNothing().when(cardService).block(TEST_CARD_ID);

        mockMvc.perform(patch("/api/cards/{cardId}", TEST_CARD_ID)
                        .param("action", "block"))
                .andExpect(status().isOk());

        verify(cardService).block(TEST_CARD_ID);
    }

    @Test
    void renew_ShouldReturnRenewedCard_AndStatus201() throws Exception {
        CardDTO renewedCard = createMockCardDTO();
        when(cardService.renew(TEST_CARD_ID)).thenReturn(renewedCard);

        mockMvc.perform(put("/api/cards/{cardId}", TEST_CARD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_CARD_ID.toString()));

        verify(cardService).renew(TEST_CARD_ID);
    }
}