package com.epam.bank.controllers;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDto;
import com.epam.bank.dtos.UserDto;
import com.epam.bank.entities.Role;
import com.epam.bank.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_FULL_NAME = "John Doe";
    private static final String TEST_EMAIL = "test@example.com";

    private List<UserDto> createMockUserDto() {
        return List.of(new UserDto(
                TEST_USER_ID,
                TEST_FULL_NAME,
                "AB123456",
                TEST_EMAIL,
                "hashedpassword",
                false,
                Role.USER,
                null
        ));
    }

    private UserDto createUpdatedUserDto(UserDto baseDto, String newEmail, Boolean newIsDisabled) {
        return new UserDto(
                baseDto.getId(),
                baseDto.getFullName(),
                baseDto.getPassportId(),
                newEmail != null ? newEmail : baseDto.getEmail(),
                baseDto.getPassword(),
                newIsDisabled != null ? newIsDisabled : baseDto.getIsDisabled(),
                baseDto.getRole(),
                baseDto.getBankAccount()
        );
    }


    private RegisterRequest createMockRegisterRequest() {
        return new RegisterRequest(
                TEST_FULL_NAME,
                TEST_EMAIL,
                "password123",
                "AB123456",
                Role.USER
        );
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    void register_ShouldReturnNewUser_AndStatus201() throws Exception {
        RegisterRequest request = createMockRegisterRequest();
        List<UserDto> mockDto = createMockUserDto();
        when(userService.register(any(RegisterRequest.class))).thenReturn(mockDto.getFirst());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void getById_ShouldReturnUser_AndStatus200() throws Exception {
        List<UserDto> mockDto = createMockUserDto();
        when(userService.getById(TEST_USER_ID)).thenReturn(mockDto.getFirst());

        mockMvc.perform(get("/api/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()));

        verify(userService).getById(TEST_USER_ID);
    }

    @Test
    void getByFullName_ShouldReturnUser_AndStatus200() throws Exception {
        List<UserDto> mockDto = createMockUserDto();
        when(userService.getByFullName(TEST_FULL_NAME)).thenReturn(mockDto);

        mockMvc.perform(get("/api/users")
                        .param("full_name", TEST_FULL_NAME))
                .andExpect(status().isOk());

        verify(userService).getByFullName(TEST_FULL_NAME);
    }

    @Test
    void changeCredentials_ShouldReturnUpdatedUser_AndStatus200() throws Exception {
        List<UserDto> baseDto = createMockUserDto();
        UserCredentialsDto credentialsDto = new UserCredentialsDto("new@example.com", "newpassword", Role.USER);

        UserDto updatedDto = createUpdatedUserDto(baseDto.getFirst(), credentialsDto.email(), null);

        when(userService.changeCredentials(eq(TEST_USER_ID), any(UserCredentialsDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/users/{userId}", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentialsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userService).changeCredentials(eq(TEST_USER_ID), any(UserCredentialsDto.class));
    }

    @Test
    void changeDisabled_ShouldReturnUpdatedUser_AndStatus200() throws Exception {
        List<UserDto> baseDto = createMockUserDto();

        UserDto disabledUserDto = createUpdatedUserDto(baseDto.getFirst(), null, true);

        when(userService.setStatus(TEST_USER_ID, true)).thenReturn(disabledUserDto);

        mockMvc.perform(patch("/api/users/{userId}", TEST_USER_ID)
                        .param("disabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDisabled").value(true));

        verify(userService).setStatus(TEST_USER_ID, true);
    }
}