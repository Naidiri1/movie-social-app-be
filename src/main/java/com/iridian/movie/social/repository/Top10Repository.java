package com.iridian.movie.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.model.Top10;

@Repository
public interface Top10Repository extends JpaRepository<Top10, Long> {
  

    Optional<Top10> findByUserUserIdAndMovieId(String userId, Long movieId);
    long countByUserUserId(String userId);

    List<Top10> findByUserUserIdOrderByRankAsc(String userId);

}
