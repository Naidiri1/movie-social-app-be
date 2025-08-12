package com.iridian.movie.social.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.iridian.movie.social.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
