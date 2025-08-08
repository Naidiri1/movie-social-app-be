package com.iridian.movie.social.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iridian.movie.social.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId); 
    Optional<User> findByShareSlugAndShareEnabledTrue(UUID shareSlug);
}
