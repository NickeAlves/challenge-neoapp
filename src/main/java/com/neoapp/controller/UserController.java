package com.neoapp.controller;

import com.neoapp.dto.request.UpdateRequestUserDTO;
import com.neoapp.dto.response.*;
import com.neoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<DataUserDTO>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(defaultValue = "name") String sortBy,
                                                                      @RequestParam(defaultValue = "asc") String sortDirection) {
        return userService.listUsersPaginated(page, size, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> getUserById(@PathVariable UUID id) {
        return userService.findUserById(id);
    }

    @GetMapping("/email")
    public ResponseEntity<ResponseUserDTO> getUserByEmail(@RequestParam String email) {
        return userService.findUserByEmail(email);
    }

    @GetMapping("/cpf")
    public ResponseEntity<ResponseUserDTO> getUserByCpf(@RequestParam String cpf) {
        return userService.findUserByCpf(cpf);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponseDTO<DataUserDTO>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return userService.searchUsers(q, page, size, sortBy, sortDirection);
    }

    @GetMapping("/search/name")
    public ResponseEntity<PaginatedResponseDTO<DataUserDTO>> searchUsersByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return userService.searchUsersByName(name, page, size, sortBy, sortDirection);
    }

    @GetMapping("/search/lastname")
    public ResponseEntity<PaginatedResponseDTO<DataUserDTO>> searchUsersByLastName(
            @RequestParam String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return userService.searchUsersByLastName(lastName, page, size, sortBy, sortDirection);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UpdateResponseDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateRequestUserDTO dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponseDTO> deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id);
    }
}
