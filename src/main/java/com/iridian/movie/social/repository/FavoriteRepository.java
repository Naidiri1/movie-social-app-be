package com.iridian.movie.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.model.Favorites;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorites, Long> {
    List<Favorites> findByUser_UserId(String userId);
    Optional<Favorites> findByUser_UserIdAndMovieId(String userId, Long movieId);
}
