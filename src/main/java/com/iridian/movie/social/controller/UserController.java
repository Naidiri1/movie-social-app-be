package com.iridian.movie.social.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import com.iridian.movie.social.model.EntryType;
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
import com.iridian.movie.social.service.MovieCommentLikeService;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchedRepository watchedRepository;
    private final Top10Repository top10Repository;
    private final WatchLaterRepository watchLaterRepository;
    
    @Autowired(required = false)
    private MovieCommentLikeService likeService;
    
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
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @PathVariable String userId,
            @RequestParam(required = false) String viewerId) {  // Add viewerId parameter
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Get all lists for this user
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
        
        if (likeService != null && viewerId != null) {
            enrichFavoritesWithLikes(favorites, viewerId);
            enrichWatchedWithLikes(watched, viewerId);
            enrichTop10WithLikes(top10, viewerId);
            enrichWatchLaterWithLikes(watchLater, viewerId);
        }
        
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String viewerId) {
        
        System.out.println("=== Getting favorites for user: " + userId + ", viewer: " + viewerId + " ===");
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorites> favorites = favoriteRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<FavoriteFlat> dtoList = favorites.getContent().stream()
                .map(this::convertToFavoriteFlat)
                .collect(Collectors.toList());
        
        // Enrich with likes
        if (likeService != null && viewerId != null) {
            enrichFavoritesWithLikes(dtoList, viewerId);
        }
        
        Page<FavoriteFlat> dtoPage = new PageImpl<>(dtoList, pageable, favorites.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/{userId}/watched")
    public ResponseEntity<Page<WatchedFlat>> getUserWatched(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String viewerId) {
        
        System.out.println("=== Getting watched for user: " + userId + ", viewer: " + viewerId + " ===");
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Watched> watched = watchedRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<WatchedFlat> dtoList = watched.getContent().stream()
                .map(this::convertToWatchedFlat)
                .collect(Collectors.toList());
        
        // Enrich with likes
        if (likeService != null && viewerId != null) {
            enrichWatchedWithLikes(dtoList, viewerId);
        }
        
        Page<WatchedFlat> dtoPage = new PageImpl<>(dtoList, pageable, watched.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/{userId}/top10")
    public ResponseEntity<List<Top10Flat>> getUserTop10(
            @PathVariable String userId,
            @RequestParam(required = false) String viewerId) {
        
        System.out.println("=== Getting top10 for user: " + userId + ", viewer: " + viewerId + " ===");
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        List<Top10> top10 = top10Repository.findByUserUserIdOrderByRankAsc(userId);
        List<Top10Flat> dtos = top10.stream()
                .map(this::convertToTop10Flat)
                .collect(Collectors.toList());
        
        // Enrich with likes
        if (likeService != null && viewerId != null) {
            enrichTop10WithLikes(dtos, viewerId);
        }
                
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{userId}/watch-later")
    public ResponseEntity<Page<WatchLaterFlat>> getUserWatchLater(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String viewerId) {
        
        System.out.println("=== Getting watch-later for user: " + userId + ", viewer: " + viewerId + " ===");
        
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<WatchLater> watchLater = watchLaterRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<WatchLaterFlat> dtoList = watchLater.getContent().stream()
                .map(this::convertToWatchLaterFlat)
                .collect(Collectors.toList());
        
        // Enrich with likes
        if (likeService != null && viewerId != null) {
            enrichWatchLaterWithLikes(dtoList, viewerId);
        }
        
        Page<WatchLaterFlat> dtoPage = new PageImpl<>(dtoList, pageable, watchLater.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }
    
    // ============= SHARING ENDPOINTS =============
    
    @GetMapping("/share/{shareSlug}")
    public ResponseEntity<UserProfileDTO> getSharedProfile(
            @PathVariable UUID shareSlug,
            @RequestParam(required = false) String viewerId) {
        
        User user = userRepository.findByShareSlugAndShareEnabledTrue(shareSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found or disabled"));
        
        return getUserProfile(user.getUserId(), viewerId);
    }
    
    // ============= ENRICHMENT METHODS =============
    
    private void enrichFavoritesWithLikes(List<FavoriteFlat> dtos, String viewerId) {
        if (dtos.isEmpty()) return;
        
        try {
            List<Long> entryIds = dtos.stream()
                    .map(FavoriteFlat::getId)
                    .collect(Collectors.toList());
            
            Map<Long, Map<String, Object>> batchLikeData = likeService.getBatchLikeData(
                entryIds, EntryType.FAVORITE, viewerId
            );
            
            for (FavoriteFlat dto : dtos) {
                Map<String, Object> likeData = batchLikeData.get(dto.getId());
                if (likeData != null) {
                    dto.setCommentLikes((Long) likeData.getOrDefault("likes", 0L));
                    dto.setCommentDislikes((Long) likeData.getOrDefault("dislikes", 0L));
                    dto.setUserLikeStatus((String) likeData.get("userStatus"));
                } else {
                    dto.setCommentLikes(0L);
                    dto.setCommentDislikes(0L);
                    dto.setUserLikeStatus(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error enriching favorites: " + e.getMessage());
            setDefaultLikeValues(dtos);
        }
    }
    
    private void enrichWatchedWithLikes(List<WatchedFlat> dtos, String viewerId) {
        if (dtos.isEmpty()) return;
        
        try {
            List<Long> entryIds = dtos.stream()
                    .map(WatchedFlat::getId)
                    .collect(Collectors.toList());
            
            Map<Long, Map<String, Object>> batchLikeData = likeService.getBatchLikeData(
                entryIds, EntryType.WATCHED, viewerId
            );
            
            for (WatchedFlat dto : dtos) {
                Map<String, Object> likeData = batchLikeData.get(dto.getId());
                if (likeData != null) {
                    dto.setCommentLikes((Long) likeData.getOrDefault("likes", 0L));
                    dto.setCommentDislikes((Long) likeData.getOrDefault("dislikes", 0L));
                    dto.setUserLikeStatus((String) likeData.get("userStatus"));
                } else {
                    dto.setCommentLikes(0L);
                    dto.setCommentDislikes(0L);
                    dto.setUserLikeStatus(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error enriching watched: " + e.getMessage());
            setDefaultLikeValuesWatched(dtos);
        }
    }
    
    private void enrichTop10WithLikes(List<Top10Flat> dtos, String viewerId) {
        if (dtos.isEmpty()) return;
        
        try {
            List<Long> entryIds = dtos.stream()
                    .map(Top10Flat::getId)
                    .collect(Collectors.toList());
            
            Map<Long, Map<String, Object>> batchLikeData = likeService.getBatchLikeData(
                entryIds, EntryType.TOP10, viewerId
            );
            
            for (Top10Flat dto : dtos) {
                Map<String, Object> likeData = batchLikeData.get(dto.getId());
                if (likeData != null) {
                    dto.setCommentLikes((Long) likeData.getOrDefault("likes", 0L));
                    dto.setCommentDislikes((Long) likeData.getOrDefault("dislikes", 0L));
                    dto.setUserLikeStatus((String) likeData.get("userStatus"));
                } else {
                    dto.setCommentLikes(0L);
                    dto.setCommentDislikes(0L);
                    dto.setUserLikeStatus(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error enriching top10: " + e.getMessage());
            setDefaultLikeValuesTop10(dtos);
        }
    }
    
    private void enrichWatchLaterWithLikes(List<WatchLaterFlat> dtos, String viewerId) {
        if (dtos.isEmpty()) return;
        
        try {
            List<Long> entryIds = dtos.stream()
                    .map(WatchLaterFlat::getId)
                    .collect(Collectors.toList());
            
            Map<Long, Map<String, Object>> batchLikeData = likeService.getBatchLikeData(
                entryIds, EntryType.WATCH_LATER, viewerId
            );
            
            for (WatchLaterFlat dto : dtos) {
                Map<String, Object> likeData = batchLikeData.get(dto.getId());
                if (likeData != null) {
                    dto.setCommentLikes((Long) likeData.getOrDefault("likes", 0L));
                    dto.setCommentDislikes((Long) likeData.getOrDefault("dislikes", 0L));
                    dto.setUserLikeStatus((String) likeData.get("userStatus"));
                } else {
                    dto.setCommentLikes(0L);
                    dto.setCommentDislikes(0L);
                    dto.setUserLikeStatus(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error enriching watch-later: " + e.getMessage());
            setDefaultLikeValuesWatchLater(dtos);
        }
    }
    
    private void setDefaultLikeValues(List<FavoriteFlat> dtos) {
        for (FavoriteFlat dto : dtos) {
            dto.setCommentLikes(0L);
            dto.setCommentDislikes(0L);
            dto.setUserLikeStatus(null);
        }
    }
    
    private void setDefaultLikeValuesWatched(List<WatchedFlat> dtos) {
        for (WatchedFlat dto : dtos) {
            dto.setCommentLikes(0L);
            dto.setCommentDislikes(0L);
            dto.setUserLikeStatus(null);
        }
    }
    
    private void setDefaultLikeValuesTop10(List<Top10Flat> dtos) {
        for (Top10Flat dto : dtos) {
            dto.setCommentLikes(0L);
            dto.setCommentDislikes(0L);
            dto.setUserLikeStatus(null);
        }
    }
    
    private void setDefaultLikeValuesWatchLater(List<WatchLaterFlat> dtos) {
        for (WatchLaterFlat dto : dtos) {
            dto.setCommentLikes(0L);
            dto.setCommentDislikes(0L);
            dto.setUserLikeStatus(null);
        }
    }
    
    // ============= CONVERSION METHODS =============
    
    private FavoriteFlat convertToFavoriteFlat(Favorites f) {
        if (f == null) return null;
        
        FavoriteFlat dto = new FavoriteFlat();
        dto.setId(f.getId());
        dto.setUserId(f.getUser() != null ? f.getUser().getUserId() : null);
        dto.setUsername(f.getUser() != null ? f.getUser().getUsername() : null);
        dto.setMovieId(f.getMovieId());
        dto.setTitle(f.getTitle());
        dto.setPosterPath(f.getPosterPath());
        dto.setComment(f.getComment());
        dto.setUserScore(f.getUserScore());
        dto.setCommentEnabled(f.getCommentEnabled() != null ? f.getCommentEnabled() : false);
        dto.setReleasedDate(f.getReleasedDate());
        dto.setMovieDescription(f.getMovieDescription());
        dto.setPublicScore(f.getPublicScore());
        dto.setGenres(f.getGenreIds() != null ? GenreMap.toNames(f.getGenreIds()) : List.of());
        dto.setCreatedAt(f.getCreatedAt());
        
        return dto;
    }
    
    private WatchedFlat convertToWatchedFlat(Watched w) {
        if (w == null) return null;
        
        WatchedFlat dto = new WatchedFlat();
        dto.setId(w.getId());
        dto.setUserId(w.getUser() != null ? w.getUser().getUserId() : null);
        dto.setUsername(w.getUser() != null ? w.getUser().getUsername() : null);
        dto.setMovieId(w.getMovieId());
        dto.setTitle(w.getTitle());
        dto.setPosterPath(w.getPosterPath());
        dto.setComment(w.getComment());
        dto.setUserScore(w.getUserScore());
        dto.setCommentEnabled(w.getCommentEnabled() != null ? w.getCommentEnabled() : false);
        dto.setReleasedDate(w.getReleasedDate());
        dto.setMovieDescription(w.getMovieDescription());
        dto.setPublicScore(w.getPublicScore());
        dto.setGenres(w.getGenreIds() != null ? GenreMap.toNames(w.getGenreIds()) : List.of());
        dto.setCreatedAt(w.getCreatedAt());
        
        return dto;
    }
    
    private Top10Flat convertToTop10Flat(Top10 t) {
        if (t == null) return null;
        
        Top10Flat dto = new Top10Flat();
        dto.setId(t.getId());
        dto.setUserId(t.getUser() != null ? t.getUser().getUserId() : null);
        dto.setUsername(t.getUser() != null ? t.getUser().getUsername() : null);
        dto.setMovieId(t.getMovieId());
        dto.setTitle(t.getTitle());
        dto.setPosterPath(t.getPosterPath());
        dto.setComment(t.getComment());
        dto.setUserScore(t.getUserScore());
        dto.setCommentEnabled(t.getCommentEnabled() != null ? t.getCommentEnabled() : false);
        dto.setReleasedDate(t.getReleasedDate());
        dto.setRank(t.getRank());
        dto.setMovieDescription(t.getMovieDescription());
        dto.setPublicScore(t.getPublicScore());
        dto.setGenres(t.getGenreIds() != null ? GenreMap.toNames(t.getGenreIds()) : List.of());
        dto.setCreatedAt(t.getCreatedAt());
        
        return dto;
    }
    
    private WatchLaterFlat convertToWatchLaterFlat(WatchLater w) {
        if (w == null) return null;
        
        WatchLaterFlat dto = new WatchLaterFlat();
        dto.setId(w.getId());
        dto.setUserId(w.getUser() != null ? w.getUser().getUserId() : null);
        dto.setUsername(w.getUser() != null ? w.getUser().getUsername() : null);
        dto.setMovieId(w.getMovieId());
        dto.setTitle(w.getTitle());
        dto.setPosterPath(w.getPosterPath());
        dto.setComment(w.getComment());
        dto.setUserScore(w.getUserScore());
        dto.setCommentEnabled(w.getCommentEnabled() != null ? w.getCommentEnabled() : false);
        dto.setReleasedDate(w.getReleasedDate());
        dto.setMovieDescription(w.getMovieDescription());
        dto.setPublicScore(w.getPublicScore());
        dto.setGenres(w.getGenreIds() != null ? GenreMap.toNames(w.getGenreIds()) : List.of());
        dto.setCreatedAt(w.getCreatedAt());
        
        return dto;
    }
}