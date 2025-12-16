package com.epam.bank.services;

import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserDTO;

import java.util.UUID;

public interface UserService {
    UserDTO register(RegisterRequest registerUserDTO);

    UserDTO changeCredentials(UUID userId, UserCredentialsDTO userCredentialsDTO);

    UserDTO getById(UUID uuid);

    UserDTO setStatus(UUID userId, boolean disabled);

    UserDTO getByFullName(String fullName);

    UserDTO getByEmail(String email);
}
