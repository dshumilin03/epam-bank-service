package com.epam.bank.controllers;

import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.UserDTO;
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

    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_FULL_NAME = "John Doe";
    private final String TEST_EMAIL = "test@example.com";

    private List<UserDTO> createMockUserDTO() {
        return List.of(new UserDTO(
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

    private UserDTO createUpdatedUserDTO(UserDTO baseDTO, String newEmail, Boolean newIsDisabled) {
        return new UserDTO(
                baseDTO.getId(),
                baseDTO.getFullName(),
                baseDTO.getPassportId(),
                newEmail != null ? newEmail : baseDTO.getEmail(),
                baseDTO.getPassword(),
                newIsDisabled != null ? newIsDisabled : baseDTO.getIsDisabled(),
                baseDTO.getRole(),
                baseDTO.getBankAccount()
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
        List<UserDTO> mockDTO = createMockUserDTO();
        when(userService.register(any(RegisterRequest.class))).thenReturn(mockDTO.getFirst());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void getById_ShouldReturnUser_AndStatus200() throws Exception {
        List<UserDTO> mockDTO = createMockUserDTO();
        when(userService.getById(TEST_USER_ID)).thenReturn(mockDTO.getFirst());

        mockMvc.perform(get("/api/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()));

        verify(userService).getById(TEST_USER_ID);
    }

    @Test
    void getByFullName_ShouldReturnUser_AndStatus200() throws Exception {
        List<UserDTO> mockDTO = createMockUserDTO();
        when(userService.getByFullName(TEST_FULL_NAME)).thenReturn(mockDTO);

        mockMvc.perform(get("/api/users")
                        .param("full_name", TEST_FULL_NAME))
                .andExpect(status().isOk());

        verify(userService).getByFullName(TEST_FULL_NAME);
    }

    @Test
    void changeCredentials_ShouldReturnUpdatedUser_AndStatus200() throws Exception {
        List<UserDTO> baseDTO = createMockUserDTO();
        UserCredentialsDTO credentialsDTO = new UserCredentialsDTO("new@example.com", "newpassword", Role.USER);

        UserDTO updatedDTO = createUpdatedUserDTO(baseDTO.getFirst(), credentialsDTO.email(), null);

        when(userService.changeCredentials(eq(TEST_USER_ID), any(UserCredentialsDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/users/{userId}", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentialsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userService).changeCredentials(eq(TEST_USER_ID), any(UserCredentialsDTO.class));
    }

    @Test
    void changeDisabled_ShouldReturnUpdatedUser_AndStatus200() throws Exception {
        List<UserDTO> baseDTO = createMockUserDTO();

        UserDTO disabledUserDTO = createUpdatedUserDTO(baseDTO.getFirst(), null, true);

        when(userService.setStatus(TEST_USER_ID, true)).thenReturn(disabledUserDTO);

        mockMvc.perform(patch("/api/users/{userId}", TEST_USER_ID)
                        .param("disabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDisabled").value(true));

        verify(userService).setStatus(TEST_USER_ID, true);
    }
}