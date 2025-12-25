package com.epam.bank.mappers;

import com.epam.bank.dtos.BankAccountDto;
import com.epam.bank.dtos.UserDto;
import com.epam.bank.entities.BankAccount;
import com.epam.bank.entities.Role;
import com.epam.bank.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Test
    void shouldMapUserEntityToUserDto() {

        when(bankAccountMapper.toDto(any(BankAccount.class)))
                .thenAnswer(invocation -> {
                    BankAccount source = invocation.getArgument(0);
                    return new BankAccountDto(
                            source.getBankAccountNumber(),
                            null,
                            null, null, null, null
                    );
                });

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Ivan Ivanov");
        user.setEmail("ivan@test.com");
        user.setRole(Role.USER);
        user.setIsDisabled(false);
        user.setPassportId("AB123456");
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankAccountNumber(1L);
        user.setBankAccount(bankAccount);

        UserDto dto = mapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getFullName()).isEqualTo(user.getFullName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getRole()).isEqualTo(user.getRole());
        assertThat(dto.getIsDisabled()).isEqualTo(user.getIsDisabled());
        assertThat(dto.getBankAccount().bankAccountNumber()).isEqualTo(bankAccount.getBankAccountNumber());
    }

    @Test
    void shouldMapUserDtoToUserEntity() {
        BankAccountDto bankAccount = new BankAccountDto(1L, null, null, null, null, null);
        UserDto dto = new UserDto(
                UUID.randomUUID(),
                "Petr Petrov",
                "CD654321",
                "petr@test.com",
                "pass123",
                true,
                Role.MANAGER,
                bankAccount

        );

        when(bankAccountMapper.toEntity(any(BankAccountDto.class)))
                .thenAnswer(invocationOnMock -> {
                    BankAccountDto source = invocationOnMock.getArgument(0);
                    BankAccount ba = new BankAccount();
                    ba.setBankAccountNumber(source.bankAccountNumber());
                    return ba;
                });

        User entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getFullName()).isEqualTo(dto.getFullName());
        assertThat(entity.getRole()).isEqualTo(dto.getRole());
        assertThat(entity.getIsDisabled()).isEqualTo(dto.getIsDisabled());
        assertThat(entity.getBankAccount().getBankAccountNumber()).isEqualTo(dto.getBankAccount().bankAccountNumber());
    }
}