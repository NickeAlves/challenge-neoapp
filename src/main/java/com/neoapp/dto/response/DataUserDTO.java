package com.neoapp.dto.response;

import java.util.UUID;

public record DataUserDTO(UUID id,
                          String name,
                          String lastName,
                          String cpf,
                          String email,
                          Integer age) {

    public String cpf() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." +
                cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" +
                cpf.substring(9, 11);
    }
}