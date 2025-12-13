package com.epam.bank.mappers;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDTO userDTO);

    User toUser(RegisterRequest registerRequest);

    UserDTO toUserDTO(User user);

    User toUser(UserCredentialsDTO userCredentialsDTO);
}