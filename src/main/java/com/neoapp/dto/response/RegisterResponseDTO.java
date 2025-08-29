package com.neoapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegisterResponseDTO(
        boolean success,
        String message,
        String token,
        DataUserDTO user,
        String timestamp) {

    public static RegisterResponseDTO success(String message, String token, DataUserDTO user) {
        return new RegisterResponseDTO(true, message, token, user, java.time.Instant.now().toString());
    }

    public static RegisterResponseDTO error(String message) {
        return new RegisterResponseDTO(false, message, null, null, java.time.Instant.now().toString());
    }
}
