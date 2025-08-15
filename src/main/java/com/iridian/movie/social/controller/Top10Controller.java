package com.iridian.movie.social.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.iridian.movie.social.dto.RankUpdate;
import com.iridian.movie.social.dto.Top10Flat;
import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.model.Top10;
import com.iridian.movie.social.model.User;
import com.iridian.movie.social.repository.Top10Repository;
import com.iridian.movie.social.repository.UserRepository;
import com.iridian.movie.social.service.MovieCommentLikeService;
import com.iridian.movie.social.util.GenreMap;

@RestController
@RequestMapping("/api/top10")
public class Top10Controller {

    private final Top10Repository top10Repository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private MovieCommentLikeService likeService;

    public Top10Controller(Top10Repository top10Repository, UserRepository userRepository) {
        this.top10Repository = top10Repository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Top10Flat> addTop10(@RequestBody Top10 Top10,
            @RequestParam String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Top10.setUser(user);
        Top10 saved = top10Repository.save(Top10);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<Top10Flat>> getTop10(
            @RequestParam String userId,
            @RequestParam(required = false) String viewerId) {

        List<Top10> top10 = top10Repository.findByUserUserIdOrderByRankAsc(userId);
        List<Top10Flat> dtos = top10.stream()
                .map(this::convertToFlatDTO)
                .collect(Collectors.toList());

        if (likeService != null) {
            for (Top10Flat dto : dtos) {
                try {
                    Map<String, Object> likeData = likeService.getLikeData(
                            dto.getId(),
                            EntryType.TOP10,
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

    private Top10Flat convertToFlatDTO(Top10 f) {
        return new Top10Flat(
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
                f.getRank(),
                f.getMovieDescription(),
                f.getPublicScore(),
                GenreMap.toNames(f.getGenreIds()),
                f.getCreatedAt());
    }

    @PutMapping("/{top10Id}")
    public ResponseEntity<Top10Flat> updateTop10(@PathVariable Long top10Id,
            @RequestBody Top10 updatedTop10,
            @RequestParam String userId) {
        Top10 existing = top10Repository.findById(top10Id)
                .orElseThrow(() -> new RuntimeException("Top10 not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setComment(updatedTop10.getComment());
        existing.setUserScore(updatedTop10.getUserScore());
        existing.setCommentEnabled(updatedTop10.getCommentEnabled());

        Top10 saved = top10Repository.save(existing);
        return ResponseEntity.ok(convertToFlatDTO(saved));
    }

    @PutMapping("/rank")
    public ResponseEntity<?> updateRanks(@RequestBody List<RankUpdate> updates) {
        for (RankUpdate update : updates) {
            Optional<Top10> movieOpt = top10Repository.findById(update.getId());
            movieOpt.ifPresent(movie -> {
                movie.setRank(update.getRank());
                top10Repository.save(movie);
            });
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{top10Id}")
    public ResponseEntity<Top10Flat> deleteMovieTop10(@PathVariable Long top10Id,
            @RequestParam String userId) {
        Top10 existing = top10Repository.findById(top10Id)
                .orElseThrow(() -> new RuntimeException("Top10 not found"));

        if (!existing.getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        top10Repository.delete(existing);
        return ResponseEntity.noContent().build();
    }

    // ============= LIKE/DISLIKE METHODS =============
    @PostMapping("/{top10Id}/like")
    public ResponseEntity<Map<String, Object>> likeTop10(
            @PathVariable Long top10Id,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        Top10 top10 = top10Repository.findById(top10Id)
                .orElseThrow(() -> new RuntimeException("Top10 entry not found"));

        if (top10.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot like your own top10 entry");
            return ResponseEntity.badRequest().body(response);
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                        top10Id, "TOP10", userId, true
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

    @PostMapping("/{top10Id}/dislike")
    public ResponseEntity<Map<String, Object>> dislikeTop10(
            @PathVariable Long top10Id,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        Top10 top10 = top10Repository.findById(top10Id)
                .orElseThrow(() -> new RuntimeException("Top10 entry not found"));

        if (top10.getUser().getUserId().equals(userId)) {
            response.put("error", "You cannot dislike your own top10 entry");
            return ResponseEntity.badRequest().body(response);
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.toggleLike(
                        top10Id, "TOP10", userId, false
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

    @GetMapping("/{top10Id}/likes")
    public ResponseEntity<Map<String, Object>> getTop10Likes(
            @PathVariable Long top10Id,
            @RequestParam(required = false) String userId) {

        Map<String, Object> data = new HashMap<>();

        if (!top10Repository.existsById(top10Id)) {
            data.put("error", "Top10 entry not found");
            return ResponseEntity.notFound().build();
        }

        if (likeService != null) {
            try {
                return ResponseEntity.ok(likeService.getLikeData(
                        top10Id, EntryType.TOP10, userId
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
