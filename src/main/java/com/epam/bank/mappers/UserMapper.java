package com.epam.bank.mappers;

import com.epam.bank.dto.UserDTO;
import com.epam.bank.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDTO userDTO);

    UserDTO toUserDTO(User user);
}