package com.neoapp.dto.response;

public record ResponseUserDTO(
        boolean success,
        String message,
        DataUserDTO user,
        String timestamp
) {
    public static ResponseUserDTO success(String message, DataUserDTO user) {
        return new ResponseUserDTO(true, message, user, java.time.Instant.now().toString());
    }

    public static ResponseUserDTO error(String message) {
        return new ResponseUserDTO(false, message, null, java.time.Instant.now().toString());
    }

    public static ResponseUserDTO notFound(String message) {
        return new ResponseUserDTO(false, message, null, java.time.Instant.now().toString());
    }
}
