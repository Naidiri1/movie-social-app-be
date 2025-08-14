package com.iridian.movie.social.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.iridian.movie.social.dto.WatchedFlat;
import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.model.Watched;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.repository.WatchedRepository;
import com.iridian.movie.social.service.MovieCommentLikeService;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/watched")
public class WatchedController {

    private final WatchedRepository watchedRepository;
    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private MovieCommentLikeService likeService;

    public WatchedController(WatchedRepository watchedRepository, UserRepository userRepository) {
        this.watchedRepository = watchedRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<WatchedFlat> addWatched(@RequestBody Watched watched,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        watched.setUser(user);
        Watched saved = watchedRepository.save(watched);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<Page<WatchedFlat>> getWatched(
            @RequestParam String userId,
            @RequestParam(required = false) String viewerId,
            Pageable pageable) {
        
        Page<Watched> watchedPage = watchedRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<WatchedFlat> dtoPage = watchedPage.map(this::convertToFlatDTO);
        
        if (likeService != null && !dtoPage.isEmpty()) {
            List<WatchedFlat> content = dtoPage.getContent();
            for (WatchedFlat dto : content) {
                try {
                    Map<String, Object> likeData = likeService.getLikeData(
                        dto.getId(),
                        EntryType.WATCHED,
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
        
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/all")
    public ResponseEntity<List<WatchedFlat>> getAllWatched(
            @RequestParam String userId,
            @RequestParam(required = false) String viewerId) {
        
        List<Watched> watchedList = watchedRepository.findByUserUserId(userId);
        List<WatchedFlat> dtos = watchedList.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());
        
        if (likeService != null) {
            for (WatchedFlat dto : dtos) {
                try {
                    Map<String, Object> likeData = likeService.getLikeData(
                        dto.getId(),
                        EntryType.WATCHED,
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

    private WatchedFlat convertToFlatDTO(Watched w) {
        return new WatchedFlat(
                w.getId(),
                w.getUser().getUserId(),
                w.getUser().getUsername(),
                w.getMovieId(),
                w.getTitle(),
                w.getPosterPath(),
                w.getComment(),
                w.getUserScore(),
                w.getCommentEnabled(),
                w.getReleasedDate(),
                w.getMovieDescription(),
                w.getPublicScore(),
                GenreMap.toNames(w.getGenreIds()),
                w.getCreatedAt()
        );
    }

    @PutMapping("/{watchedId}")
    public ResponseEntity<WatchedFlat> updateWatched(@PathVariable Long watchedId,
            @RequestBody Watched updatedWatched,
            @RequestParam String userId) {
        Watched existing = watchedRepository.findById(watchedId)
                .orElseThrow(() -> new RuntimeException("Watched entry not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedWatched.getComment());
        existing.setUserScore(updatedWatched.getUserScore());
        existing.setCommentEnabled(updatedWatched.getCommentEnabled());

        Watched saved = watchedRepository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @DeleteMapping("/{watchedId}")
    public ResponseEntity<Void> deleteWatched(@PathVariable Long watchedId,
            @RequestParam String userId) {
        Watched existing = watchedRepository.findById(watchedId)
                .orElseThrow(() -> new RuntimeException("Watched entry not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        watchedRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    // ============= LIKE/DISLIKE METHODS =============

    @PostMapping("/{watchedId}/like")
    public ResponseEntity<Map<String, Object>> likeWatched(
            @PathVariable Long watchedId,
            @RequestParam String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        Watched watched = watchedRepository.findById(watchedId)
                .orElseThrow(() -> new RuntimeException("Watched entry not found"));
        
        if (watched.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot like your own watched entry");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                    watchedId, "WATCHED", userId, true
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
    

    @PostMapping("/{watchedId}/dislike")
    public ResponseEntity<Map<String, Object>> dislikeWatched(
            @PathVariable Long watchedId,
            @RequestParam String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        Watched watched = watchedRepository.findById(watchedId)
                .orElseThrow(() -> new RuntimeException("Watched entry not found"));
        
        if (watched.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot dislike your own watched entry");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                    watchedId, "WATCHED", userId, false
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
    
    @GetMapping("/{watchedId}/likes")
    public ResponseEntity<Map<String, Object>> getWatchedLikes(
            @PathVariable Long watchedId,
            @RequestParam(required = false) String userId) {
        
        Map<String, Object> data = new HashMap<>();
        
        if (!watchedRepository.existsById(watchedId)) {
            data.put("error", "Watched entry not found");
            return ResponseEntity.notFound().build();
        }
        
        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.getLikeData(
                    watchedId, EntryType.WATCHED, userId
                ));
            } catch (Exception e) {
                data.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(data);
            }
        }
        
        data.put("likes", 0L);
        data.put("dislikes", 0L);
        data.put("userStatus", null);
        data.put("message", "Mock response - service not available");
        return ResponseEntity.ok(data);
    }
}