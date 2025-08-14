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

import com.iridian.movie.social.dto.WatchLaterFlat;
import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.model.WatchLater;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.repository.WatchLaterRepository;
import com.iridian.movie.social.service.MovieCommentLikeService;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/watch-later")
public class WatchLaterController {

    private final WatchLaterRepository watchLaterRepository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private MovieCommentLikeService likeService;

    public WatchLaterController(WatchLaterRepository watchLaterRepository, UserRepository userRepository) {
        this.watchLaterRepository = watchLaterRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<WatchLaterFlat> addWatchLater(@RequestBody WatchLater watchLater,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        watchLater.setUser(user);
        WatchLater saved = watchLaterRepository.save(watchLater);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<WatchLaterFlat>> getWatchLater(
            @RequestParam String userId,
            @RequestParam(required = false) String viewerId) {

        List<WatchLater> watchLaterList = watchLaterRepository.findByUserUserId(userId);
        List<WatchLaterFlat> dtos = watchLaterList.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());

        if (likeService != null) {
            for (WatchLaterFlat dto : dtos) {
                try {
                    Map<String, Object> likeData = likeService.getLikeData(
                            dto.getId(),
                            EntryType.WATCH_LATER,
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

    private WatchLaterFlat convertToFlatDTO(WatchLater w) {
        return new WatchLaterFlat(
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

    @PutMapping("/{watchLaterId}")
    public ResponseEntity<WatchLaterFlat> updateWatchLater(@PathVariable Long watchLaterId,
            @RequestBody WatchLater updatedWatchLater,
            @RequestParam String userId) {
        WatchLater existing = watchLaterRepository.findById(watchLaterId)
                .orElseThrow(() -> new RuntimeException("WatchLater not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedWatchLater.getComment());
        existing.setUserScore(updatedWatchLater.getUserScore());
        existing.setCommentEnabled(updatedWatchLater.getCommentEnabled());

        WatchLater saved = watchLaterRepository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @DeleteMapping("/{watchLaterId}")
    public ResponseEntity<WatchLaterFlat> deleteMovieWatchLater(@PathVariable Long watchLaterId,
            @RequestParam String userId) {
        WatchLater existing = watchLaterRepository.findById(watchLaterId)
                .orElseThrow(() -> new RuntimeException("WatchLater not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        watchLaterRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    // ============= NEW LIKE/DISLIKE METHODS =============
    @PostMapping("/{watchLaterId}/like")
    public ResponseEntity<Map<String, Object>> likeWatchLater(
            @PathVariable Long watchLaterId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        WatchLater watchLater = watchLaterRepository.findById(watchLaterId)
                .orElseThrow(() -> new RuntimeException("Watch-later entry not found"));

        if (watchLater.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot like your own watch-later entry");
            return ResponseEntity.badRequest().body(response);
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                        watchLaterId, "WATCH_LATER", userId, true
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

    @PostMapping("/{watchLaterId}/dislike")
    public ResponseEntity<Map<String, Object>> dislikeWatchLater(
            @PathVariable Long watchLaterId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        WatchLater watchLater = watchLaterRepository.findById(watchLaterId)
                .orElseThrow(() -> new RuntimeException("Watch-later entry not found"));

        if (watchLater.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot dislike your own watch-later entry");
            return ResponseEntity.badRequest().body(response);
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                        watchLaterId, "WATCH_LATER", userId, false
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

    @GetMapping("/{watchLaterId}/likes")
    public ResponseEntity<Map<String, Object>> getWatchLaterLikes(
            @PathVariable Long watchLaterId,
            @RequestParam(required = false) String userId) {

        Map<String, Object> data = new HashMap<>();

        if (!watchLaterRepository.existsById(watchLaterId)) {
            data.put("error", "Watch-later entry not found");
            return ResponseEntity.notFound().build();
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.getLikeData(
                        watchLaterId, EntryType.WATCH_LATER, userId
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
