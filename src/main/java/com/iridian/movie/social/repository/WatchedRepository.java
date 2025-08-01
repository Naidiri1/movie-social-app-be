package com.iridian.movie.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.model.Watched;

@Repository
public interface WatchedRepository extends JpaRepository<Watched, Long> {
    List<Watched> findByUser_UserId(String userId);
    Optional<Watched> findByUser_UserIdAndMovieId(String userId, Long movieId);
}
