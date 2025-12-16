package com.epam.bank.mappers;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BankAccountMapper.class})
public interface UserMapper {
    User toEntity(UserDTO userDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDisabled", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    User toEntity(RegisterRequest registerRequest);

    UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "passportId", ignore = true)
    @Mapping(target = "isDisabled", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    User toEntity(UserCredentialsDTO userCredentialsDTO);
}