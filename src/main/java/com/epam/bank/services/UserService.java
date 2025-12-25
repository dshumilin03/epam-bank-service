package com.epam.bank.services;

import com.epam.bank.dtos.UserCredentialsDto;
import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto register(RegisterRequest registerUserDto);

    UserDto changeCredentials(UUID userId, UserCredentialsDto userCredentialsDto);

    UserDto getById(UUID uuid);

    UserDto setStatus(UUID userId, Boolean disabled);

    List<UserDto> getByFullName(String fullName);

    UserDto getByEmail(String email);
}
