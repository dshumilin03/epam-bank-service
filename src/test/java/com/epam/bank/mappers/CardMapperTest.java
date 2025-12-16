package com.epam.bank.mappers;

import com.epam.bank.dtos.CardDTO;
import com.epam.bank.entities.Card;
import com.epam.bank.entities.CardStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CardMapperTest {

    private final CardMapper mapper = new CardMapperImpl();

    @Test
    void shouldMapEntityToDTO() {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setCardNumber("1234-5678");
        card.setExpiresAt(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setCvv("123");
        card.setPinCode("0000");
        card.setOwnerName("John Doe");

        CardDTO dto = mapper.toDTO(card);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(card.getId());
        assertThat(dto.getCardNumber()).isEqualTo(card.getCardNumber());
        assertThat(dto.getExpiresAt()).isEqualTo(card.getExpiresAt());
        assertThat(dto.getOwnerName()).isEqualTo(card.getOwnerName());
    }

    @Test
    void shouldMapDTOToEntity() {
        UUID id = UUID.randomUUID();
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(id);
        cardDTO.setCardNumber("4444-5555-6666-7777");
        cardDTO.setOwnerName("Sidor Sidorov");
        cardDTO.setExpiresAt(LocalDate.of(2030, 1, 1));
        cardDTO.setCvv("777");
        cardDTO.setPinCode("4321");
        cardDTO.setStatus(CardStatus.ACTIVE);
        Card entity = mapper.toEntity(cardDTO);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(cardDTO.getId());
        assertThat(entity.getCardNumber()).isEqualTo(cardDTO.getCardNumber());
        assertThat(entity.getExpiresAt()).isEqualTo(cardDTO.getExpiresAt());
        assertThat(entity.getCvv()).isEqualTo(cardDTO.getCvv());
        assertThat(entity.getPinCode()).isEqualTo(cardDTO.getPinCode());
        assertThat(entity.getStatus()).isEqualTo(cardDTO.getStatus());

        assertThat(entity.getOwnerName()).isEqualTo(cardDTO.getOwnerName());

        assertThat(entity.getBankAccount()).isNull();
    }
}