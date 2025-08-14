package com.iridian.movie.social.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.iridian.movie.social.dto.FavoriteFlat;
import com.iridian.movie.social.dto.Top10Flat;
import com.iridian.movie.social.dto.UserProfileDTO;
import com.iridian.movie.social.dto.UserSearchDTO;
import com.iridian.movie.social.dto.WatchLaterFlat;
import com.iridian.movie.social.dto.WatchedFlat;
import com.iridian.movie.social.model.Favorites;
import com.iridian.movie.social.model.Top10;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.model.WatchLater;
import com.iridian.movie.social.model.Watched;
import com.iridian.movie.social.repository.FavoriteRepository;
import com.iridian.movie.social.repository.Top10Repository;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.repository.WatchLaterRepository;
import com.iridian.movie.social.repository.WatchedRepository;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchedRepository watchedRepository;
    private final Top10Repository top10Repository;
    private final WatchLaterRepository watchLaterRepository;
    
    public UserController(UserRepository userRepository,
                         FavoriteRepository favoriteRepository,
                         WatchedRepository watchedRepository,
                         Top10Repository top10Repository,
                         WatchLaterRepository watchLaterRepository) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.watchedRepository = watchedRepository;
        this.top10Repository = top10Repository;
        this.watchLaterRepository = watchLaterRepository;
    }
    
    // ============= SEARCH ENDPOINTS (Returns users with counts) =============
    
    @GetMapping("/search")
    public ResponseEntity<Page<UserSearchDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (query == null || query.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query cannot be empty");
        }
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        
        // Use the optimized query that returns DTOs directly with counts
        Page<UserSearchDTO> users = userRepository.searchUsersWithCounts(query.trim(), pageable);
        
        return ResponseEntity.ok(users);
    }
    
    // ============= USER PROFILE ENDPOINT (Returns complete user data) =============
    
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String userId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Get all lists for this user (using the List versions, not Page)
        List<Favorites> favoritesList = favoriteRepository.findByUserUserId(userId);
        List<FavoriteFlat> favorites = favoritesList.stream()
                .map(this::convertToFavoriteFlat)
                .collect(Collectors.toList());
        
        List<Watched> watchedList = watchedRepository.findByUserUserId(userId);
        List<WatchedFlat> watched = watchedList.stream()
                .map(this::convertToWatchedFlat)
                .collect(Collectors.toList());
        
        List<Top10> top10List = top10Repository.findByUserUserIdOrderByRankAsc(userId);
        List<Top10Flat> top10 = top10List.stream()
                .map(this::convertToTop10Flat)
                .collect(Collectors.toList());
        
        List<WatchLater> watchLaterList = watchLaterRepository.findByUserUserId(userId);
        List<WatchLaterFlat> watchLater = watchLaterList.stream()
                .map(this::convertToWatchLaterFlat)
                .collect(Collectors.toList());
        
        // Create complete profile DTO (no email for security)
        UserProfileDTO profile = new UserProfileDTO(
                user.getUserId(),
                user.getUsername(),
                favorites,
                watched,
                top10,
                watchLater
        );
        
        return ResponseEntity.ok(profile);
    }
    
    // ============= INDIVIDUAL LIST ENDPOINTS (For lazy loading) =============
    
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<Page<FavoriteFlat>> getUserFavorites(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorites> favorites = favoriteRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        
        Page<FavoriteFlat> dtos = favorites.map(this::convertToFavoriteFlat);
                
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{userId}/watched")
    public ResponseEntity<Page<WatchedFlat>> getUserWatched(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Watched> watched = watchedRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        
        Page<WatchedFlat> dtos = watched.map(this::convertToWatchedFlat);
                
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{userId}/top10")
    public ResponseEntity<List<Top10Flat>> getUserTop10(@PathVariable String userId) {
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        // Top 10 doesn't need pagination since it's always 10 items
        List<Top10> top10 = top10Repository.findByUserUserIdOrderByRankAsc(userId);
        List<Top10Flat> dtos = top10.stream()
                .map(this::convertToTop10Flat)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{userId}/watch-later")
    public ResponseEntity<Page<WatchLaterFlat>> getUserWatchLater(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<WatchLater> watchLater = watchLaterRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        
        Page<WatchLaterFlat> dtos = watchLater.map(this::convertToWatchLaterFlat);
                
        return ResponseEntity.ok(dtos);
    }
    
    // ============= SHARING ENDPOINTS =============
    
    @GetMapping("/share/{shareSlug}")
    public ResponseEntity<UserProfileDTO> getSharedProfile(@PathVariable UUID shareSlug) {
        
        User user = userRepository.findByShareSlugAndShareEnabledTrue(shareSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found or disabled"));
        
        // Return the same as getUserProfile
        return getUserProfile(user.getUserId());
    }
    
    // ============= CONVERSION METHODS =============
    
    private FavoriteFlat convertToFavoriteFlat(Favorites f) {
        if (f == null) return null;
        
        return new FavoriteFlat(
                f.getId(),
                f.getUser() != null ? f.getUser().getUserId() : null,
                f.getUser() != null ? f.getUser().getUsername() : null,
                f.getMovieId(),
                f.getTitle(),
                f.getPosterPath(),
                f.getComment(),
                f.getUserScore(),
                f.getCommentEnabled() != null ? f.getCommentEnabled() : false,
                f.getReleasedDate(),
                f.getMovieDescription(),
                f.getPublicScore(),
                f.getGenreIds() != null ? GenreMap.toNames(f.getGenreIds()) : List.of(),
                f.getCreatedAt()
        );
    }
    
    private WatchedFlat convertToWatchedFlat(Watched w) {
        if (w == null) return null;
        
        return new WatchedFlat(
                w.getId(),
                w.getUser() != null ? w.getUser().getUserId() : null,
                w.getUser() != null ? w.getUser().getUsername() : null,
                w.getMovieId(),
                w.getTitle(),
                w.getPosterPath(),
                w.getComment(),
                w.getUserScore(),
                w.getCommentEnabled() != null ? w.getCommentEnabled() : false,
                w.getReleasedDate(),
                w.getMovieDescription(),
                w.getPublicScore(),
                w.getGenreIds() != null ? GenreMap.toNames(w.getGenreIds()) : List.of(),
                w.getCreatedAt()
        );
    }
    
    private Top10Flat convertToTop10Flat(Top10 t) {
        if (t == null) return null;
        
        return new Top10Flat(
                t.getId(),
                t.getUser() != null ? t.getUser().getUserId() : null,
                t.getUser() != null ? t.getUser().getUsername() : null,
                t.getMovieId(),
                t.getTitle(),
                t.getPosterPath(),
                t.getComment(),
                t.getUserScore(),
                t.getCommentEnabled() != null ? t.getCommentEnabled() : false,
                t.getReleasedDate(),
                t.getRank(),
                t.getMovieDescription(),
                t.getPublicScore(),
                t.getGenreIds() != null ? GenreMap.toNames(t.getGenreIds()) : List.of(),
                t.getCreatedAt()
        );
    }
    
    private WatchLaterFlat convertToWatchLaterFlat(WatchLater w) {
        if (w == null) return null;
        
        return new WatchLaterFlat(
                w.getId(),
                w.getUser() != null ? w.getUser().getUserId() : null,
                w.getUser() != null ? w.getUser().getUsername() : null,
                w.getMovieId(),
                w.getTitle(),
                w.getPosterPath(),
                w.getComment(),
                w.getUserScore(),
                w.getCommentEnabled() != null ? w.getCommentEnabled() : false,
                w.getReleasedDate(),
                w.getMovieDescription(),
                w.getPublicScore(),
                w.getGenreIds() != null ? GenreMap.toNames(w.getGenreIds()) : List.of(),
                w.getCreatedAt()
        );
    }
}