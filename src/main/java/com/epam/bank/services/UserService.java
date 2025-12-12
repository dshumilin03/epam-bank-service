package com.epam.bank.services;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserDTO;

import java.util.UUID;

public interface UserService {
    UserDTO register(RegisterRequest registerUserDTO);

    UserDTO changeCredentials(UserDTO updateDTO);

    UserDTO getById(UUID uuid);

    UserDTO setStatus(UUID userId, boolean disabled);
}
