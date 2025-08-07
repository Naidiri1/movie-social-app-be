package com.iridian.movie.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.model.WatchLater;

@Repository
public interface WatchLaterRepository extends JpaRepository<WatchLater, Long> {
    List<WatchLater> findByUser_UserId(String userId);
    Optional<WatchLater> findByUser_UserIdAndMovieId(String userId, Long movieId);
}
