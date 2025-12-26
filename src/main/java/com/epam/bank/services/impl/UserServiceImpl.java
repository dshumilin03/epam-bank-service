package com.epam.bank.services.impl;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDto;
import com.epam.bank.dtos.UserDto;
import com.epam.bank.entities.Role;
import com.epam.bank.entities.User;
import com.epam.bank.exceptions.ExistsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.exceptions.UserExistsException;
import com.epam.bank.mappers.UserMapper;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String NOT_FOUND_USER_BY_ID = "User not found by Id";

    @Transactional
    public UserDto register(RegisterRequest registerUserDto) {
        if (userRepository.findByEmail(
                registerUserDto.email()).isPresent() ||
                userRepository.findByPassportId(
                        passwordEncoder.encode(
                                registerUserDto.passportId())).isPresent()) {

            throw new UserExistsException("User with this data already exists");
        }

        User newUser = userMapper.toEntity(registerUserDto);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setPassportId(passwordEncoder.encode(newUser.getPassportId()));
        newUser.setIsDisabled(false);
        // todo add Role to registerUserDto
        newUser.setRole(Role.USER);
        return userMapper.toDto(userRepository.save(newUser));
    }

    @Transactional
    public UserDto changeCredentials(UUID id, UserCredentialsDto userCredentialsDto) {

        User entity = getOrThrow(id);

        Boolean existsByEmail = userRepository.existsByEmail(userCredentialsDto.email());
        Boolean notSameEmail = !userCredentialsDto.email().equals(entity.getEmail());
        if (existsByEmail && notSameEmail) {
            throw new ExistsException("This email is attached to other User");
        }
        entity.setPassword(passwordEncoder.encode(userCredentialsDto.password()));
        entity.setRole(userCredentialsDto.role());
        entity.setEmail(userCredentialsDto.email());
        return userMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        return userMapper.toDto(getOrThrow(id));
    }

    @Transactional
    public UserDto setStatus(UUID userId, Boolean disabled) {
        User user = getOrThrow(userId);

        user.setIsDisabled(disabled);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getByFullName(String fullName) {

        return userRepository.findByFullName(fullName).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getByEmail(String email) {
        return userMapper.toDto(userRepository.findByEmail((email))
                .orElseThrow(() -> new NotFoundException("User not found by email")));
    }

    private User getOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER_BY_ID));
    }
}