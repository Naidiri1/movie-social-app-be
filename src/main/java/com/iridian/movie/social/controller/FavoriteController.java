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
        
        System.out.println("=== Getting favorites for user: " + userId + ", viewer: " + viewerId + " ===");
        
        // Get all favorites for the user
        List<Favorites> favorites = favoriteRepository.findByUserUserId(userId);
        System.out.println("Found " + favorites.size() + " favorites");
        
        // Convert to DTOs
        List<FavoriteFlat> dtos = favorites.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());
        
        // Enrich with like data if service is available
        if (likeService != null && !dtos.isEmpty()) {
            System.out.println("Like service available, enriching data...");
            
            try {
                // Use batch processing if available
                List<Long> entryIds = dtos.stream()
                        .map(FavoriteFlat::getId)
                        .collect(Collectors.toList());
                
                System.out.println("Getting like data for entry IDs: " + entryIds);
                
                // Try batch method first
                try {
                    Map<Long, Map<String, Object>> batchLikeData = likeService.getBatchLikeData(
                        entryIds, 
                        EntryType.FAVORITE, 
                        viewerId
                    );
                    
                    for (FavoriteFlat dto : dtos) {
                        Map<String, Object> likeData = batchLikeData.get(dto.getId());
                        if (likeData != null) {
                            dto.setCommentLikes((Long) likeData.getOrDefault("likes", 0L));
                            dto.setCommentDislikes((Long) likeData.getOrDefault("dislikes", 0L));
                            dto.setUserLikeStatus((String) likeData.get("userStatus"));
                            
                            System.out.println("Favorite " + dto.getId() + 
                                " - Likes: " + dto.getCommentLikes() + 
                                ", Dislikes: " + dto.getCommentDislikes() + 
                                ", User Status: " + dto.getUserLikeStatus());
                        }
                    }
                    
                } catch (Exception batchError) {
                    System.out.println("Batch processing failed, falling back to individual queries: " + batchError.getMessage());
                    
                    // Fallback to individual queries
                    for (FavoriteFlat dto : dtos) {
                        try {
                            Map<String, Object> likeData = likeService.getLikeData(
                                dto.getId(),
                                EntryType.FAVORITE,
                                viewerId
                            );
                            
                            dto.setCommentLikes((Long) likeData.getOrDefault("likes", 0L));
                            dto.setCommentDislikes((Long) likeData.getOrDefault("dislikes", 0L));
                            dto.setUserLikeStatus((String) likeData.get("userStatus"));
                            
                            System.out.println("Favorite " + dto.getId() + 
                                " - Likes: " + dto.getCommentLikes() + 
                                ", Dislikes: " + dto.getCommentDislikes() + 
                                ", User Status: " + dto.getUserLikeStatus());
                            
                        } catch (Exception e) {
                            System.err.println("Error enriching favorite " + dto.getId() + ": " + e.getMessage());
                            dto.setCommentLikes(0L);
                            dto.setCommentDislikes(0L);
                            dto.setUserLikeStatus(null);
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error in like enrichment: " + e.getMessage());
                e.printStackTrace();
                // Set defaults if enrichment fails
                for (FavoriteFlat dto : dtos) {
                    dto.setCommentLikes(0L);
                    dto.setCommentDislikes(0L);
                    dto.setUserLikeStatus(null);
                }
            }
        } else {
            if (likeService == null) {
                System.out.println("Like service not available");
            }
            if (dtos.isEmpty()) {
                System.out.println("No favorites to enrich");
            }
            
            // Set default values
            for (FavoriteFlat dto : dtos) {
                dto.setCommentLikes(0L);
                dto.setCommentDislikes(0L);
                dto.setUserLikeStatus(null);
            }
        }
        
        System.out.println("=== Returning " + dtos.size() + " favorites ===");
        return ResponseEntity.ok(dtos);
    }

   private FavoriteFlat convertToFlatDTO(Favorites favorite) {
    FavoriteFlat dto = new FavoriteFlat();
    dto.setId(favorite.getId());
    dto.setMovieId(favorite.getMovieId());
    dto.setTitle(favorite.getTitle());
    dto.setPosterPath(favorite.getPosterPath());
    dto.setComment(favorite.getComment());
    dto.setUserScore(favorite.getUserScore());
    dto.setCommentEnabled(favorite.getCommentEnabled());
    dto.setReleasedDate(favorite.getReleasedDate());
    dto.setMovieDescription(favorite.getMovieDescription());
    dto.setPublicScore(favorite.getPublicScore());
    dto.setUserId(favorite.getUser().getUserId());
    dto.setUsername(favorite.getUser().getUsername());
    dto.setCreatedAt(favorite.getCreatedAt());
    
    if (favorite.getGenreIds() != null) {
        dto.setGenres(GenreMap.toNames(favorite.getGenreIds()));
    }
    
    // DON'T set like data here - it will be enriched later
    // dto.setCommentLikes(0L);  // Remove this if it exists
    // dto.setCommentDislikes(0L);  // Remove this if it exists
    // dto.setUserLikeStatus(null);  // Remove this if it exists
    
    return dto;
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
