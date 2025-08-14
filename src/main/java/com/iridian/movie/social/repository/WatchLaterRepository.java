package com.iridian.movie.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.model.WatchLater;

@Repository
public interface WatchLaterRepository extends JpaRepository<WatchLater, Long> {
    List<WatchLater> findByUserUserId(String userId);
    Page<WatchLater> findByUserUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    long countByUserUserId(String userId);

    Optional<WatchLater> findByUserUserIdAndMovieId(String userId, Long movieId);
}
