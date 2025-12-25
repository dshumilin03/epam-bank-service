package com.epam.bank.mappers;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDto;
import com.epam.bank.dtos.UserDto;
import com.epam.bank.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BankAccountMapper.class})
public interface UserMapper {
    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDisabled", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    User toEntity(RegisterRequest registerRequest);

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "passportId", ignore = true)
    @Mapping(target = "isDisabled", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    User toEntity(UserCredentialsDto userCredentialsDto);
}