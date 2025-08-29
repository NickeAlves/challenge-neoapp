package com.neoapp.service;

import com.neoapp.dto.request.RegisterUserDTO;
import com.neoapp.dto.request.UpdateUserDTO;
import com.neoapp.dto.response.*;
import com.neoapp.entity.User;
import com.neoapp.repository.UserRepository;
import com.neoapp.security.TokenService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    );

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<PaginatedResponseDTO<DataUserDTO>> listUsersPaginated(int page, int size, String sortBy, String sortDirection) {
        try {
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            if (sortBy == null || sortBy.isEmpty()) sortBy = "name";

            Sort.Direction direction = Sort.Direction.ASC;
            if ("desc".equalsIgnoreCase(sortDirection)) {
                direction = Sort.Direction.DESC;
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<User> usersPage = userRepository.findAll(pageable);

            if (usersPage.isEmpty()) {
                return ResponseEntity.ok(PaginatedResponseDTO.error("No users found"));
            }

            Page<DataUserDTO> userDTOsPage = usersPage.map(user -> new DataUserDTO(
                    user.getId(),
                    user.getName(),
                    user.getLastName(),
                    user.getCpf(),
                    user.getEmail(),
                    calculateAge(user)
            ));

            return ResponseEntity.ok(PaginatedResponseDTO.success("Users retrieved successfully", userDTOsPage));

        } catch (Exception exception) {
            logger.error("Error listing users with pagination: ", exception);
            return ResponseEntity.internalServerError()
                    .body(PaginatedResponseDTO.error("Internal server error occurred while retrieving users"));
        }
    }

    public ResponseEntity<ResponseUserDTO> findUserById(UUID id) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                logger.warn("User not found with id: {}", id);
                return ResponseEntity.status(404)
                        .body(ResponseUserDTO.notFound("User not found with the provided ID"));
            }

            User user = optionalUser.get();
            DataUserDTO userDTO = new DataUserDTO(
                    user.getId(),
                    user.getName(),
                    user.getLastName(),
                    user.getCpf(),
                    user.getEmail(),
                    calculateAge(user)
            );

            return ResponseEntity.ok(ResponseUserDTO.success("User found successfully", userDTO));

        } catch (Exception exception) {
            logger.error("Error finding user by ID: ", exception);
            return ResponseEntity.internalServerError()
                    .body(ResponseUserDTO.notFound("Internal server error occurred while searching for user"));
        }
    }

    public ResponseEntity<ResponseUserDTO> findUserByEmail(String email) {
        try {
            String adjustedEmail = email.trim().toLowerCase();

            if (!EMAIL_PATTERN.matcher(adjustedEmail).matches()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUserDTO.notFound("Invalid email format"));
            }

            Optional<User> optionalUser = userRepository.findByEmail(adjustedEmail);

            if (optionalUser.isEmpty()) {
                logger.warn("User not found with email: {}", adjustedEmail);
                return ResponseEntity.status(404)
                        .body(ResponseUserDTO.notFound("User not found with the provided email"));
            }

            User user = optionalUser.get();
            DataUserDTO userDTO = new DataUserDTO(
                    user.getId(),
                    user.getName(),
                    user.getLastName(),
                    user.getCpf(),
                    user.getEmail(),
                    calculateAge(user)
            );

            return ResponseEntity.ok(ResponseUserDTO.success("User found successfully", userDTO));

        } catch (Exception exception) {
            logger.error("Error finding user by email: ", exception);
            return ResponseEntity.internalServerError()
                    .body(ResponseUserDTO.notFound("Internal server error occurred while searching for user"));
        }
    }

    @Transactional
    public ResponseEntity<RegisterResponseDTO> register(RegisterUserDTO dto) {
        try {
            String email = dto.email().trim().toLowerCase();

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return ResponseEntity.badRequest()
                        .body(RegisterResponseDTO.error("Invalid email format"));
            }

            if (userRepository.existsByEmail(email)) {
                logger.warn("Registration attempt with existing email: {}", email);
                return ResponseEntity.badRequest()
                        .body(RegisterResponseDTO.error("Email already registered"));
            }

            if (userRepository.existsByCpf(dto.cpf())) {
                logger.warn("Registration attempt with existing CPF: {}", dto.cpf());
                return ResponseEntity.badRequest()
                        .body(RegisterResponseDTO.error("CPF already registered"));
            }

            User user = new User();
            user.setName(capitalizeFirstLetters(dto.name()));
            user.setLastName(capitalizeFirstLetters(dto.lastName()));
            user.setCpf(dto.cpf());
            user.setDateOfBirth(dto.dateOfBirth());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(dto.password()));

            User savedUser = userRepository.save(user);
            String token = tokenService.generateToken(savedUser);

            DataUserDTO userData = createCustomerData(savedUser);

            logger.info("User registered successfully with email: {}", email);
            return ResponseEntity.status(201)
                    .body(RegisterResponseDTO.success("User registered successfully", token, userData));

        } catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Registration validation failed: {}", illegalArgumentException.getMessage());
            return ResponseEntity.badRequest()
                    .body(RegisterResponseDTO.error(illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            logger.error("Unexpected error during registration: ", exception);
            return ResponseEntity.internalServerError()
                    .body(RegisterResponseDTO.error("An unexpected error occurred during registration"));
        }
    }

    public ResponseEntity<UpdateResponseDTO> updateUser(UUID id, UpdateUserDTO dto) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                logger.warn("Update attempt for non-existent user with id: {}", id);
                return ResponseEntity.status(404)
                        .body(UpdateResponseDTO.error("User not found"));
            }

            User existingUser = optionalUser.get();

            if (dto.name() != null && !dto.name().trim().isEmpty()) {
                existingUser.setName(capitalizeFirstLetters(dto.name()));
            }

            if (dto.lastName() != null && !dto.lastName().trim().isEmpty()) {
                existingUser.setLastName(capitalizeFirstLetters(dto.lastName()));
            }

            if (dto.email() != null && !dto.email().isEmpty()) {
                String newEmail = dto.email().trim().toLowerCase();

                if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                    return ResponseEntity.badRequest()
                            .body(UpdateResponseDTO.error("Invalid email format"));
                }

                if (newEmail.equals(existingUser.getEmail())) {
                    return ResponseEntity.status(409)
                            .body(UpdateResponseDTO.error("New email must be different from current email"));
                }

                if (userRepository.existsByEmail(newEmail)) {
                    return ResponseEntity.status(409)
                            .body(UpdateResponseDTO.error("Email already in use by another user"));
                }

                existingUser.setEmail(newEmail);
            }

            User updatedUser = userRepository.save(existingUser);
            DataUserDTO updatedUserDTO = new DataUserDTO(
                    updatedUser.getId(),
                    updatedUser.getName(),
                    updatedUser.getLastName(),
                    updatedUser.getCpf(),
                    updatedUser.getEmail(),
                    calculateAge(updatedUser)
            );

            logger.info("User updated successfully with id: {}", id);
            return ResponseEntity.ok(UpdateResponseDTO.success("User updated successfully", updatedUserDTO));

        } catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Update validation failed: {}", illegalArgumentException.getMessage());
            return ResponseEntity.badRequest()
                    .body(UpdateResponseDTO.error(illegalArgumentException.getMessage()));
        } catch (Exception exception) {
            logger.error("Unexpected error during user update: ", exception);
            return ResponseEntity.internalServerError()
                    .body(UpdateResponseDTO.error("An unexpected error occurred during update"));
        }
    }

    @Transactional
    public ResponseEntity<DeleteResponseDTO> deleteUser(UUID id) {
        try {
            if (!userRepository.existsById(id)) {
                logger.warn("Delete attempt for non-existent user with id: {}", id);
                return ResponseEntity.status(404)
                        .body(DeleteResponseDTO.error("User not found"));
            }

            userRepository.deleteById(id);
            logger.info("User deleted successfully with id: {}", id);
            return ResponseEntity.ok(DeleteResponseDTO.success("User deleted successfully"));

        } catch (Exception exception) {
            logger.error("Unexpected error during user deletion: ", exception);
            return ResponseEntity.internalServerError()
                    .body(DeleteResponseDTO.error("An unexpected error occurred during deletion"));
        }
    }

    public static String capitalizeFirstLetters(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

    private DataUserDTO createCustomerData(User user) {
        return new DataUserDTO(user.getId(),
                user.getName(),
                user.getLastName(),
                user.getCpf(),
                user.getEmail(),
                calculateAge(user));
    }

    private int calculateAge(User user) {
        LocalDate userDateOfBirth = user.getDateOfBirth();

        return Period.between(userDateOfBirth, LocalDate.now()).getYears();
    }
}
