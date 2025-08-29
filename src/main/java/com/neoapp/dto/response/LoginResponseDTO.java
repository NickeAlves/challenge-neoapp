package com.neoapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDTO(
        boolean success,
        String message,
        String token,
        DataUserDTO user,
        String timestamp
) {
    public static LoginResponseDTO success(String message, String token, DataUserDTO user) {
        return new LoginResponseDTO(true, message, token, user, java.time.Instant.now().toString());
    }

    public static LoginResponseDTO error(String message) {
        return new LoginResponseDTO(false, message, null, null, java.time.Instant.now().toString());
    }
}
