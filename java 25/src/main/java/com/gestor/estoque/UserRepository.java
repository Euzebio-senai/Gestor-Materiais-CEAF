package com.gestor.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // O Spring entende "findByEMAIL" e cria a query:
    // SELECT * FROM "USER" WHERE EMAIL = ?
    Optional<User> findByEMAIL(String email);
}