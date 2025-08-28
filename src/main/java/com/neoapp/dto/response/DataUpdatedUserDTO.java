package com.neoapp.dto.response;

import java.util.UUID;

public record DataUpdatedUserDTO(boolean success,
                                 String message,
                                 UUID id,
                                 String name,
                                 String lastName,
                                 String cpf,
                                 String email,
                                 Integer age) {

    public DataUpdatedUserDTO(boolean success, String message) {
        this(success, message, null, null, null, null, null, null);
    }
}
