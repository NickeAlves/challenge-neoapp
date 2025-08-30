package com.neoapp.service;

import com.neoapp.dto.request.LoginRequestDTO;
import com.neoapp.dto.request.RegisterUserDTO;
import com.neoapp.dto.request.UpdateRequestUserDTO;
import com.neoapp.dto.response.*;
import com.neoapp.entity.User;
import com.neoapp.repository.UserRepository;
import com.neoapp.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = createMockUser();
    }

    private User createMockUser() {
        User user = new User();
        user.generateId();
        user.setName("João");
        user.setLastName("Silva");
        user.setCpf("12345678901");
        user.setEmail("joao@email.com");
        user.setPassword("encodedPassword");
        user.setDateOfBirth(LocalDate.of(1990, 5, 15));
        return user;
    }

    @Nested
    @DisplayName("listUsersPaginated Tests")
    class ListUsersPaginatedTests {

        @Test
        @DisplayName("Should return paginated users successfully")
        void shouldReturnPaginatedUsersSuccessfully() {
            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.listUsersPaginated(0, 10, "name", "asc");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().success());
            assertEquals("Users retrieved successfully", response.getBody().message());
            assertEquals(1, response.getBody().content().size());
        }

        @Test
        @DisplayName("Should return error when no users found")
        void shouldReturnErrorWhenNoUsersFound() {
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
            when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.listUsersPaginated(0, 10, "name", "asc");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().success());
            assertEquals("No users found", response.getBody().message());
        }

        @Test
        @DisplayName("Should handle invalid pagination parameters")
        void shouldHandleInvalidPaginationParameters() {
            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.listUsersPaginated(-1, 0, "", "invalid");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userRepository).findAll(PageRequest.of(0, 10,
                    org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.ASC, "name")));
        }

        @Test
        @DisplayName("Should return internal server error on exception")
        void shouldReturnInternalServerErrorOnException() {
            when(userRepository.findAll(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.listUsersPaginated(0, 10, "name", "asc");

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().success());
            assertEquals("Internal server error occurred while retrieving users",
                    response.getBody().message());
        }
    }

    @Nested
    @DisplayName("findUserById Tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("Should find user by ID successfully")
        void shouldFindUserByIdSuccessfully() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            ResponseEntity<ResponseUserDTO> response = userService.findUserById(userId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().success());
            assertEquals("User found successfully", response.getBody().message());
            assertNotNull(response.getBody().user());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ResponseEntity<ResponseUserDTO> response = userService.findUserById(userId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().success());
            assertEquals("User not found with the provided ID", response.getBody().message());
        }

        @Test
        @DisplayName("Should return internal server error on exception")
        void shouldReturnInternalServerErrorOnException() {
            when(userRepository.findById(userId))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<ResponseUserDTO> response = userService.findUserById(userId);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("findUserByCpf Tests")
    class FindUserByCpfTests {

        @Test
        @DisplayName("Should find user by CPF successfully")
        void shouldFindUserByCpfSuccessfully() {
            String cpf = "12345678901";
            when(userRepository.findByCpf(cpf)).thenReturn(Optional.of(mockUser));

            ResponseEntity<ResponseUserDTO> response = userService.findUserByCpf(cpf);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().success());
        }

        @Test
        @DisplayName("Should return 400 for invalid CPF format")
        void shouldReturn400ForInvalidCpfFormat() {
            ResponseEntity<ResponseUserDTO> response = userService.findUserByCpf("123");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse(response.getBody().success());
            assertEquals("Invalid CPF format. Use only numbers, e.g. 00000000000",
                    response.getBody().message());
        }

        @Test
        @DisplayName("Should return 404 when user not found by CPF")
        void shouldReturn404WhenUserNotFoundByCpf() {
            String cpf = "12345678901";
            when(userRepository.findByCpf(cpf)).thenReturn(Optional.empty());

            ResponseEntity<ResponseUserDTO> response = userService.findUserByCpf(cpf);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("findUserByEmail Tests")
    class FindUserByEmailTests {

        @Test
        @DisplayName("Should find user by email successfully")
        void shouldFindUserByEmailSuccessfully() {
            String email = "joao@email.com";
            when(userRepository.findByEmail(email.toLowerCase()))
                    .thenReturn(Optional.of(mockUser));

            ResponseEntity<ResponseUserDTO> response = userService.findUserByEmail(email);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().success());
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() {
            ResponseEntity<ResponseUserDTO> response =
                    userService.findUserByEmail("invalid-email");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse(response.getBody().success());
        }

        @Test
        @DisplayName("Should normalize email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            String upperEmail = "JOAO@EMAIL.COM";
            when(userRepository.findByEmail("joao@email.com"))
                    .thenReturn(Optional.of(mockUser));

            ResponseEntity<ResponseUserDTO> response = userService.findUserByEmail(upperEmail);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userRepository).findByEmail("joao@email.com");
        }
    }

    @Nested
    @DisplayName("searchUsers Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users successfully")
        void shouldSearchUsersSuccessfully() {
            String searchTerm = "João";
            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser));
            when(userRepository.searchByNameOrLastName(eq(searchTerm), any(Pageable.class)))
                    .thenReturn(userPage);

            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.searchUsers(searchTerm, 0, 10, "name", "asc");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().success());
            assertTrue(response.getBody().message().contains("Found 1 users matching"));
        }

        @Test
        @DisplayName("Should return 400 for empty search term")
        void shouldReturn400ForEmptySearchTerm() {
            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.searchUsers("", 0, 10, "name", "asc");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Search term is required", response.getBody().message());
        }

        @Test
        @DisplayName("Should return error when no users found")
        void shouldReturnErrorWhenNoUsersFound() {
            String searchTerm = "NonExistent";
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
            when(userRepository.searchByNameOrLastName(eq(searchTerm), any(Pageable.class)))
                    .thenReturn(emptyPage);

            ResponseEntity<PaginatedResponseDTO<DataUserDTO>> response =
                    userService.searchUsers(searchTerm, 0, 10, "name", "asc");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().success());
            assertEquals("No users found with the provided search term",
                    response.getBody().message());
        }
    }

    @Nested
    @DisplayName("register Tests")
    class RegisterTests {

        private RegisterUserDTO createValidRegisterDTO() {
            return new RegisterUserDTO(
                    "João",
                    "Silva",
                    "12345678901",
                    LocalDate.of(1990, 5, 15),
                    "joao@email.com",
                    "password123"
            );
        }

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            RegisterUserDTO dto = createValidRegisterDTO();
            String token = "generated-token";

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(tokenService.generateToken(any(User.class))).thenReturn(token);

            ResponseEntity<RegisterResponseDTO> response = userService.register(dto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.getBody().success());
            assertEquals("User registered successfully", response.getBody().message());
            assertEquals(token, response.getBody().token());
            assertNotNull(response.getBody().user());
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() {
            RegisterUserDTO dto = new RegisterUserDTO(
                    "João", "Silva", "12345678901", LocalDate.of(1990, 5, 15),
                    "invalid-email", "password123"
            );

            ResponseEntity<RegisterResponseDTO> response = userService.register(dto);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid email format", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 400 when email already exists")
        void shouldReturn400WhenEmailAlreadyExists() {
            RegisterUserDTO dto = createValidRegisterDTO();
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            ResponseEntity<RegisterResponseDTO> response = userService.register(dto);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Email already registered", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 400 when CPF already exists")
        void shouldReturn400WhenCpfAlreadyExists() {
            RegisterUserDTO dto = createValidRegisterDTO();
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByCpf(anyString())).thenReturn(true);

            ResponseEntity<RegisterResponseDTO> response = userService.register(dto);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("CPF already registered", response.getBody().message());
        }

        @Test
        @DisplayName("Should capitalize names during registration")
        void shouldCapitalizeNamesDuringRegistration() {
            RegisterUserDTO dto = new RegisterUserDTO(
                    "joão da silva", "dos santos", "12345678901",
                    LocalDate.of(1990, 5, 15), "joao@email.com", "password123"
            );

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(tokenService.generateToken(any(User.class))).thenReturn("token");

            userService.register(dto);

            verify(userRepository).save(argThat(user ->
                    "João Da Silva".equals(user.getName()) &&
                            "Dos Santos".equals(user.getLastName())
            ));
        }
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfullyWithValidCredentials() {
            LoginRequestDTO dto = new LoginRequestDTO("joao@email.com", "password123");
            String token = "generated-token";

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(tokenService.generateToken(any(User.class))).thenReturn(token);

            ResponseEntity<LoginResponseDTO> response = userService.login(dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().success());
            assertEquals("Logged in successfully", response.getBody().message());
            assertEquals(token, response.getBody().token());
        }

        @Test
        @DisplayName("Should return 400 when email is null or empty")
        void shouldReturn400WhenEmailIsNullOrEmpty() {
            ResponseEntity<LoginResponseDTO> response =
                    userService.login(new LoginRequestDTO("", "password123"));

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Email is required", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 400 when password is null or empty")
        void shouldReturn400WhenPasswordIsNullOrEmpty() {
            ResponseEntity<LoginResponseDTO> response =
                    userService.login(new LoginRequestDTO("joao@email.com", ""));

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Password is required", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 401 when user not found")
        void shouldReturn401WhenUserNotFound() {
            LoginRequestDTO dto = new LoginRequestDTO("joao@email.com", "password123");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            ResponseEntity<LoginResponseDTO> response = userService.login(dto);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid email or password", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 401 when password doesn't match")
        void shouldReturn401WhenPasswordDoesntMatch() {
            LoginRequestDTO dto = new LoginRequestDTO("joao@email.com", "wrongpassword");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            ResponseEntity<LoginResponseDTO> response = userService.login(dto);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid email or password", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            UpdateRequestUserDTO dto = new UpdateRequestUserDTO(
                    "João Updated", "Silva Updated", "newemail@email.com", "passwordUpdated"
            );
            User updatedUser = createMockUser();
            updatedUser.setName("João Updated");
            updatedUser.setLastName("Silva Updated");
            updatedUser.setEmail("newemail@email.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userRepository.existsByEmail("newemail@email.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            ResponseEntity<UpdateResponseDTO> response = userService.updateUser(userId, dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().success());
            assertEquals("User updated successfully", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            UpdateRequestUserDTO dto = new UpdateRequestUserDTO("João", "Silva", null, "password");
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ResponseEntity<UpdateResponseDTO> response = userService.updateUser(userId, dto);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("User not found", response.getBody().message());
        }

        @Test
        @DisplayName("Should return 409 when new email is same as current")
        void shouldReturn409WhenNewEmailIsSameAsCurrent() {
            UpdateRequestUserDTO dto = new UpdateRequestUserDTO(null, null, "joao@email.com", null);
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            ResponseEntity<UpdateResponseDTO> response = userService.updateUser(userId, dto);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertEquals("New email must be different from current email",
                    response.getBody().message());
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailAlreadyExists() {
            UpdateRequestUserDTO dto = new UpdateRequestUserDTO(null, null, "existing@email.com", null);
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

            ResponseEntity<UpdateResponseDTO> response = userService.updateUser(userId, dto);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertEquals("Email already in use by another user", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            when(userRepository.existsById(userId)).thenReturn(true);

            ResponseEntity<DeleteResponseDTO> response = userService.deleteUser(userId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().success());
            assertEquals("User deleted successfully", response.getBody().message());
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            when(userRepository.existsById(userId)).thenReturn(false);

            ResponseEntity<DeleteResponseDTO> response = userService.deleteUser(userId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertFalse(response.getBody().success());
            assertEquals("User not found", response.getBody().message());
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should capitalize first letters correctly")
        void shouldCapitalizeFirstLettersCorrectly() {
            assertEquals("João Da Silva", UserService.capitalizeFirstLetters("joão da silva"));
            assertEquals("Maria", UserService.capitalizeFirstLetters("MARIA"));
            assertEquals("José Antônio", UserService.capitalizeFirstLetters("josé antônio"));
            assertEquals("", UserService.capitalizeFirstLetters(""));
            assertEquals("A", UserService.capitalizeFirstLetters("a"));
            assertNull(UserService.capitalizeFirstLetters(null));
        }

        @Test
        @DisplayName("Should calculate age correctly")
        void shouldCalculateAgeCorrectly() {
            User testUser = new User();
            testUser.setDateOfBirth(LocalDate.now().minusYears(25));

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            ResponseEntity<ResponseUserDTO> response = userService.findUserById(userId);

            verify(userRepository, times(1)).findById(userId);

            assertEquals(25, response.getBody().user().age());
        }

    }
}