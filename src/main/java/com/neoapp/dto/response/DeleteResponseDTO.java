package com.neoapp.dto.response;

public record DeleteResponseDTO(
        boolean success,
        String message,
        String timestamp)
{
    public static DeleteResponseDTO success(String message) {
        return new DeleteResponseDTO(true, message, java.time.Instant.now().toString());
    }

    public static DeleteResponseDTO error(String message) {
        return new DeleteResponseDTO(false, message, java.time.Instant.now().toString());
    }
}
