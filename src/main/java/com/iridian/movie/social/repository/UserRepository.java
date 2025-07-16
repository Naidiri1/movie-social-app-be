package com.iridian.movie.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iridian.movie.social.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
