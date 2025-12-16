package com.epam.bank.services;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Card;
import com.epam.bank.entities.CardStatus;
import com.epam.bank.entities.User;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.CardMapper;
import com.epam.bank.repositories.BankAccountRepository;
import com.epam.bank.repositories.CardRepository;
import com.epam.bank.security.EncryptionService;
import com.epam.bank.services.impl.CardServiceImpl;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Captor
    private ArgumentCaptor<Card> cardCaptor;

    private final UUID CARD_ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID();
    private final Long BANK_ACCOUNT_NUMBER = 100L;
    private final String CARD_NUMBER = "4043100001";
    private final String FULL_NAME = "John Doe";
    private final String CVV = "123";
    private final String PIN_CODE = "1234";

    private Card card;
    private CardDTO cardDTO;
    private BankAccount bankAccount;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setFullName(FULL_NAME);

        bankAccount = new BankAccount();
        bankAccount.setBankAccountNumber(BANK_ACCOUNT_NUMBER);
        bankAccount.setUser(user);

        card = new Card();
        card.setId(CARD_ID);
        card.setCardNumber(CARD_NUMBER);
        card.setOwnerName(FULL_NAME);
        card.setExpiresAt(LocalDate.now().plusYears(5));
        card.setCvv(CVV);
        card.setStatus(CardStatus.ACTIVE);
        card.setBankAccount(bankAccount);
        card.setPinCode(PIN_CODE);

        cardDTO = new CardDTO();
        cardDTO.setId(CARD_ID);
        cardDTO.setCardNumber(CARD_NUMBER);
        cardDTO.setOwnerName(FULL_NAME);
        cardDTO.setExpiresAt(LocalDate.now().plusYears(5));
        cardDTO.setCvv(CVV);
    }

    @Nested
    @DisplayName("Tests for getByNumber()")
    class GetByNumberTests {

        @Test
        @DisplayName("Should return card by card number")
        void shouldReturnCardByNumber() {
            when(cardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.of(card));
            when(cardMapper.toDTO(card)).thenReturn(cardDTO);

            CardDTO result = cardService.getByNumber(CARD_NUMBER);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(cardDTO);
            assertThat(result.getCardNumber()).isEqualTo(CARD_NUMBER);
            verify(cardRepository).findByCardNumber(CARD_NUMBER);
            verify(cardMapper).toDTO(card);
        }

        @Test
        @DisplayName("Should throw NotFoundException when card not found by number")
        void shouldThrowNotFoundExceptionWhenCardNotFoundByNumber() {
            when(cardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardService.getByNumber(CARD_NUMBER))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Card not found by card number");

            verify(cardMapper, never()).toDTO(any());
        }
    }

    @Nested
    @DisplayName("Tests for getByUserId()")
    class GetByUserIdTests {

        @Test
        @DisplayName("Should return list of user cards")
        void shouldReturnUserCards() {
            List<Card> cards = List.of(card);
            when(cardRepository.findByUserId(USER_ID)).thenReturn(cards);
            when(cardMapper.toDTO(card)).thenReturn(cardDTO);

            List<CardDTO> result = cardService.getByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(cardDTO);
            verify(cardRepository).findByUserId(USER_ID);
            verify(cardMapper).toDTO(card);
        }

        @Test
        @DisplayName("Should return empty list when user has no cards")
        void shouldReturnEmptyListWhenNoCards() {
            when(cardRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            List<CardDTO> result = cardService.getByUserId(USER_ID);

            assertThat(result).isEmpty();
            verify(cardRepository).findByUserId(USER_ID);
            verify(cardMapper, never()).toDTO(any());
        }

        @Test
        @DisplayName("Should return list with multiple cards")
        void shouldReturnMultipleCards() {
            Card card2 = new Card();
            card2.setId(UUID.randomUUID());
            card2.setCardNumber("4043100002");

            CardDTO cardDTO2 = new CardDTO();
            cardDTO2.setId(card2.getId());
            cardDTO2.setCardNumber("4043100002");

            List<Card> cards = List.of(card, card2);
            when(cardRepository.findByUserId(USER_ID)).thenReturn(cards);
            when(cardMapper.toDTO(card)).thenReturn(cardDTO);
            when(cardMapper.toDTO(card2)).thenReturn(cardDTO2);

            List<CardDTO> result = cardService.getByUserId(USER_ID);

            assertThat(result).hasSize(2);
            verify(cardMapper, times(2)).toDTO(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Tests for create()")
    class CreateTests {

        @BeforeEach
        void setUpCreate() {
            when(bankAccountRepository.findById(BANK_ACCOUNT_NUMBER))
                    .thenReturn(Optional.of(bankAccount));

            lenient().when(encryptionService.encrypt(anyString()))
                    .thenAnswer(inv -> "ENCRYPTED_" + inv.getArgument(0));

            lenient().when(cardRepository.save(any(Card.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            lenient().when(cardMapper.toDTO(any(Card.class))).thenAnswer(inv -> {
                Card c = inv.getArgument(0);
                CardDTO dto = new CardDTO();
                dto.setCardNumber(c.getCardNumber());
                dto.setExpiresAt(c.getExpiresAt());
                dto.setOwnerName(c.getOwnerName());
                return dto;
            });
        }

        @Test
        @DisplayName("Should create card successfully (Overall Check)")
        void shouldCreateCardSuccessfully() {
            CardDTO result = cardService.create(USER_ID, BANK_ACCOUNT_NUMBER);

            assertThat(result).isNotNull();
            verify(bankAccountRepository).findById(BANK_ACCOUNT_NUMBER);
            verify(cardRepository).save(any(Card.class));
            verify(cardMapper).toDTO(any(Card.class));
        }

        @Test
        @DisplayName("Should generate card number with correct format")
        void shouldGenerateCardNumberWithCorrectFormat() {
            CardDTO result = cardService.create(USER_ID, BANK_ACCOUNT_NUMBER);

            assertThat(result.getCardNumber()).startsWith("4043");
            assertThat(result.getCardNumber()).contains(BANK_ACCOUNT_NUMBER.toString());
        }

        @Test
        @DisplayName("Should set card expiration to 5 years from now")
        void shouldSetExpirationTo5Years() {
            CardDTO result = cardService.create(USER_ID, BANK_ACCOUNT_NUMBER);

            assertThat(result.getExpiresAt())
                    .isCloseTo(LocalDate.now().plusYears(5), within(1, ChronoUnit.DAYS));
        }

        @Test
        @DisplayName("Should set card status to ACTIVE")
        void shouldSetCardStatusToActive() {
            cardService.create(USER_ID, BANK_ACCOUNT_NUMBER);

            verify(cardRepository).save(cardCaptor.capture());
            Card savedCard = cardCaptor.getValue();

            assertThat(savedCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should set owner name from bank account user")
        void shouldSetOwnerNameFromBankAccountUser() {
            CardDTO result = cardService.create(USER_ID, BANK_ACCOUNT_NUMBER);

            assertThat(result.getOwnerName()).isEqualTo(FULL_NAME);

            verify(cardRepository).save(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getOwnerName()).isEqualTo(FULL_NAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when bank account not found")
        void shouldThrowNotFoundWhenBankAccountNotFound() {
            when(bankAccountRepository.findById(BANK_ACCOUNT_NUMBER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardService.create(USER_ID, BANK_ACCOUNT_NUMBER))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("BankAccount not found by number");

            verify(cardRepository, never()).save(any());
            verify(encryptionService, never()).encrypt(anyString());
        }
    }

    @Nested
    @DisplayName("Tests for changePin()")
    class ChangePinTests {

        @Test
        @DisplayName("Should change card PIN successfully")
        void shouldChangePinSuccessfully() {
            String newPin = "5678";
            card = new Card();
            card.setPinCode("123");
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(encryptionService.encrypt(newPin)).thenReturn(newPin);

            cardService.changePin(CARD_ID, newPin);

            verify(cardRepository).findById(CARD_ID);

            assertThat(card.getPinCode()).isEqualTo(newPin);
        }

        @Test
        @DisplayName("Should throw NotFoundException when card not found")
        void shouldThrowNotFoundWhenCardNotFound() {
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardService.changePin(CARD_ID, "5678"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Card not found by Id");

            verify(cardRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for renew()")
    class RenewTests {

        @Test
        @DisplayName("Should renew card successfully")
        void shouldRenewCardSuccessfully() {
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
            when(cardMapper.toDTO(card)).thenReturn(cardDTO);

            CardDTO result = cardService.renew(CARD_ID);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(cardDTO);
            verify(cardRepository).findById(CARD_ID);
            verify(cardRepository).save(cardCaptor.capture());

            Card savedCard = cardCaptor.getValue();
            assertThat(savedCard.getExpiresAt()).isCloseTo(LocalDate.now().plusYears(5), within(1, ChronoUnit.DAYS));
        }

        @Test
        @DisplayName("Should throw NotFoundException when card not found")
        void shouldThrowNotFoundWhenCardNotFound() {
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardService.renew(CARD_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Card not found by Id");

            verify(cardRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for block()")
    class BlockTests {

        @Test
        @DisplayName("Should block card successfully")
        void shouldBlockCardSuccessfully() {
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

            cardService.block(CARD_ID);

            verify(cardRepository).findById(CARD_ID);
            verify(cardRepository).save(cardCaptor.capture());

            Card savedCard = cardCaptor.getValue();
            assertThat(savedCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should throw NotFoundException when card not found")
        void shouldThrowNotFoundWhenCardNotFound() {
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardService.block(CARD_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Card not found by Id");

            verify(cardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should change status from ACTIVE to BLOCKED")
        void shouldChangeStatusFromActiveToBlocked() {
            card.setStatus(CardStatus.ACTIVE);
            when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

            cardService.block(CARD_ID);

            assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        }
    }
}