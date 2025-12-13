package com.epam.bank.services.impl;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.User;
import com.epam.bank.exceptions.ExistsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.UserMapper;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public UserDTO register(RegisterRequest registerUserDTO) {
        if (userRepository.getUserByEmail(registerUserDTO.email()).isPresent() ||
                userRepository.getUserByPassportId(registerUserDTO.passportId()).isPresent()) {
            throw new ExistsException("User with this data already exists");
        }
        User newUser = userMapper.toUser(registerUserDTO);
        newUser.setIsDisabled(false);
        return userMapper.toUserDTO(userRepository.save(newUser));
    }

    public UserDTO changeCredentials(UUID id, UserCredentialsDTO userCredentialsDTO) {
        User old = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found by Id"));

        boolean existsByEmail = userRepository.existsByEmail(userCredentialsDTO.email());
        boolean notSameEmail = !userCredentialsDTO.email().equals(old.getEmail());
        if (existsByEmail && notSameEmail) {
            throw new ExistsException("This email is attached to other User");
        }

        old.setRole(userCredentialsDTO.role());
        old.setPassword(userCredentialsDTO.password());
        old.setEmail(userCredentialsDTO.email());
        return userMapper.toUserDTO(userRepository.save(old));
    }

    public UserDTO getById(UUID uuid) {
        return userMapper.toUserDTO(userRepository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("User not found by Id")));
    }

    public UserDTO setStatus(UUID userId, boolean disabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found by Id"));

        user.setIsDisabled(disabled);

        return userMapper.toUserDTO(userRepository.save(user));
    }
}