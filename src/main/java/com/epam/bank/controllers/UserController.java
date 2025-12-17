package com.epam.bank.controllers;

import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserDTO;
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
    public ResponseEntity<UserDTO> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserDTO userDTO = userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getById(@PathVariable @Valid UUID userId) {
        UserDTO userDTO = userService.getById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getById(@RequestParam(value = "full_name") String fullName) {
        List<UserDTO> userDTOList = userService.getByFullName(fullName);
        return ResponseEntity.status(HttpStatus.OK).body(userDTOList);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> changeCredentials(@PathVariable UUID userId, @RequestBody @Valid UserCredentialsDTO credentialsDTO) {
        UserDTO updated = userService.changeCredentials(userId, credentialsDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDTO> changeDisabled(
            @PathVariable @Valid UUID userId,
            @RequestParam(name = "disabled") Boolean isDisabled
    ) {
        UserDTO updated = userService.setStatus(userId, isDisabled);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }
}
