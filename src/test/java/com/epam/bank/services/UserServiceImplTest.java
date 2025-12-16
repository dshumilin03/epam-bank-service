package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDTO;
import com.epam.bank.dtos.RegisterRequest;
import com.epam.bank.dtos.UserCredentialsDTO;
import com.epam.bank.dtos.UserDTO;
import com.epam.bank.entities.Role;
import com.epam.bank.entities.User;
import com.epam.bank.exceptions.ExistsException;
import com.epam.bank.exceptions.NotFoundException;
import com.epam.bank.mappers.UserMapper;
import com.epam.bank.repositories.UserRepository;
import com.epam.bank.security.EncryptionService;
import com.epam.bank.services.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private final UUID USER_ID = UUID.randomUUID();
    private final String EMAIL = "test@example.com";
    private final String PASSPORT = "1234567890";
    private final String FULL_NAME = "John Doe";
    private final String PASSWORD = "password";

    @Nested
    @DisplayName("Tests for register()")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void shouldRegisterUserSuccessfully() {
            RegisterRequest request = new RegisterRequest(FULL_NAME, EMAIL, PASSWORD, PASSPORT, Role.USER);
            User userEntity = new User();
            User savedUser = new User();
            savedUser.setId(USER_ID);
            BankAccountDTO bankAccountDTO = new BankAccountDTO(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDTO expectedDTO = new UserDTO(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, false, Role.USER, bankAccountDTO);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByPassportId(PASSPORT)).thenReturn(Optional.empty());
            when(userMapper.toEntity(request)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(savedUser);
            when(userMapper.toDTO(savedUser)).thenReturn(expectedDTO);

            UserDTO result = userService.register(request);

            assertThat(result).isEqualTo(expectedDTO);
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("Should throw ExistsException when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            RegisterRequest request = new RegisterRequest(FULL_NAME, EMAIL, PASSWORD, PASSPORT, Role.USER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(ExistsException.class)
                    .hasMessage("User with this data already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ExistsException when passport already exists")
        void shouldThrowExceptionWhenPassportExists() {
            RegisterRequest request = new RegisterRequest(FULL_NAME, EMAIL, PASSWORD, PASSPORT, Role.USER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByPassportId(PASSPORT)).thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(ExistsException.class)
                    .hasMessage("User with this data already exists");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for changeCredentials()")
    class ChangeCredentialsTests {

        @Test
        @DisplayName("Should successfully change credentials with new unique email")
        void shouldChangeCredentialsSuccessfully() {
            String newEmail = "new@example.com";
            UserCredentialsDTO credentialsDTO = new UserCredentialsDTO(newEmail, "newPass", Role.MANAGER);

            User existingUser = new User();
            existingUser.setId(USER_ID);
            existingUser.setEmail(EMAIL);

            User updatedUser = new User();
            BankAccountDTO bankAccountDTO = new BankAccountDTO(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDTO expectedDTO = new UserDTO(USER_ID, FULL_NAME, PASSPORT, newEmail, "newPass", false, Role.MANAGER, bankAccountDTO);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(newEmail)).thenReturn(false);
            when(userRepository.save(existingUser)).thenReturn(updatedUser);
            when(userMapper.toDTO(updatedUser)).thenReturn(expectedDTO);

            UserDTO result = userService.changeCredentials(USER_ID, credentialsDTO);

            assertThat(result).isEqualTo(expectedDTO);
            assertThat(result.getEmail()).isEqualTo(newEmail);
            assertThat(result.getPassword()).isEqualTo("newPass");
            assertThat(result.getRole()).isEqualTo(Role.MANAGER);
        }

        @Test
        @DisplayName("Should successfully update when keeping the SAME email (even if existsByEmail returns true)")
        void shouldUpdateWhenEmailIsSameAsOld() {
            UserCredentialsDTO credentialsDTO = new UserCredentialsDTO(EMAIL, "newPass", Role.USER);

            User existingUser = new User();
            existingUser.setId(USER_ID);
            existingUser.setEmail(EMAIL);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            when(userRepository.save(existingUser)).thenReturn(existingUser);
            when(userMapper.toDTO(any())).thenReturn(mock(UserDTO.class));

            userService.changeCredentials(USER_ID, credentialsDTO);

            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user ID not found")
        void shouldThrowNotFoundWhenUserMissing() {
            UserCredentialsDTO credentialsDTO = new UserCredentialsDTO("email", "pass", Role.USER);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changeCredentials(USER_ID, credentialsDTO))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found by Id");
        }

        @Test
        @DisplayName("Should throw ExistsException when new email is taken by another user")
        void shouldThrowExistsWhenEmailTaken() {
            String takenEmail = "taken@example.com";
            UserCredentialsDTO credentialsDTO = new UserCredentialsDTO(takenEmail, "pass", Role.USER);

            User existingUser = new User();
            existingUser.setId(USER_ID);
            existingUser.setEmail(EMAIL);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(takenEmail)).thenReturn(true);

            assertThatThrownBy(() -> userService.changeCredentials(USER_ID, credentialsDTO))
                    .isInstanceOf(ExistsException.class)
                    .hasMessage("This email is attached to other User");
        }
    }

    @Nested
    @DisplayName("Tests for getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return UserDTO when user exists")
        void shouldReturnUserById() {
            User user = new User();
            BankAccountDTO bankAccountDTO = new BankAccountDTO(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDTO expectedDTO = new UserDTO(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, false, Role.USER, bankAccountDTO);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(expectedDTO);

            UserDTO result = userService.getById(USER_ID);

            assertThat(result).isEqualTo(expectedDTO);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found by Id");
        }
    }

    @Nested
    @DisplayName("Tests for setStatus()")
    class SetStatusTests {

        @Test
        @DisplayName("Should update user disabled status")
        void shouldSetStatus() {
            User user = new User();
            user.setId(USER_ID);
            user.setIsDisabled(false);

            BankAccountDTO bankAccountDTO = new BankAccountDTO(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDTO expectedDTO = new UserDTO(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, true, Role.USER, bankAccountDTO);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(expectedDTO);

            UserDTO result = userService.setStatus(USER_ID, true);

            assertThat(user.getIsDisabled()).isTrue();
            assertThat(result).isEqualTo(expectedDTO);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user missing")
        void shouldThrowWhenUserMissing() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.setStatus(USER_ID, true))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found by Id");
        }
    }

    @Nested
    @DisplayName("Tests for getByFullName()")
    class GetByFullNameTests {

        @Test
        @DisplayName("Should return UserDTO by full name")
        void shouldReturnUserByName() {
            User user = new User();
            BankAccountDTO bankAccountDTO = new BankAccountDTO(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDTO expectedDTO = new UserDTO(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, false, Role.USER, bankAccountDTO);

            when(userRepository.findByFullName(FULL_NAME)).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(expectedDTO);

            UserDTO result = userService.getByFullName(FULL_NAME);

            assertThat(result).isEqualTo(expectedDTO);
        }

        @Test
        @DisplayName("Should throw NotFoundException when name not found")
        void shouldThrowWhenNameNotFound() {
            when(userRepository.findByFullName(FULL_NAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByFullName(FULL_NAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found by full name");
        }
    }
}