package com.epam.bank.controllers;

import com.epam.bank.dtos.UserCredentialsDto;
import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserDto;
import com.epam.bank.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserDto userDto = userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getById(@PathVariable @Valid UUID userId) {
        UserDto userDto = userService.getById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getById(@RequestParam(value = "full_name") String fullName) {
        List<UserDto> userDtoList = userService.getByFullName(fullName);
        return ResponseEntity.status(HttpStatus.OK).body(userDtoList);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> changeCredentials(@PathVariable UUID userId, @RequestBody @Valid UserCredentialsDto credentialsDto) {
        UserDto updated = userService.changeCredentials(userId, credentialsDto);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> changeDisabled(
            @PathVariable @Valid UUID userId,
            @RequestParam(name = "disabled") Boolean isDisabled
    ) {
        UserDto updated = userService.setStatus(userId, isDisabled);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }
}
