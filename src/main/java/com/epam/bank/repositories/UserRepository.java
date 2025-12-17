package com.epam.bank.repositories;

import com.epam.bank.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPassportId(String passportId);

    Boolean existsByEmail(String email);

    List<User> findByFullName(String fullName);

}
