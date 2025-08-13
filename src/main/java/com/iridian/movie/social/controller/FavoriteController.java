package com.iridian.movie.social.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.dto.FavoriteFlat;
import com.iridian.movie.social.model.Favorites;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.FavoriteRepository;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<FavoriteFlat> addFavorite(@RequestBody Favorites favorite,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        favorite.setUser(user);
        Favorites saved = favoriteRepository.save(favorite);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteFlat>> getFavorites(@RequestParam String userId) {
        List<Favorites> favorites = favoriteRepository.findByUserUserId(userId);
        List<FavoriteFlat> dtos = favorites.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private FavoriteFlat convertToFlatDTO(Favorites f) {
        return new FavoriteFlat(
                f.getId(),
                f.getUser().getUserId(),
                f.getUser().getUsername(),
                f.getMovieId(),
                f.getTitle(),
                f.getPosterPath(),
                f.getComment(),
                f.getUserScore(),
                f.getCommentEnabled(),
                f.getReleasedDate(),
                f.getMovieDescription(),
                f.getPublicScore(),
                GenreMap.toNames(f.getGenreIds()), // Convert genre IDs to names
                f.getCreatedAt()
        );
    }

    @PutMapping("/{favoriteId}")
    public ResponseEntity<FavoriteFlat> updateFavorite(@PathVariable Long favoriteId,
            @RequestBody Favorites updatedFavorite,
            @RequestParam String userId) {
        Favorites existing = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedFavorite.getComment());
        existing.setUserScore(updatedFavorite.getUserScore());
        existing.setCommentEnabled(updatedFavorite.getCommentEnabled());

        Favorites saved = favoriteRepository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<FavoriteFlat> deleteMovieFav(@PathVariable Long favoriteId,
            @RequestParam String userId) {
        Favorites existing = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        favoriteRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}