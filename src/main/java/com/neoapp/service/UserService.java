package com.neoapp.service;

import com.neoapp.dto.request.RegisterUserDTO;
import com.neoapp.dto.request.UpdateUserDTO;
import com.neoapp.dto.response.DataUpdatedUserDTO;
import com.neoapp.dto.response.DataUserDTO;
import com.neoapp.dto.response.ResponseUserDTO;
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

    public ResponseEntity<Page<DataUserDTO>> listUsersPaginated(int page, int size, String sortBy, String sortDirection) {
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

            Page<DataUserDTO> userDTOsPage = usersPage.map(user -> new DataUserDTO(
                    user.getId(),
                    user.getName(),
                    user.getLastName(),
                    user.getCpf(),
                    user.getEmail(),
                    calculateAge(user)
            ));

            return ResponseEntity.ok(userDTOsPage);
        } catch (Exception exception) {
            logger.error("Error listing users with pagination: ", exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Transactional
    public ResponseEntity<ResponseUserDTO> register(RegisterUserDTO dto) {
        try {
            String email = dto.email().trim().toLowerCase();

            if (userRepository.existsByEmail(email)) {
                logger.warn("Registration attempt with existing email.");
                return ResponseEntity.badRequest().body(new ResponseUserDTO(false, null, "Email already registered.", null));
            }

            if (userRepository.existsByCpf(dto.cpf())) {
                logger.warn("User already registered.");
                return ResponseEntity.badRequest().body(new ResponseUserDTO(false, null, "User already registered.", null));
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

            logger.info("User registered successfully.");

            return ResponseEntity.ok(new ResponseUserDTO(true, token, "Registered successfully.", createCustomerData(savedUser)));
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Registration validation failed: {}", illegalArgumentException.getMessage());
            return ResponseEntity.badRequest().body(new ResponseUserDTO(false, null, illegalArgumentException.getMessage(), null));
        } catch (Exception exception) {
            logger.error("Unexpected error during registration: ", exception);
            return ResponseEntity.internalServerError().body(new ResponseUserDTO(false, null, "An unexpected error occurred", null));
        }
    }

    public ResponseEntity<DataUpdatedUserDTO> updateUser(UUID id, UpdateUserDTO dto) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                logger.warn("User not found with id: {}", id);
                return ResponseEntity.status(404).body(new DataUpdatedUserDTO(false, "User not found.", null, null, null, null, null, null));
            }

            User existingUser = optionalUser.get();

            if (dto.name() != null) existingUser.setName(capitalizeFirstLetters(dto.name()));
            if (dto.lastName() != null) existingUser.setName(capitalizeFirstLetters(dto.lastName()));

            if (dto.email() != null && !dto.email().isEmpty()) {
                String newEmail = dto.email().trim().toLowerCase();

                if (newEmail.equals(existingUser.getEmail())) {
                    logger.warn("Email same as previous one. Please choose a different email.");
                    return ResponseEntity.status(409)
                            .body(new DataUpdatedUserDTO(false, "Email same as previous one. Please choose a different email.", null, null, null, null, null, null));
                }

                existingUser.setEmail(newEmail);
            }

            userRepository.save(existingUser);
            logger.info("User updated successfully!");
            return ResponseEntity.ok().body(new DataUpdatedUserDTO(true, "User updated successfully!", existingUser.getId(), existingUser.getName(), existingUser.getLastName(), existingUser.getCpf(), existingUser.getEmail(), calculateAge(existingUser)));
        } catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Updated failed: {}", illegalArgumentException.getMessage());
            return ResponseEntity.badRequest().body(new DataUpdatedUserDTO(false, illegalArgumentException.getMessage(), null, null, null, null, null, null));
        } catch (Exception exception) {
            logger.error("Unexpected error during updating: ", exception);
            return ResponseEntity.internalServerError().body(new DataUpdatedUserDTO(false, "An unexpected error occurred", null, null, null, null, null, null));
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
