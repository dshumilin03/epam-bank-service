package com.epam.bank.repositories;

import com.epam.bank.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByPassportId(String passportId);
}
