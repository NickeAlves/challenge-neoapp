package com.neoapp.controller;

import com.neoapp.dto.request.UpdateUserDTO;
import com.neoapp.dto.response.DataUpdatedUserDTO;
import com.neoapp.dto.response.DataUserDTO;
import com.neoapp.dto.response.DeleteUserResponseDTO;
import com.neoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<DataUserDTO>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "name") String sortBy,
                                                      @RequestParam(defaultValue = "asc") String sortDirection) {
        return userService.listUsersPaginated(page, size, sortBy, sortDirection);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataUpdatedUserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDTO dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResponseDTO> deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id);
    }
}
