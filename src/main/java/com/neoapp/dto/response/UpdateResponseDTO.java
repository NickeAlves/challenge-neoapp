package com.neoapp.dto.response;

public record UpdateResponseDTO(
        boolean success,
        String message,
        DataUserDTO user,
        String timestamp
) {
    public static UpdateResponseDTO success(String message, DataUserDTO user) {
        return new UpdateResponseDTO(true, message, user, java.time.Instant.now().toString());
    }

    public static UpdateResponseDTO error(String message) {
        return new UpdateResponseDTO(false, message, null, java.time.Instant.now().toString());
    }
}
