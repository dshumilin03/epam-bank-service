package com.epam.bank.mappers;

import com.epam.bank.dtos.CardDto;
import com.epam.bank.entities.Card;
import com.epam.bank.entities.CardStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CardMapperTest {

    private final CardMapper mapper = new CardMapperImpl();

    @Test
    void shouldMapEntityToDto() {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setCardNumber("1234-5678");
        card.setExpiresAt(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setCvv("123");
        card.setPinCode("0000");
        card.setOwnerName("John Doe");

        CardDto dto = mapper.toDto(card);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(card.getId());
        assertThat(dto.getCardNumber()).isEqualTo(card.getCardNumber());
        assertThat(dto.getExpiresAt()).isEqualTo(card.getExpiresAt());
        assertThat(dto.getOwnerName()).isEqualTo(card.getOwnerName());
    }

    @Test
    void shouldMapDtoToEntity() {
        UUID id = UUID.randomUUID();
        CardDto cardDto = new CardDto();
        cardDto.setId(id);
        cardDto.setCardNumber("4444-5555-6666-7777");
        cardDto.setOwnerName("Sidor Sidorov");
        cardDto.setExpiresAt(LocalDate.of(2030, 1, 1));
        cardDto.setCvv("777");
        cardDto.setPinCode("4321");
        cardDto.setStatus(CardStatus.ACTIVE);
        Card entity = mapper.toEntity(cardDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(cardDto.getId());
        assertThat(entity.getCardNumber()).isEqualTo(cardDto.getCardNumber());
        assertThat(entity.getExpiresAt()).isEqualTo(cardDto.getExpiresAt());
        assertThat(entity.getCvv()).isEqualTo(cardDto.getCvv());
        assertThat(entity.getPinCode()).isEqualTo(cardDto.getPinCode());
        assertThat(entity.getStatus()).isEqualTo(cardDto.getStatus());

        assertThat(entity.getOwnerName()).isEqualTo(cardDto.getOwnerName());

        assertThat(entity.getBankAccount()).isNull();
    }
}