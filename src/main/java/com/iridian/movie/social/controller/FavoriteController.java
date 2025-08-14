package com.iridian.movie.social.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.model.Favorites;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.FavoriteRepository;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.service.MovieCommentLikeService;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private MovieCommentLikeService likeService;

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
    public ResponseEntity<List<FavoriteFlat>> getFavorites(
            @RequestParam String userId,
            @RequestParam(required = false) String viewerId) {

        List<Favorites> favorites = favoriteRepository.findByUserUserId(userId);
        List<FavoriteFlat> dtos = favorites.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());

        if (likeService != null) {
            for (FavoriteFlat dto : dtos) {
                try {
                    Map<String, Object> likeData = likeService.getLikeData(
                            dto.getId(),
                            EntryType.FAVORITE,
                            viewerId
                    );

                    dto.setCommentLikes((Long) likeData.get("likes"));
                    dto.setCommentDislikes((Long) likeData.get("dislikes"));
                    dto.setUserLikeStatus((String) likeData.get("userStatus"));
                } catch (Exception e) {
                    dto.setCommentLikes(0L);
                    dto.setCommentDislikes(0L);
                    dto.setUserLikeStatus(null);
                }
            }
        }

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

    // ============= NEW LIKE/DISLIKE METHODS =============
    @PostMapping("/{favoriteId}/like")
    public ResponseEntity<Map<String, Object>> likeFavorite(
            @PathVariable Long favoriteId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        Favorites favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        if (favorite.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot like your own favorite");
            return ResponseEntity.badRequest().body(response);
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                        favoriteId, "FAVORITE", userId, true
                ));
            } catch (Exception e) {
                response.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }

        response.put("action", "added");
        response.put("likes", 1L);
        response.put("dislikes", 0L);
        response.put("message", "Mock response - service not available");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{favoriteId}/dislike")
    public ResponseEntity<Map<String, Object>> dislikeFavorite(
            @PathVariable Long favoriteId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        Favorites favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        if (favorite.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot dislike your own favorite");
            return ResponseEntity.badRequest().body(response);
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                        favoriteId, "FAVORITE", userId, false
                ));
            } catch (Exception e) {
                response.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }

        response.put("action", "added");
        response.put("likes", 0L);
        response.put("dislikes", 1L);
        response.put("message", "Mock response - service not available");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{favoriteId}/likes")
    public ResponseEntity<Map<String, Object>> getFavoriteLikes(
            @PathVariable Long favoriteId,
            @RequestParam(required = false) String userId) {

        Map<String, Object> data = new HashMap<>();

        if (!favoriteRepository.existsById(favoriteId)) {
            data.put("error", "Favorite not found");
            return ResponseEntity.notFound().build();
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.getLikeData(
                        favoriteId, EntryType.FAVORITE, userId
                ));
            } catch (Exception e) {
                data.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(data);
            }
        }

        data.put("likes", 5L);
        data.put("dislikes", 2L);
        data.put("userStatus", userId != null ? "liked" : null);
        data.put("message", "Mock response - service not available");
        return ResponseEntity.ok(data);
    }
}
