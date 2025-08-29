package com.neoapp.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateRequestUserDTO(String name,
                                   String lastName,
                                   String email,
                                   String password) {

}
