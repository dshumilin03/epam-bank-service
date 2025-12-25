package com.epam.bank.services;

import com.epam.bank.dtos.BankAccountDto;
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
import java.util.List;
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
            BankAccountDto bankAccountDto = new BankAccountDto(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDto expectedDto = new UserDto(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, false, Role.USER, bankAccountDto);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn(PASSPORT);
            when(userRepository.findByPassportId(PASSPORT)).thenReturn(Optional.empty());
            when(userMapper.toEntity(request)).thenReturn(userEntity);
            when(userRepository.save(userEntity)).thenReturn(savedUser);
            when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

            UserDto result = userService.register(request);

            assertThat(result).isEqualTo(expectedDto);
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("Should throw UserExistsException when email already exists")
        void shouldThrowUserExceptionWhenEmailExists() {

            RegisterRequest request = new RegisterRequest(FULL_NAME, EMAIL, PASSWORD, PASSPORT, Role.USER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(UserExistsException.class)
                    .hasMessage("User with this data already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UserExistsException when passport already exists")
        void shouldThrowExceptionWhenPassportExists() {
            RegisterRequest request = new RegisterRequest(FULL_NAME, EMAIL, PASSWORD, PASSPORT, Role.USER);
            when(passwordEncoder.encode(any())).thenReturn(PASSPORT);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByPassportId(PASSPORT)).thenReturn(Optional.of(new User()));

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(UserExistsException.class)
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
            UserCredentialsDto credentialsDto = new UserCredentialsDto(newEmail, "newPass", Role.MANAGER);

            User existingUser = new User();
            existingUser.setId(USER_ID);
            existingUser.setEmail(EMAIL);

            User updatedUser = new User();
            BankAccountDto bankAccountDto = new BankAccountDto(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDto expectedDto = new UserDto(USER_ID, FULL_NAME, PASSPORT, newEmail, "newPass", false, Role.MANAGER, bankAccountDto);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(newEmail)).thenReturn(false);
            when(userRepository.save(existingUser)).thenReturn(updatedUser);
            when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

            UserDto result = userService.changeCredentials(USER_ID, credentialsDto);

            assertThat(result).isEqualTo(expectedDto);
            assertThat(result.getEmail()).isEqualTo(newEmail);
            assertThat(result.getPassword()).isEqualTo("newPass");
            assertThat(result.getRole()).isEqualTo(Role.MANAGER);
        }

        @Test
        @DisplayName("Should successfully update when keeping the SAME email (even if existsByEmail returns true)")
        void shouldUpdateWhenEmailIsSameAsOld() {
            UserCredentialsDto credentialsDto = new UserCredentialsDto(EMAIL, "newPass", Role.USER);

            User existingUser = new User();
            existingUser.setId(USER_ID);
            existingUser.setEmail(EMAIL);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            when(userRepository.save(existingUser)).thenReturn(existingUser);
            when(userMapper.toDto(any())).thenReturn(mock(UserDto.class));

            userService.changeCredentials(USER_ID, credentialsDto);

            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user ID not found")
        void shouldThrowNotFoundWhenUserMissing() {
            UserCredentialsDto credentialsDto = new UserCredentialsDto("email", "pass", Role.USER);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changeCredentials(USER_ID, credentialsDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found by Id");
        }

        @Test
        @DisplayName("Should throw ExistsException when new email is taken by another user")
        void shouldThrowExistsWhenEmailTaken() {
            String takenEmail = "taken@example.com";
            UserCredentialsDto credentialsDto = new UserCredentialsDto(takenEmail, "pass", Role.USER);

            User existingUser = new User();
            existingUser.setId(USER_ID);
            existingUser.setEmail(EMAIL);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(takenEmail)).thenReturn(true);

            assertThatThrownBy(() -> userService.changeCredentials(USER_ID, credentialsDto))
                    .isInstanceOf(ExistsException.class)
                    .hasMessage("This email is attached to other User");
        }
    }

    @Nested
    @DisplayName("Tests for getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return UserDto when user exists")
        void shouldReturnUserById() {
            User user = new User();
            BankAccountDto bankAccountDto = new BankAccountDto(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDto expectedDto = new UserDto(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, false, Role.USER, bankAccountDto);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserDto result = userService.getById(USER_ID);

            assertThat(result).isEqualTo(expectedDto);
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

            BankAccountDto bankAccountDto = new BankAccountDto(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDto expectedDto = new UserDto(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, true, Role.USER, bankAccountDto);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserDto result = userService.setStatus(USER_ID, true);

            assertThat(user.getIsDisabled()).isTrue();
            assertThat(result).isEqualTo(expectedDto);
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
        @DisplayName("Should return UserDto by full name")
        void shouldReturnUserByName() {
            User user = new User();
            BankAccountDto bankAccountDto = new BankAccountDto(1L, BigDecimal.valueOf(123), USER_ID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            UserDto expectedDto = new UserDto(USER_ID, FULL_NAME, PASSPORT, EMAIL, PASSWORD, false, Role.USER, bankAccountDto);

            List<User> expectedList = new ArrayList<>();
            expectedList.add(user);
            when(userRepository.findByFullName(FULL_NAME)).thenReturn(expectedList);
            when(userMapper.toDto(user)).thenReturn(expectedDto);

            List<UserDto> result = userService.getByFullName(FULL_NAME);

            assertThat(result.getFirst()).isEqualTo(expectedDto);
        }
    }
}