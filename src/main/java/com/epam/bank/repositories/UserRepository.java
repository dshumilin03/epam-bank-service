package com.epam.bank.repositories;

import com.epam.bank.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPassportId(String passportId);

    boolean existsByEmail(String email);

    Optional<User> findByFullName(String fullName);

}
