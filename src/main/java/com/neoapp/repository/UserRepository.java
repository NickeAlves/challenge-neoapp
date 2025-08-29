package com.neoapp.repository;

import com.neoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    Optional<User> findByName(String name);
    Boolean existsByName(String name);

    Optional<User> findByCpf(String cpf);
    Boolean existsByCpf(String cpf);


}
