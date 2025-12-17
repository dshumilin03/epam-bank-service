package com.epam.bank.services.impl;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.Role;
import com.epam.bank.entities.User;
import com.epam.bank.exceptions.ExistsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.exceptions.UserExistsException;
import com.epam.bank.mappers.UserMapper;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.security.EncryptionService;
import com.epam.bank.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    public UserDTO register(RegisterRequest registerUserDTO) {
        if (userRepository.findByEmail(registerUserDTO.email()).isPresent() ||
                userRepository.findByPassportId(encryptionService.encrypt(registerUserDTO.passportId())).isPresent()) {
            throw new UserExistsException("User with this data already exists");
        }
        User newUser = userMapper.toEntity(registerUserDTO);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setPassportId(encryptionService.encrypt(newUser.getPassportId()));
        newUser.setIsDisabled(false);
        newUser.setRole(Role.USER);
        return userMapper.toDTO(userRepository.save(newUser));
    }

    public UserDTO changeCredentials(UUID id, UserCredentialsDTO userCredentialsDTO) {
        User old = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found by Id"));

        Boolean existsByEmail = userRepository.existsByEmail(userCredentialsDTO.email());
        Boolean notSameEmail = !userCredentialsDTO.email().equals(old.getEmail());
        if (existsByEmail && notSameEmail) {
            throw new ExistsException("This email is attached to other User");
        }
        old.setPassword(passwordEncoder.encode(userCredentialsDTO.password()));
        old.setRole(userCredentialsDTO.role());
        old.setEmail(userCredentialsDTO.email());
        return userMapper.toDTO(userRepository.save(old));
    }

    public UserDTO getById(UUID uuid) {
        return userMapper.toDTO(userRepository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("User not found by Id")));
    }

    public UserDTO setStatus(UUID userId, Boolean disabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found by Id"));

        user.setIsDisabled(disabled);

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public List<UserDTO> getByFullName(String fullName) {
        List<UserDTO> userList = new ArrayList<>();

        userRepository.findByFullName((fullName)).forEach(user -> {
            userList.add(userMapper.toDTO(user));
        });

        return userList;
    }

    @Override
    public UserDTO getByEmail(String email) {
        return userMapper.toDTO(userRepository.findByEmail((email))
                .orElseThrow(() -> new NotFoundException("User not found by email")));
    }
}