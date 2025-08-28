package com.neoapp.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterUserDTO(@NotBlank(message = "Name is required")
                              @Size(max = 50)
                              String name,

                              @NotBlank(message = "Last name is required")
                              @Size(max = 50)
                              String lastName,

                              @NotBlank(message = "CPF is required")
                              @Pattern(regexp = "\\d{11}", message = "CPF must contain 11 digits")
                              String cpf,

                              @NotNull
                              @Past
                              @JsonFormat(pattern = "dd/MM/yyyy")
                              LocalDate dateOfBirth,

                              @Email
                              @NotBlank(message = "Email is required")
                              String email,

                              @NotBlank(message = "Password is required")
                              @Size(min = 6, max = 100, message = "Password must be a minimum of 6 characters")
                              String password) {
}
